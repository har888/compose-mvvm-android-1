package com.example.myapplication.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.myapplication.R
import com.example.myapplication.model.UserComment
import com.example.myapplication.repository.IUserCommentsRepository
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.UiState
import com.example.myapplication.viewmodel.UserCommentsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun UserCommentsScreen(viewModel: UserCommentsViewModel = hiltViewModel()) {
    Scaffold(
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(paddingValues)
            ) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                when (uiState) {
                    is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is UiState.Success -> UserComments((uiState as UiState.Success).data)
                    is UiState.Error -> {
                        val errorMessageId = (uiState as UiState.Error).errorMessageId
                        ErrorDialog(
                            message = stringResource(id = errorMessageId),
                            onDismiss = {
                                viewModel.handleErrorDismiss()
                            },
                            onRetry = {
                                viewModel.retryFetchUserComments()
                            }
                        )
                    }
                    is UiState.Empty -> {
                        EmptyMessages()
                    }
                }
            }
        }
    )
}

@Composable
fun EmptyMessages() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.no_comments_loaded),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun UserComments(userComments: List<UserComment>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = dimensionResource(id = R.dimen.dimen_5dp),
                vertical = dimensionResource(id = R.dimen.dimen_15dp),
            )
    ) {
        items(userComments) { comment ->
            UserComment(comment)
        }
    }
}

