package com.mobile.memorise.domain.repository

import com.mobile.memorise.ui.screen.home.HomeData // Menggunakan model UI kamu
import com.mobile.memorise.util.Resource
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getHomeData(): Flow<Resource<HomeData>>
}