package com.sanathcoding.workmanagerguide.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET

interface FileApi {

    @GET("/u/11756630?v=4")
    suspend fun downloadImage(): Response<ResponseBody>


    companion object {
        val instance by lazy {
            Retrofit.Builder()
                .baseUrl("https://avatars.githubusercontent.com")
                .build()
                .create(FileApi::class.java)
        }
    }

}