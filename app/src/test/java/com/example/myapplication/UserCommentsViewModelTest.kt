package com.example.myapplication

import com.example.myapplication.model.UserComment
import com.example.myapplication.repository.IUserCommentsRepository
import com.example.myapplication.viewmodel.UiState
import com.example.myapplication.viewmodel.UserCommentsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.mockito.kotlin.doThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.setMain

@ExperimentalCoroutinesApi
class UserCommentsViewModelTest {
    private lateinit var viewModel: UserCommentsViewModel
    private lateinit var mockRepository: IUserCommentsRepository

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())

        mockRepository = Mockito.mock(IUserCommentsRepository::class.java)
        viewModel = UserCommentsViewModel(mockRepository)
    }

    @Test
    fun `test fetchUserComments success`() = runTest {
        // Arrange
        val mockComments = listOf(
            UserComment(name = "John", email = "john@example.com", postId = 1, id = 1, body = "Hello")
        )
        whenever(mockRepository.getUserComments()).thenReturn(flowOf(mockComments))

        // Act
        viewModel.fetchUserComments()

        // Assert: Collect state from the ViewModel
        val job = launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // Assert loading state
                        assert(state is UiState.Loading)
                    }
                    is UiState.Success -> {
                        // Assert success state with data
                        assert(state.data == mockComments)
                    }
                    is UiState.Error -> {
                        // This block should not be reached
                        assert(false)
                    }
                    is UiState.Empty -> {
                        // This block should not be reached
                        assert(false)
                    }
                }
            }
        }

        // Wait for the coroutine to finish its work
        advanceUntilIdle()

        // Ensure the job is completed
        job.cancel()
    }

    @Test
    fun `test fetchUserComments error`() = runTest {
        // Arrange: Simulate an error when calling the repository method
        val errorMessage = "Network Error"
        whenever(mockRepository.getUserComments()).doThrow(RuntimeException(errorMessage))

        // Act: Call the function that fetches the comments
        viewModel.fetchUserComments()

        // Assert: Collect and check the emitted states from uiState
        val job = launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // Assert that it shows loading first
                        assert(state is UiState.Loading)
                    }
                    is UiState.Success -> {
                        // This block should not be reached because an error is expected
                        assert(false) // Fail the test if it reaches here
                    }
                    is UiState.Error -> {
                        // Assert that the error state is emitted with the expected error message
                        assert(state.errorMessageId == R.string.post_comments_failure_error)
                    }
                    is UiState.Empty -> {
                        // This block should not be reached
                        assert(false)
                    }
                }
            }
        }

        // Wait for the coroutine to finish its work
        advanceUntilIdle()

        // Ensure the job is completed
        job.cancel()
    }

    @Test
    fun `test fetchUserComments empty list`() = runTest {
        // Arrange: Simulate an empty list from the repository
        val emptyComments = emptyList<UserComment>()
        whenever(mockRepository.getUserComments()).thenReturn(flowOf(emptyComments))

        // Act
        viewModel.fetchUserComments()

        // Assert: Collect and check the emitted states from uiState
        val job = launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // Assert loading state
                        assert(state is UiState.Loading)
                    }
                    is UiState.Success -> {
                        // Assert success state with an empty list
                        assert(state.data.isEmpty())
                    }
                    is UiState.Error -> {
                        // This block should not be reached
                        assert(false)
                    }
                    is UiState.Empty -> {
                        // This block should not be reached
                        assert(false)
                    }
                }
            }
        }

        // Wait for the coroutine to finish its work
        advanceUntilIdle()

        // Ensure the job is completed
        job.cancel()
    }

    @Test
    fun `test handleErrorDismiss updates UI state to Empty`() = runTest {
        // Arrange: Simulate an error state first by making the repository throw an error
        val errorMessage = "Network Error"
        whenever(mockRepository.getUserComments()).doThrow(RuntimeException(errorMessage))

        // Act: Fetch user comments which will result in an error state
        viewModel.fetchUserComments()

        // Collect the first emission of the UI state
        val job = launch {
            // Collect only the first emission, which should be the Error state
            viewModel.uiState.first { state ->
                state is UiState.Error
            }

            // Act: Simulate dismissing the error
            viewModel.handleErrorDismiss()

            // Now collect the next state (Empty)
            val updatedState = viewModel.uiState.first { state ->
                state is UiState.Empty
            }

            // Assert: Ensure the state transitions to Empty
            assert(updatedState is UiState.Empty)
        }

        // Wait for the coroutine to finish its work
        advanceUntilIdle()

        // Ensure the job is completed
        job.cancel()
    }
}