@Composable
fun UserComment(comment: UserComment, viewModel: UserCommentsViewModel = hiltViewModel()) {
    val selectedImageUri by viewModel.selectedImageUris.collectAsStateWithLifecycle()
    val currentImageUri = selectedImageUri[comment.id] ?: R.drawable.profile_user_default
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let{
            viewModel.updateSelectedImageUri(comment.id, uri)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.dimen_1dp)),
        shape = RoundedCornerShape(
            dimensionResource(id = R.dimen.dimen_7dp)
        ),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = dimensionResource(id = R.dimen.dimen_2dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = dimensionResource(id = R.dimen.dimen_5dp),
                    horizontal = dimensionResource(id = R.dimen.dimen_5dp)
                )
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = currentImageUri)
                        .apply(block = fun ImageRequest.Builder.() {
                            transformations(CircleCropTransformation())
                        }).build()
                ),
                contentDescription = "User Image",
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.dimen_100dp))
                    .clickable {
                        launcher.launch("image/*")
                    }
                    .padding(dimensionResource(id = R.dimen.dimen_5dp)),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(id = R.dimen.dimen_10dp), horizontal = dimensionResource(id = R.dimen.dimen_5dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(
                        vertical = dimensionResource(id = R.dimen.dimen_5dp)
                    )
                ) {
                    Text(
                        text = "Name: ",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(end = dimensionResource(id = R.dimen.dimen_2dp))
                    )
                    Text(
                        text = comment.name,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(
                        vertical = dimensionResource(id = R.dimen.dimen_5dp)
                    )
                ) {
                    Text(
                        text = "Email: ",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(end = dimensionResource(id = R.dimen.dimen_2dp))
                    )
                    Text(
                        text = comment.email,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(
                        vertical = dimensionResource(id = R.dimen.dimen_5dp)
                    )
                ) {
                    Text(
                        text = "ID: ",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(end = dimensionResource(id = R.dimen.dimen_2dp))
                    )
                    Text(
                        text = comment.id.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(
                        vertical = dimensionResource(id = R.dimen.dimen_5dp)
                    )
                ) {
                    Text(
                        text = "Body: ",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(end = dimensionResource(id = R.dimen.dimen_2dp))
                    )
                    Text(
                        text = comment.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUserCommentsScreen() {
    MyApplicationTheme {
        val mockUserComments = listOf(
            UserComment(name = "John", email = "john@example.com", postId = 1, id = 1, body = "Hello, World!"),
            UserComment(name = "id labore ex et quam laborum", email = "JohnSmith@example.com", postId = 1, id = 2, body = "quia molestiae reprehenderit quasi aspernatur\\naut expedita occaecati aliquam eveniet laudantium\\nomnis quibusdam delectus saepe quia accusamus maiores nam est\\ncum et ducimus et vero voluptates excepturi deleniti ratione"),
            UserComment(name = "Fred", email = "fred@example.com", postId = 1, id = 3, body = "Hello, World!"),
            UserComment(name = "Peter Smith Johnson", email = "peterjohnson@example.com", postId = 1, id = 4, body = "harum non quasi et ratione\\ntempore iure ex voluptates in ratione\\nharum architecto fugit inventore cupiditate\\nvoluptates magni quo et"),
            UserComment(name = "Mark", email = "mark@example.com", postId = 1, id = 5, body = "Hello, World!"),
            UserComment(name = "Diego", email = "diego@example.com", postId = 1, id = 6, body = "This is a test."),
            UserComment(name = "Chris", email = "chris@example.com", postId = 1, id = 7, body = "Hello, World!"),
            UserComment(name = "Mary", email = "mary@example.com", postId = 1, id = 8, body = "This is a test."),
            UserComment(name = "Robert", email = "robert@example.com", postId = 1, id = 9, body = "Hello, World!"),
            UserComment(name = "Juan", email = "juan@example.com", postId = 1, id = 10, body = "This is a test."),
            UserComment(name = "Albert", email = "albert@example.com", postId = 1, id = 11, body = "Hello, World!"),
            UserComment(name = "Henry", email = "henry@example.com", postId = 1, id = 12, body = "This is a test."),
            UserComment(name = "Nikolas", email = "nikolas@example.com", postId = 1, id = 13, body = "Hello, World!"),
            UserComment(name = "Sam", email = "sam@example.com", postId = 1, id = 14, body = "This is a test."),
            UserComment(name = "Tim", email = "tim@example.com", postId = 1, id = 15, body = "This is a test."),
            UserComment(name = "James", email = "james@example.com", postId = 1, id = 16, body = "Hello, World!"),
            UserComment(name = "William", email = "william@example.com", postId = 1, id = 17, body = "This is a test."),
            UserComment(name = "Oliver", email = "oliver@example.com", postId = 1, id = 18, body = "Hello, World!"),
            UserComment(name = "Emma", email = "emma@example.com", postId = 1, id = 19, body = "This is a test."),
            UserComment(name = "Alice", email = "alice@example.com", postId = 1, id = 20, body = "Loving this app so far."),
            UserComment(name = "Bob", email = "bob@example.com", postId = 1, id = 21, body = "I have some feedback on this feature."),
            UserComment(name = "Charlie", email = "charlie@example.com", postId = 1, id = 22, body = "Can’t wait for the next update."),
            UserComment(name = "David", email = "david@example.com", postId = 1, id = 23, body = "Great work on the UI!"),
            UserComment(name = "Eve", email = "eve@example.com", postId = 1, id = 24, body = "I encountered a bug here."),
            UserComment(name = "Grace", email = "grace@example.com", postId = 1, id = 25, body = "Can you fix the layout on smaller screens?"),
            UserComment(name = "Hannah", email = "hannah@example.com", postId = 1, id = 26, body = "Really enjoying the content."),
            UserComment(name = "Isaac", email = "isaac@example.com", postId = 1, id = 27, body = "Please improve performance in this area."),
        )

        val mockRepository = MockUserCommentsRepository(mockUserComments)
        val mockViewModel = UserCommentsViewModel(mockRepository)
        mockViewModel.fetchUserComments()
        UserCommentsScreen(viewModel = mockViewModel)
    }
}

class MockUserCommentsRepository(private val comments: List<UserComment>) : IUserCommentsRepository {
    override suspend fun getUserComments():Flow<List<UserComment>> {
        return flowOf(comments)
    }
}




