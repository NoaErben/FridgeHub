package com.example.fridgeapp.data.ui.favoritesItems

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fridgeapp.data.model.FoodItem
import com.example.fridgeapp.data.repository.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * FavoriteViewModel is a ViewModel class that manages the data for favorite food items, interacting with the FoodRepository.
 * It leverages coroutines to handle data operations asynchronously on a background thread, ensuring smooth UI performance.
 */

class FavoriteViewModel(private val foodRep: FoodRepository) : ViewModel() {

    private val _chosenFoodItem = MutableLiveData<FoodItem>()

    val foodItems: LiveData<List<FoodItem>>? = foodRep.getAllFoodItems()
    val foodItemsNames: LiveData<List<String>>? = foodRep.getFoodsNameList()
    val chosenFoodItem: LiveData<FoodItem> get() = _chosenFoodItem

    fun setFoodChosenItem(foodItem: FoodItem) {
        _chosenFoodItem.value = foodItem
    }

    fun insertFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            foodRep.insert(foodItem)
        }
    }

    fun deleteFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            foodRep.delete(foodItem)
            Log.d("FridgeViewModel", "Food item deleted from repository")
        }
    }

    fun deleteAllFoodItems() {
        viewModelScope.launch {
            foodRep.deleteAllFoodTable()
        }
    }

    fun updateFoodName(id: Int, name: String) {
        viewModelScope.launch {
            foodRep.updateName(id, name)
        }
    }

    fun updateFoodCategory(id: Int, newCategory: String) {
        viewModelScope.launch {
            foodRep.updateCategory(id, newCategory)
        }

    }

    fun updateFoodPhotoUrl(id: Int, photoUrl: String?) {
        viewModelScope.launch {
            foodRep.updatePhotoUrl(id, photoUrl)
        }
    }

    fun updateFoodDaysToExpire(id: Int, daysToExpire: Int) {
        viewModelScope.launch {
            foodRep.updateDaysToExpire(id, daysToExpire)
        }
    }

    suspend fun getFoodItem(name: String): FoodItem? {
        return withContext(Dispatchers.IO) {
            foodRep.getFoodItem(name)
        }
    }

    fun resetToDefaultItems(defaultFoodItems: List<FoodItem>) {
        viewModelScope.launch {
            foodRep.deleteAll()
            foodRep.insertAll(defaultFoodItems)
        }
    }

    class FavoriteViewModelFactory(private val repo: FoodRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoriteViewModel(repo) as T
        }
    }

}