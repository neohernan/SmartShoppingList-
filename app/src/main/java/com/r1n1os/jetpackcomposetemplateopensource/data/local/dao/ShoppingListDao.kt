package com.r1n1os.jetpackcomposetemplateopensource.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ShoppingListEntity
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    // ── Lists ──────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingListEntity): Long

    @Update
    suspend fun updateList(list: ShoppingListEntity)

    @Delete
    suspend fun deleteList(list: ShoppingListEntity)

    @Query("SELECT * FROM shopping_lists ORDER BY createdAt DESC")
    fun getAllLists(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
    suspend fun getListById(listId: Long): ShoppingListEntity?

    // ── Items ──────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingListItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingListItemEntity>)

    @Update
    suspend fun updateItem(item: ShoppingListItemEntity)

    @Delete
    suspend fun deleteItem(item: ShoppingListItemEntity)

    @Query("SELECT * FROM shopping_list_items WHERE listId = :listId ORDER BY isChecked ASC, id ASC")
    fun getItemsForList(listId: Long): Flow<List<ShoppingListItemEntity>>

    @Query("DELETE FROM shopping_list_items WHERE listId = :listId")
    suspend fun deleteItemsForList(listId: Long)
}
