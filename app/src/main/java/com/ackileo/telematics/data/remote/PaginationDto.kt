package com.ackileo.telematics.data.remote

import com.google.gson.annotations.SerializedName

data class PaginationDto(
    @SerializedName("page")
    val page: Int? = null,

    @SerializedName("limit")
    val limit: Int? = null,

    @SerializedName("total")
    val total: Int? = null,

    @SerializedName("totalPages")
    val totalPages: Int? = null,

    @SerializedName("hasNextPage")
    val hasNextPage: Boolean? = null,

    @SerializedName("hasPrevPage")
    val hasPrevPage: Boolean? = null
)
