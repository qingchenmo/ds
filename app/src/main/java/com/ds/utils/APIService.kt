package com.ds.utils

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


interface APIService {
    @GET("users/{user}/repos")
    fun parking(@Path("dev_sn") dev_sn: String,@Path("plate_number") plate_number:String): Call<String>
}