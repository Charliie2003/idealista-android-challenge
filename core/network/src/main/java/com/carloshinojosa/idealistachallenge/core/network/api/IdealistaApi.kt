package com.carloshinojosa.idealistachallenge.core.network.api

import com.carloshinojosa.idealistachallenge.core.network.dto.PropertyDetailDto
import com.carloshinojosa.idealistachallenge.core.network.dto.PropertyDto
import retrofit2.http.GET

interface IdealistaApi {
    @GET("list.json")
    suspend fun getProperties(): List<PropertyDto>

    @GET("detail.json")
    suspend fun getDetail(): PropertyDetailDto
}
