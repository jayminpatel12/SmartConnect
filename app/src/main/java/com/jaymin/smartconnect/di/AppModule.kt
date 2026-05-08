package com.jaymin.smartconnect.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.jaymin.smartconnect.core.common.util.Constants
import com.jaymin.smartconnect.core.data.bluetooth.BluetoothController
import com.jaymin.smartconnect.core.data.local.dao.TransferDao
import com.jaymin.smartconnect.core.data.local.database.SmartConnectDatabase
import com.jaymin.smartconnect.core.data.nfc.NfcController
import com.jaymin.smartconnect.core.data.repository.TransferRepositoryImpl
import com.jaymin.smartconnect.core.domain.repository.BluetoothRepository
import com.jaymin.smartconnect.core.domain.repository.NfcRepository
import com.jaymin.smartconnect.core.domain.repository.TransferRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SmartConnectDatabase =
        Room.databaseBuilder(context, SmartConnectDatabase::class.java, Constants.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTransferDao(db: SmartConnectDatabase): TransferDao = db.transferDao()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideBluetoothRepository(controller: BluetoothController): BluetoothRepository = controller

    @Provides
    @Singleton
    fun provideNfcRepository(controller: NfcController): NfcRepository = controller

    @Provides
    @Singleton
    fun provideTransferRepository(
        dao: TransferDao,
        firestore: FirebaseFirestore
    ): TransferRepository = TransferRepositoryImpl(dao, firestore)
}
