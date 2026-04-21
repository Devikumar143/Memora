package com.memora.data.db

import androidx.room.*
import com.memora.data.model.MemoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(item: MemoryItem)

    @Query("SELECT * FROM memory_items ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memory_items WHERE contentText LIKE '%' || :query || '%' OR sourceApp LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMemories(query: String): Flow<List<MemoryItem>>

    @Query("DELETE FROM memory_items")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteMemory(item: MemoryItem)
}
