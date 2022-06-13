package com.igi.office.di

import android.content.Context
import app.base.demo.com.mybasekotlin.api.TLSSocketFactory
import app.base.demo.com.mybasekotlin.api.ToStringConverterFactory
import com.google.gson.GsonBuilder
import com.igi.office.BuildConfig
import com.igi.office.common.Logger
import com.igi.office.common.SharePreferenceUtils
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException


/**
 * Created by farshid.abazari since 12/27/18
 *
 * Usage: functions to create network requirements such as okHttpClient
 *
 * How to call: just call [createNetworkClient] in AppInjector
 *
 */


fun createNetworkClient(context: Context, baseUrl: String, sharePref: SharePreferenceUtils) =
    retrofitClient(baseUrl, okHttpClient(context, sharePref, true))

private fun okHttpClient(
    context: Context,
    sharePref: SharePreferenceUtils,
    addAuthHeader: Boolean
): OkHttpClient {
    val builder = OkHttpClient.Builder()
        .addInterceptor(headersInterceptor(sharePref, addAuthHeader))
        .addInterceptor(
            LoggingInterceptor.Builder()
                .loggable(BuildConfig.DEBUG)
                .tag("LoggingI")
                .setLevel(Level.BASIC)
                .log(Platform.INFO)
                .request("Request")
                .response("Response").build()
        )
        .apply { setTimeOutToOkHttpClient(this) }

    try {
        val tlsSocketFactory = TLSSocketFactory()
        builder.sslSocketFactory(tlsSocketFactory, tlsSocketFactory.systemDefaultTrustManager())
    } catch (e: KeyManagementException) {
        Logger.showLog("Failed to create Socket connection " + e)
    } catch (e: NoSuchAlgorithmException) {
        Logger.showLog("Failed to create Socket connection " + e)
    }
    return builder.build()
}


private fun retrofitClient(baseUrl: String, httpClient: OkHttpClient): Retrofit {
    val gson = GsonBuilder()
        .setLenient()
        .create()

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addConverterFactory(ToStringConverterFactory())
        .build()
}


fun headersInterceptor(sharePref: SharePreferenceUtils, addAuthHeader: Boolean) =
    Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .also {
                    if (addAuthHeader) {
//                        it.addHeader("device-id", sharePref.getDeviceId())
                    }
                }
                .build()
        )
    }

private fun setTimeOutToOkHttpClient(okHttpClientBuilder: OkHttpClient.Builder) =
    okHttpClientBuilder.apply {
        readTimeout(30L, TimeUnit.SECONDS)
        connectTimeout(30L, TimeUnit.SECONDS)
        writeTimeout(30L, TimeUnit.SECONDS)
    }