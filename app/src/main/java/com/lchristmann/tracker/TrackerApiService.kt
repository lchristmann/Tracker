package com.lchristmann.tracker

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TrackerApiService {

    @Headers("x-api-key: ${BuildConfig.API_KEY}")
    @POST("location")
    suspend fun uploadLocation(@Body request: LocationUploadRequest): retrofit2.Response<Unit>

}