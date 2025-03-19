package com.example.myapplication.di

import com.example.myapplication.repository.IUserCommentsRepository
import com.example.myapplication.repository.UserCommentsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindUserCommentsRepository(
        userCommentsRepository: UserCommentsRepository
    ): IUserCommentsRepository
}