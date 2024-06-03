package com.example.fridgeapp.data.ui.favoritesItems

import android.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.ui.FridgeViewModel
import com.example.fridgeapp.databinding.FavoriteAddItemBinding

class AddItemToFavoriteFragment : Fragment() {

    private var _binding: FavoriteAddItemBinding? = null
    private val binding
        get() = _binding!!

    private var imageUri: Uri? = null

    private val viewModel: FridgeViewModel by activityViewModels()

    private val pickLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let {
                binding.itemImage.setImageURI(it)
                requireActivity().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                imageUri = it
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FavoriteAddItemBinding.inflate(inflater, container, false)

        val categories = viewModel.categories

        val adapter = CustomArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            categories,
            com.example.fridgeapp.R.font.amaranth // Custom font resource
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productCategory.adapter = adapter

        //Defines what happens when the "Add" button is clicked
        binding.addItemButton.setOnClickListener {
            val name = binding.productName.text.toString()
            val daysToExpireStr = binding.productDaysToExpire.text.toString()
            val category = binding.productCategory.selectedItem.toString()
            val photoUrl = imageUri?.toString()

            // Check if the name is not empty
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a product name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the days to expire is a valid integer
            val daysToExpire = daysToExpireStr.toIntOrNull()
            if (daysToExpire == null || daysToExpire <= 0) {
                Toast.makeText(requireContext(), "Please enter a valid number of days to expire", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //create a FridgeItem by the fields
            val foodItem = FoodItem(
                name = name,
                photoUrl = photoUrl,
                category = category,
                daysToExpire = daysToExpire
            )

            //add the FridgeItem to viewModel
            viewModel.insertFoodItem(foodItem)
            //To do navigation-> after click add, add it to the FridgeFragment
            findNavController().navigate(com.example.fridgeapp.R.id.action_addItemToFavoriteFragment_to_defaultExpirationDatesFragment)
        }

        binding.itemImage.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }

        binding.productCategory

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (hasUnsavedChanges()) {
                        DialogUtils.showConfirmDiscardChangesDialog(requireContext(), onConfirm = {
                            // Navigate back
                            findNavController().popBackStack()
                        }, onCancel = {
                            // Do nothing, just dismiss the dialog
                        })
                    } else {
                        // Navigate back if there are no changes
                        findNavController().popBackStack()
                    }
                }
            })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hasUnsavedChanges(): Boolean {
        val defaultCategory = "Breads"
        return binding.productName.text.toString() != "" ||
                binding.productCategory.selectedItem.toString() != defaultCategory ||
                binding.productDaysToExpire.text.toString() != "" ||
                imageUri?.toString() != null
    }
}

