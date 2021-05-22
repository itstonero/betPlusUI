package com.example.betplus.services

import com.example.betplus.models.*
import retrofit2.Call
import retrofit2.http.*


public interface BetPlusAPI {
        @POST("auth/signin")
        fun signIn(@Body user: AuthRequest?): Call<AuthResponse?>?

        @GET("fixtures/{userId}/all")
        fun getAvailableFixture(@Path("userId") userId:String) : Call<List<Fixture?>?>?

        @GET("fixtures/{userId}/today")
        fun getRegisteredFixtures(@Path("userId") userId:String) : Call<List<Fixture?>?>?

        @POST("fixtures/{userId}/create")
        fun registerFixtures(@Path("userId") userId:String, @Body fixtures: List<Fixture>) : Call<List<Fixture?>?>?

        @POST("fixtures/{userId}/update")
        fun updateFixtures(@Path("userId") userId:String, @Body fixtures: List<Fixture>) : Call<List<Fixture?>?>?

        //@DELETE("fixtures/{userId}/modify")
        @HTTP(method = "DELETE", path = "fixtures/{userId}/modify", hasBody = true)
        fun deleteFixture(@Path("userId") userId:String, @Body fixture:Fixture) : Call<List<Fixture?>?>?

        @PUT("fixtures/{userId}/modify")
        fun updateFixture(@Path("userId") userId:String, @Body fixture:Fixture) : Call<List<Fixture?>?>?

        @POST("slips/{userId}/create")
        fun createSlip(@Path("userId") userId:String, @Body request: SlipRequest) : Call<SlipResponse?>?

        @POST("slips/{userId}/update")
        fun updateSlip(@Path("userId") userId:String) : Call<SlipResponse?>?

        @POST("slips/{userId}/upgrade")
        fun upgradeSlip(@Path("userId") userId:String, @Body request: SlipUpgradeRequest) : Call<SlipResponse?>?

        @POST("slips/{userId}/reverse")
        fun reverseSlip(@Path("userId") userId:String) : Call<SlipResponse?>?

        @GET("slips/{userId}")
        fun retrieveSlip(@Path("userId") userId:String) : Call<SlipResponse?>?


        companion object
        {
                val BASE_URL = "https://backend-betplus.herokuapp.com/"
                var SITE_USERNAME = ""
                var SITE_TOKEN = ""
        }
}