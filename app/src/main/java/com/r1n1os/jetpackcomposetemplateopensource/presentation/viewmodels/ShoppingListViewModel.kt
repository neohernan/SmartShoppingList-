package com.r1n1os.jetpackcomposetemplateopensource.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.ShoppingListDao
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ShoppingListEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListDao: ShoppingListDao
) : ViewModel() {

    val lists = shoppingListDao.getAllLists()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun deleteList(list: ShoppingListEntity) {
        viewModelScope.launch {
            shoppingListDao.deleteList(list)
        }
    }

    fun toggleListCompleted(list: ShoppingListEntity) {
        viewModelScope.launch {
            shoppingListDao.updateList(list.copy(isCompleted = !list.isCompleted))
        }
    }
}
