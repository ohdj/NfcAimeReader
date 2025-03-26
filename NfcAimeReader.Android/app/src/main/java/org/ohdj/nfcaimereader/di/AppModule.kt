package org.ohdj.nfcaimereader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ohdj.nfcaimereader.data.datastore.WebSocketPreferences
import org.ohdj.nfcaimereader.data.repository.WebSocketRepository
import org.ohdj.nfcaimereader.data.websocket.WebSocketClient
import org.ohdj.nfcaimereader.utils.NetworkScanner
import org.ohdj.nfcaimereader.utils.NfcManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideWebSocketPreferences(@ApplicationContext context: Context): WebSocketPreferences {
        return WebSocketPreferences(context)
    }

    @Singleton
    @Provides
    fun provideWebSocketClient(): WebSocketClient {
        return WebSocketClient()
    }

    @Singleton
    @Provides
    fun provideNetworkScanner(): NetworkScanner {
        return NetworkScanner()
    }

    @Singleton
    @Provides
    fun provideWebSocketRepository(
        preferences: WebSocketPreferences,
        webSocketClient: WebSocketClient,
        networkScanner: NetworkScanner
    ): WebSocketRepository {
        return WebSocketRepository(preferences, webSocketClient, networkScanner)
    }

    @Singleton
    @Provides
    fun provideNfcManager(@ApplicationContext context: Context): NfcManager {
        return NfcManager(context)
    }
}