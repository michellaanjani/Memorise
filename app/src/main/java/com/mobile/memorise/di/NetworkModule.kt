package com.mobile.memorise.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mobile.memorise.data.remote.AuthApi
import com.mobile.memorise.data.remote.AuthInterceptor
import com.mobile.memorise.data.remote.TokenAuthenticator
import com.mobile.memorise.data.remote.api.* // Pastikan UserApi / AuthApi ada di package ini
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // URL Backend
    private const val BASE_URL = "https://memorise-backend-production.up.railway.app/api/"

    // 1. GSON
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .create()
    }

    // 2. OkHttpClient
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // 3. Retrofit
    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // =================================================================
    // API SERVICES
    // =================================================================

    // AuthApi biasanya tetap terpisah karena digunakan di TokenAuthenticator
    // (Pastikan file AuthApi.kt masih ada)
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    // UserApi (Profil, dll) - Jika belum digabung ke ApiService, biarkan ini.
    // Jika UserApi juga sudah masuk ApiService, hapus ini.
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    // === PERBAIKAN DI SINI ===
    // Cukup sediakan ApiService, karena Folder, Deck, Card, Quiz, AI, File
    // semuanya ada di dalam interface ApiService ini.

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    // HAPUS provideDeckApi, provideCardApi, provideQuizApi
    // Karena interface-nya sudah dihapus/digabung.
}