package com.ds.utils

import retrofit2.Retrofit

object APICall {
    val service = Retrofit.Builder()
            .baseUrl("http://jztapi.zcym1688.com/app")
            .build().create(APIService::class.java)
}