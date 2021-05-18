package com.example.betplus.services

import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


public class Repo {

    companion object{
        var client = OkHttpClient.Builder().addInterceptor { chain ->
            val newRequest: Request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BetPlusAPI.SITE_TOKEN}")
                .build()
            chain.proceed(newRequest)
        }.build()

        val retrofit: Retrofit = Retrofit.Builder().client(client).baseUrl(BetPlusAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build();
        val betPlusAPI:BetPlusAPI =  retrofit.create(BetPlusAPI::class.java)

        val NOTIFICATION_ID = "NOTIFICATION_ID"
    }
}