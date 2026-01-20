package com.example.hikermanagementapp.di

import android.content.Context
import com.example.hikermanagementapp.data.AppDatabase
import com.example.hikermanagementapp.repo.HikeRepository
import com.example.hikermanagementapp.repo.ObservationRepository

object ServiceLocator {
    private fun db(context: Context) = AppDatabase.getInstance(context)

    fun provideHikeRepository(context: Context) = HikeRepository(db(context).hikeDao())
    fun provideObservationRepository(context: Context) = ObservationRepository(db(context).observationDao())
}

