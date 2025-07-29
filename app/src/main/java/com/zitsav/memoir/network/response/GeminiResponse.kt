package com.zitsav.memoir.network.response

data class GeminiResponse(
    val candidates: List<Candidate>?,
    val error: ApiError?
)

data class Candidate(
    val content: ContentDetails
)

data class ContentDetails(
    val parts: List<PartDetail>
)

data class PartDetail(
    val text: String
)

data class ApiError(
    val code: Int,
    val message: String,
    val status: String
)