package com.r1n1os.jetpackcomposetemplateopensource.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.r1n1os.jetpackcomposetemplateopensource.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Query("""
        SELECT * FROM search_history
        WHERE timestamp >= :since
        GROUP BY query
        ORDER BY COUNT(*) DESC, MAX(timestamp) DESC
        LIMIT 1
    """)
    suspend fun getMostFrequentProduct(since: Long): SearchHistoryEntity?

    @Query("DELETE FROM search_history")
    suspend fun clearAll()

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}
