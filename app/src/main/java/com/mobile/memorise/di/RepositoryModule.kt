package com.mobile.memorise.di

import com.mobile.memorise.data.repository.UserRepositoryImpl // <-- Ganti dengan lokasi implementasi Anda yang sebenarnya
import com.mobile.memorise.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

}
    