package com.example.fridgeapp.data.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.fridgeapp.R
import com.example.fridgeapp.data.local_db.FridgeDB
import com.example.fridgeapp.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var fridgeViewModel: FridgeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FirebaseApp.initializeApp(this)

        /*setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationOnClickListener {
            showPopupMenu(it)
        }

         */

        // Initialize the database
        FridgeDB.getDatabase(this)

        // Initialize ViewModel
        fridgeViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(
            FridgeViewModel::class.java)

        // Show food db context for example
        fridgeViewModel.foodItemsNames?.observe(this, Observer<List<String>> { foodNames ->
            // Update UI with the new food names
            val concatenatedNames = fridgeViewModel.getConcatenatedString()
            // For example, you can update a TextView with the concatenated string
            //binding.textv.setText("From Db: " + concatenatedNames)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
