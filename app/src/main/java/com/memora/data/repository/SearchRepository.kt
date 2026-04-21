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

    fun search(query: String, filter: String? = null): Flow<List<MemoryItem>> {
        return when {
            query.isBlank() && filter == null -> allMemories
            query.isBlank() && filter != null -> memoryDao.filterByType(filter)
            else -> memoryDao.searchMemories(query) // Simple search for now, could be improved to intersect
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
