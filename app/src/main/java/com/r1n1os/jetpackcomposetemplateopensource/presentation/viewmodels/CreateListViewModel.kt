package com.r1n1os.jetpackcomposetemplateopensource.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.ShoppingListDao
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ShoppingListEntity
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ShoppingListItemEntity
import com.r1n1os.jetpackcomposetemplateopensource.data.remote.GeminiAssistant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateListUiState(
    val listName: String = "",
    val items: List<ShoppingListItemEntity> = emptyList(),
    val newItemName: String = "",
    val newItemQuantity: String = "1",
    val savedListId: Long? = null,
    val showGeminiDialog: Boolean = false,
    val geminiInput: String = "",
    val isGeminiLoading: Boolean = false,
    val geminiError: String? = null
)

@HiltViewModel
class CreateListViewModel @Inject constructor(
    private val shoppingListDao: ShoppingListDao,
    private val geminiAssistant: GeminiAssistant
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateListUiState())
    val uiState: StateFlow<CreateListUiState> = _uiState.asStateFlow()

    fun onListNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(listName = name)
    }

    fun onNewItemNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(newItemName = name)
    }

    fun onNewItemQuantityChanged(qty: String) {
        _uiState.value = _uiState.value.copy(newItemQuantity = qty)
    }

    fun addItem() {
        val name = _uiState.value.newItemName.trim()
        if (name.isBlank()) return
        val item = ShoppingListItemEntity(
            listId = 0,
            productName = name,
            quantity = _uiState.value.newItemQuantity
        )
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items + item,
            newItemName = "",
            newItemQuantity = "1"
        )
    }

    fun removeItem(index: Int) {
        val updated = _uiState.value.items.toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _uiState.value = _uiState.value.copy(items = updated)
        }
    }

    fun showGeminiDialog() {
        _uiState.value = _uiState.value.copy(
            showGeminiDialog = true,
            geminiInput = ""
        )
    }

    fun dismissGeminiDialog() {
        _uiState.value = _uiState.value.copy(showGeminiDialog = false)
    }

    fun onGeminiInputChanged(input: String) {
        _uiState.value = _uiState.value.copy(geminiInput = input)
    }

    fun generateWithGemini() {
        val input = _uiState.value.geminiInput.trim()
        if (input.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeminiLoading = true, geminiError = null)
            val result = geminiAssistant.generateShoppingList(input)
            result.fold(
                onSuccess = { items ->
                    val newItems = items.filter { it.isNotBlank() }.map { name ->
                        ShoppingListItemEntity(listId = 0, productName = name)
                    }
                    _uiState.value = _uiState.value.copy(
                        items = _uiState.value.items + newItems,
                        showGeminiDialog = false,
                        isGeminiLoading = false,
                        geminiError = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isGeminiLoading = false,
                        geminiError = error.message ?: "Error desconocido"
                    )
                }
            )
        }
    }

    fun saveList() {
        val name = _uiState.value.listName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            val listId = shoppingListDao.insertList(
                ShoppingListEntity(name = name)
            )
            val itemsWithId = _uiState.value.items.map { it.copy(listId = listId) }
            if (itemsWithId.isNotEmpty()) {
                shoppingListDao.insertItems(itemsWithId)
            }
            _uiState.value = _uiState.value.copy(savedListId = listId)
        }
    }
}
