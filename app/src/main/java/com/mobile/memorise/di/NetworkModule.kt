package com.mobile.memorise.di

import com.mobile.memorise.data.remote.api.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory // Sesuaikan dengan converter Anda (Moshi/Gson)
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 1. Ganti dengan Base URL API Anda yang sebenarnya
    private const val BASE_URL = "https://memorise-backend-production.up.railway.app/api/"

//    @Provides
//    @Singleton
//    fun provideRetrofit(): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create()) // Pastikan library converter sudah ada di gradle
//            .build()
//    }

    // 2. INI YANG MENYELESAIKAN ERROR ANDA
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }
}