package com.memora.data.repository

import com.memora.data.db.MemoryDao
import com.memora.data.model.MemoryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val memoryDao: MemoryDao
) {
    val allMemories: Flow<List<MemoryItem>> = memoryDao.getAllMemories()

    fun search(query: String): Flow<List<MemoryItem>> {
        return if (query.isBlank()) {
            allMemories
        } else {
            memoryDao.searchMemories(query)
        }
    }

    suspend fun insert(item: MemoryItem) {
        memoryDao.insertMemory(item)
    }

    suspend fun delete(item: MemoryItem) {
        memoryDao.deleteMemory(item)
    }

    suspend fun clearAll() {
        memoryDao.deleteAll()
    }
}
