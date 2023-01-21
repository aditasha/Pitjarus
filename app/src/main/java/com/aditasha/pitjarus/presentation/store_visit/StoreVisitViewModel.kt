package com.aditasha.pitjarus.presentation.store_visit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditasha.pitjarus.data.repository.StoreVisitRepositoryImpl
import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.domain.model.StoreEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreVisitViewModel @Inject constructor(private val storeVisitRepositoryImpl: StoreVisitRepositoryImpl) :
    ViewModel() {
    private val _storeResult = MutableSharedFlow<Result<StoreEntity>>()
    val storeResult: SharedFlow<Result<StoreEntity>> = _storeResult

    private val _storePictureResult = MutableSharedFlow<Result<Any>>()
    val storePictureResult: SharedFlow<Result<Any>> = _storePictureResult

    private val _storeVisitedResult = MutableSharedFlow<Result<Long>>()
    val storeVisitedResult: SharedFlow<Result<Long>> = _storeVisitedResult

    fun fetchStore(roomId: Int) {
        viewModelScope.launch {
            _storeResult.emitAll(storeVisitRepositoryImpl.fetchStore(roomId))
        }
    }

    fun updatePicture(roomId: Int, picture: String) {
        viewModelScope.launch {
            _storePictureResult.emitAll(storeVisitRepositoryImpl.updatePicture(roomId, picture))
        }
    }

    fun updateVisited(roomId: Int, visited: Boolean, lastVisited: Long) {
        viewModelScope.launch {
            _storeVisitedResult.emitAll(
                storeVisitRepositoryImpl.updateVisited(
                    roomId,
                    visited,
                    lastVisited
                )
            )
        }
    }
}