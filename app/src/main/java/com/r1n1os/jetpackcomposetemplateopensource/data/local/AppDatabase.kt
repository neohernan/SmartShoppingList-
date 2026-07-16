package com.r1n1os.jetpackcomposetemplateopensource.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.ProductDao
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.SearchHistoryDao
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.ShoppingListDao
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ProductEntity
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.SearchHistoryEntity
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ShoppingListEntity
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.ShoppingListItemEntity

@Database(
    entities = [
        ProductEntity::class,
        SearchHistoryEntity::class,
        ShoppingListEntity::class,
        ShoppingListItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun shoppingListDao(): ShoppingListDao
}
