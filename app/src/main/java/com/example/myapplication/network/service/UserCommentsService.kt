package com.example.myapplication.network.service

import com.example.myapplication.model.UserComment
import com.example.myapplication.network.api.ApiEndpoints
import retrofit2.http.GET

interface UserCommentsService {
    @GET(ApiEndpoints.GET_COMMENTS)
    suspend fun getComments(): List<UserComment>
}