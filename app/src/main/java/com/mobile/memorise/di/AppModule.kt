// com/mobile/memorise/di/AppModule.kt (Cleaned Up)
package com.mobile.memorise.di

import com.mobile.memorise.data.repository.AuthRepositoryImpl
import com.mobile.memorise.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // This is the ONLY item that should remain if you keep the AuthRepository 
    // binding here.
    @Provides
    @Singleton
    fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository = impl
}

// NOTE: Ensure your AuthInterceptor, TokenStore, and TokenAuthenticator 
// bindings (if any) are correctly placed, likely in NetworkModule or another DI file.