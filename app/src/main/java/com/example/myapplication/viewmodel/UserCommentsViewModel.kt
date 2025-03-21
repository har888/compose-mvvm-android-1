package com.example.myapplication.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.model.UserComment
import com.example.myapplication.repository.IUserCommentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserCommentsViewModel @Inject constructor(
    private val userCommentsRepository: IUserCommentsRepository
): ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> get() = _uiState
    private val _selectedImageUris = MutableStateFlow<Map<Int, Uri?>>(emptyMap())
    val selectedImageUris: StateFlow<Map<Int, Uri?>> get() = _selectedImageUris

    fun fetchUserComments() {
        if (_uiState.value is UiState.Loading) {
            viewModelScope.launch {
                _uiState.value = UiState.Loading
                try {
                    userCommentsRepository.getUserComments().collectLatest { comments ->
                        _uiState.value = UiState.Success(comments)
                    }
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(R.string.post_comments_failure_error)
                }
            }
        }
    }

    fun handleErrorDismiss() {
        _uiState.value = UiState.Success(emptyList())
    }

    fun retryFetchUserComments() {
        _uiState.value = UiState.Loading
        fetchUserComments()
    }

    fun updateSelectedImageUri(commentId: Int, uri: Uri?) {
        _selectedImageUris.value = _selectedImageUris.value + (commentId to uri)
    }
}

sealed class UiState {
    data object Loading : UiState()
    data class Success(val data: List<UserComment>) : UiState()
    data class Error(val errorMessageId: Int) : UiState()
}
