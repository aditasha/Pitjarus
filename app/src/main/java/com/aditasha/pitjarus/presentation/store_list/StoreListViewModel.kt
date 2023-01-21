package com.aditasha.pitjarus.presentation.store_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditasha.pitjarus.data.repository.StoreListRepositoryImpl
import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.domain.model.StoreEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreListViewModel @Inject constructor(private val storeListRepositoryImpl: StoreListRepositoryImpl) :
    ViewModel() {
    private val _storeListResult = MutableSharedFlow<Result<List<StoreEntity>>>()
    val storeListResult: SharedFlow<Result<List<StoreEntity>>> = _storeListResult

    fun fetchStoreList() {
        viewModelScope.launch {
            _storeListResult.emitAll(storeListRepositoryImpl.fetchStoreList())
        }
    }
}