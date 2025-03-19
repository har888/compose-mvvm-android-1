package com.example.myapplication.repository

import com.example.myapplication.model.UserComment
import kotlinx.coroutines.flow.Flow

interface IUserCommentsRepository {
    suspend fun getUserComments(): Flow<List<UserComment>>
}