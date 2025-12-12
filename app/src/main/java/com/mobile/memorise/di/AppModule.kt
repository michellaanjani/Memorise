package com.mobile.memorise.di

import com.mobile.memorise.data.local.token.TokenStore
import com.mobile.memorise.data.remote.AuthApi
import com.mobile.memorise.data.remote.AuthInterceptor
import com.mobile.memorise.data.remote.api.FolderApi
import com.mobile.memorise.data.remote.api.DeckApi
import com.mobile.memorise.data.remote.api.CardApi
import com.mobile.memorise.data.remote.api.QuizApi
import com.mobile.memorise.data.remote.api.HomeApi
import com.mobile.memorise.data.repository.AuthRepositoryImpl
import com.mobile.memorise.data.repository.HomeRepositoryImpl
import com.mobile.memorise.domain.repository.AuthRepository
import com.mobile.memorise.domain.repository.HomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .addInterceptor(authInterceptor)
            .build()
    }

    // INI ADALAH SATU-SATUNYA PENYEDIA RETROFIT
    // Pastikan fungsi serupa di NetworkModule SUDAH DIHAPUS
    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://memorise-backend-production.up.railway.app/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHomeApi(retrofit: Retrofit): HomeApi {
        return retrofit.create(HomeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDeckApi(retrofit: Retrofit): DeckApi {
        return retrofit.create(DeckApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCardApi(retrofit: Retrofit): CardApi {
        return retrofit.create(CardApi::class.java)
    }

    @Provides
    @Singleton
    fun provideQuizApi(retrofit: Retrofit): QuizApi {
        return retrofit.create(QuizApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository = impl

    @Provides
    @Singleton
    fun provideHomeRepository(
        api: HomeApi,
        tokenStore: TokenStore
    ): HomeRepository {
        return HomeRepositoryImpl(api, tokenStore)
    }

    @Provides
    @Singleton
    fun provideFolderApi(retrofit: Retrofit): FolderApi {
        return retrofit.create(FolderApi::class.java)
    }

}