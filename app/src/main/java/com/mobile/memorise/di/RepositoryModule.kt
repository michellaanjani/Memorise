package com.mobile.memorise.di

import com.mobile.memorise.data.repository.AuthRepositoryImpl
import com.mobile.memorise.data.repository.ContentRepositoryImpl
import com.mobile.memorise.data.repository.UserRepositoryImpl
import com.mobile.memorise.domain.repository.AuthRepository
import com.mobile.memorise.domain.repository.ContentRepository
import com.mobile.memorise.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideContentRepository(impl: ContentRepositoryImpl): ContentRepository = impl

    @Provides
    @Singleton
    fun provideUserRepository(impl: UserRepositoryImpl): UserRepository = impl
}