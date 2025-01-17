package com.example.fridgeapp.data.ui.fridge

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgeapp.R
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.data.repository.firebaseImpl.FridgeRepositoryFirebase
import com.example.fridgeapp.data.ui.utils.Dialogs
import com.example.fridgeapp.data.ui.utils.autoCleared
import com.example.fridgeapp.databinding.FridgeFragmentBinding

/**
 * A Fragment representing the main screen of the fridge manager.
 * It displays a list of items in the fridge with options for managing them.
 */
class FridgeManagerFragment : Fragment() {

    private var binding : FridgeFragmentBinding by autoCleared()
    private lateinit var fridgeItemAdapter: FridgeItemAdapter

    private val viewModel: FridgeViewmodel by activityViewModels {
        FridgeViewmodel.FridgeViewmodelFactory(FridgeRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FridgeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupAdapter()
        observeViewModel()
        setupNavigation()
        setupSwipeActions()
        handleBackButtonPress()
        binding.progressBar?.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupAdapter() {
        fridgeItemAdapter = FridgeItemAdapter(emptyList(), object : FridgeItemAdapter.ItemListener {
            override fun onItemClick(index: Int) {
                val item = (binding.recyclerView.adapter as FridgeItemAdapter).itemAt(index)
                Log.d("FMF2", item.name.toString())
                viewModel.setFridgeChosenItem(item)
                findNavController().navigate(R.id.action_fridgeManagerFragment_to_editFridgeItemFragment)
            }

            override fun onItemLongClick(index: Int) {
                showToast(getString(R.string.swipe_to_delete))
            }
        })
        binding.recyclerView.adapter = fridgeItemAdapter
    }

    private fun observeViewModel() {
        viewModel.items.observe(viewLifecycleOwner, Observer { items ->
            binding.progressBar?.visibility = View.GONE
            Log.d("MyTag", "Observed items: ${items.size}")
            val sortedItems = items.sortedBy { it.timeUntilExpiry() }
            fridgeItemAdapter.updateItems(sortedItems)
            if (items.isEmpty()) {
                binding.emptyImageView?.visibility = View.VISIBLE
                binding.emptyTextView?.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyImageView?.visibility = View.GONE
                binding.emptyTextView?.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        })

        viewModel.currentUser.observe(viewLifecycleOwner, Observer { currentUser ->
            // Handle user change event
            if (currentUser != null) {
                viewModel.userChanged()
            }
        })
    }

    private fun setupNavigation() {
        binding.toolbar.setNavigationOnClickListener {
            showPopupMenu(it)
        }

        binding.addProductExpiryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_fridgeManagerFragment_to_addItemToFridgeFragment)
        }
    }

    private fun setupSwipeActions() {
        ItemTouchHelper(createSwipeCallback()).attachToRecyclerView(binding.recyclerView)
    }

    private fun createSwipeCallback() = object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) = makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val item = (binding.recyclerView.adapter as FridgeItemAdapter).itemAt(viewHolder.adapterPosition)
            Dialogs.showConfirmDeleteDialog(requireContext(),
                onConfirm = {
                    viewModel.deleteItemFromFridgeDatabase(item) { result ->
                        result.onSuccess {
                            showToast(getString(R.string.item_deleted_successfully))
                        }.onFailure { exception ->
                            val errorMessage = getString(R.string.failed_to_delete_item_in_fridge) + exception.message
                            showToast(errorMessage)
                            fridgeItemAdapter.notifyItemChanged(viewHolder.adapterPosition)
                        }
                    }
                },
                onCancel = {
                    fridgeItemAdapter.notifyItemChanged(viewHolder.adapterPosition)
                }
            )
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        // Use reflection to force icons to show
        try {
            val fields = popupMenu::class.java.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popupMenu)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.javaPrimitiveType)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.shopping_list -> {
                    findNavController().navigate(R.id.action_fridgeManagerFragment_to_fridgeShoppingListFragment)
                    true
                }
                R.id.Favorite_items -> {
                    findNavController().navigate(R.id.action_fridgeManagerFragment_to_defaultExpirationDatesFragment)
                    true
                }
                R.id.My_profile -> {
                    handleProfileClick()
                    true
                }
                R.id.My_Location -> {
                    handleLocationClick()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun handleLocationClick() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            findNavController().navigate(R.id.action_fridgeManagerFragment_to_locationFragment)
        } else {
            findNavController().navigate(R.id.action_fridgeManagerFragment_to_allowLocationFragment)
        }
    }


    private fun handleProfileClick() {
        findNavController().navigate(R.id.action_fridgeManagerFragment_to_myProfileFragment)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun FridgeItem.timeUntilExpiry(): Long {
        val currentTime = System.currentTimeMillis()
        return expiryDate - currentTime
    }

    private fun handleBackButtonPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Dialogs.showConfirmLeaveDialog(requireContext(),
                    onConfirm = { requireActivity().finish() },
                    onCancel = { /* Do nothing */ }
                )
            }
        })
    }
}
