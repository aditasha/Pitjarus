package com.aditasha.pitjarus.presentation.store_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditasha.pitjarus.data.repository.StoreDetailRepositoryImpl
import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.domain.model.StoreEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreDetailViewModel @Inject constructor(private val storeDetailRepositoryImpl: StoreDetailRepositoryImpl) :
    ViewModel() {
    private val _storeResult = MutableSharedFlow<Result<StoreEntity>>()
    val storeResult: SharedFlow<Result<StoreEntity>> = _storeResult

    fun fetchStore(roomId: Int) {
        viewModelScope.launch {
            _storeResult.emitAll(storeDetailRepositoryImpl.fetchStore(roomId))
        }
    }
}