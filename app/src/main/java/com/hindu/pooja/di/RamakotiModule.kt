package com.hindu.pooja.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hindu.pooja.feature.ramakoti.data.RamakotiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Ramakoti-specific bindings.
 * NOTE: FirebaseAuth / FirebaseFirestore singletons are provided by FirebaseModule.kt.
 */
@Module
@InstallIn(SingletonComponent::class)
object RamakotiModule {

    @Provides
    @Singleton
    fun provideRamakotiRepository(
        auth: FirebaseAuth,
        db: FirebaseFirestore,            // <-- use the same name as the repository ctor
    ): RamakotiRepository {
        return RamakotiRepository(auth = auth, db = db)
    }
}