package com.r1n1os.jetpackcomposetemplateopensource.di

import android.content.Context
import androidx.room.Room
import com.r1n1os.jetpackcomposetemplateopensource.data.local.AppDatabase
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.ProductDao
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.SearchHistoryDao
import com.r1n1os.jetpackcomposetemplateopensource.data.local.dao.ShoppingListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "smart_app_list_database"
        ).fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }

    @Provides
    @Singleton
    fun provideShoppingListDao(database: AppDatabase): ShoppingListDao {
        return database.shoppingListDao()
    }
}