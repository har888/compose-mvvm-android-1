package com.example.myapplication.repository

import com.example.myapplication.model.UserComment
import com.example.myapplication.network.service.UserCommentsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UserCommentsRepository @Inject constructor(
    private val userCommentsService: UserCommentsService
): IUserCommentsRepository {
    override suspend fun getUserComments(): Flow<List<UserComment>> = flow {
        emit(userCommentsService.getComments())
    }.flowOn(Dispatchers.IO)
}