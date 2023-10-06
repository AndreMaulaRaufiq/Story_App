package com.dicoding.submissionawalintermediate.data.remote.response

import com.google.gson.annotations.SerializedName

class UploadResponse(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null,
)