package com.memora.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memora.data.model.MemoryItem
import com.memora.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow<String?>(null)
    val selectedFilter: StateFlow<String?> = _selectedFilter

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    val searchResults: StateFlow<List<MemoryItem>> = combine(_searchQuery, _selectedFilter) { query, filter ->
        query to filter
    }
        .debounce(300)
        .flatMapLatest { (query, filter) ->
            repository.search(query, filter)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterChanged(newFilter: String?) {
        _selectedFilter.value = if (_selectedFilter.value == newFilter) null else newFilter
    }

    fun deleteMemory(item: MemoryItem) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
