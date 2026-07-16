package com.r1n1os.jetpackcomposetemplateopensource.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.ShoppingListDao
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ShoppingListEntity
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ShoppingListItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val shoppingListDao: ShoppingListDao
) : ViewModel() {

    private val _list = MutableStateFlow<ShoppingListEntity?>(null)
    val list: StateFlow<ShoppingListEntity?> = _list.asStateFlow()

    val items = MutableStateFlow<List<ShoppingListItemEntity>>(emptyList())

    fun loadList(listId: Long) {
        viewModelScope.launch {
            _list.value = shoppingListDao.getListById(listId)
        }
        viewModelScope.launch {
            shoppingListDao.getItemsForList(listId).collect { itemList ->
                items.value = itemList
            }
        }
    }

    fun toggleItemChecked(item: ShoppingListItemEntity) {
        viewModelScope.launch {
            shoppingListDao.updateItem(item.copy(isChecked = !item.isChecked))
        }
    }

    fun deleteList() {
        viewModelScope.launch {
            _list.value?.let { shoppingListDao.deleteList(it) }
        }
    }

    fun deleteItem(item: ShoppingListItemEntity) {
        viewModelScope.launch {
            shoppingListDao.deleteItem(item)
        }
    }
}
