package com.zitsav.memoir.data.entry

data class Entry(
    val id: Long = 0,
    val title: String?,
    val text: String,
    val date: Long,
    val mood: Int?,
    val attachment: String?
)