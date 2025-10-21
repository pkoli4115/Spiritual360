package com.hindu.pooja.di

import android.content.Context
import com.hindu.pooja.feature.ramakoti.prefs.LanguagePreferenceManager
import com.hindu.pooja.feature.ramakoti.data.RamakotiRepository
import com.hindu.pooja.feature.ramakoti.prefs.RamakotiPreferences
import com.hindu.pooja.feature.ramakoti.reminders.ReminderScheduler
import com.hindu.pooja.feature.ramakoti.data.CertificateRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RamakotiModule {

    // ⚠️ Do NOT provide FirebaseAuth/Firestore/Storage here — they already exist in FirebaseModule.
    // We just CONSUME them where needed (e.g., RamakotiRepository).

    /* ------------ Core repo (consumes provided FirebaseAuth/Firestore) ------------ */
    @Provides
    @Singleton
    fun provideRamakotiRepository(
        auth: FirebaseAuth,
        db: FirebaseFirestore,
    ): RamakotiRepository = RamakotiRepository(auth = auth, db = db)

    /* ------------ Preferences & managers ------------ */
    @Provides
    @Singleton
    fun provideRamakotiPreferences(
        @ApplicationContext appContext: Context
    ): RamakotiPreferences = RamakotiPreferences.getInstance(appContext)

    @Provides
    @Singleton
    fun provideLanguagePreferenceManager(
        @ApplicationContext appContext: Context
    ): LanguagePreferenceManager = LanguagePreferenceManager.getInstance(appContext)

    @Provides
    @Singleton
    fun provideReminderScheduler(
        @ApplicationContext appContext: Context
    ): ReminderScheduler = ReminderScheduler(appContext)

    /* ------------ Certificate generation ------------ */
    @Provides
    @Singleton
    fun provideCertificateRepository(): CertificateRepository = CertificateRepository()
}
