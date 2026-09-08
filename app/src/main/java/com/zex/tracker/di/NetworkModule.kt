package com.zex.tracker.di

import com.zex.tracker.BuildConfig
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.data.remote.api.ZexApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(securePrefs: SecurePrefs): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
            
            val ownerToken = securePrefs.getString(ZexConstants.KEY_OWNER_TOKEN)
            if (!ownerToken.isNullOrEmpty()) {
                builder.header(ZexConstants.HEADER_AUTHORIZATION, "Bearer $ownerToken")
            }
            
            val deviceToken = securePrefs.getString(ZexConstants.KEY_DEVICE_TOKEN)
            if (!deviceToken.isNullOrEmpty()) {
                builder.header(ZexConstants.HEADER_DEVICE_TOKEN, deviceToken)
            }
            
            builder.header("Accept", "application/json")
            chain.proceed(builder.build())
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor { message ->
            ZexLogger.d("OkHttp", message)
        }.apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            redactHeader(ZexConstants.HEADER_AUTHORIZATION)
            redactHeader(ZexConstants.HEADER_DEVICE_TOKEN)
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideZexApi(retrofit: Retrofit): ZexApi {
        return retrofit.create(ZexApi::class.java)
    }
}
