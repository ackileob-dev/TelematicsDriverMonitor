package com.ackileo.telematics.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    /**
     * Use http://10.0.2.2:5000/ if you are running a local backend
     * on your computer via the Android Emulator.
     *
     * If you are using a real server, replace this with:
     * "https://your-actual-api-domain.com/"
     */
    private const val BASE_URL = "http://10.0.2.2:5000/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}