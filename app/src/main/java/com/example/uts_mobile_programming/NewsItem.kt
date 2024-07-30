package com.example.uts_mobile_programming

data class NewsItem(val title: String, val content: String, val date: String, val time: String, val imageUrl: String? = null)

fun NewsItem.toMap(): Map<String, Any?> {
    return mapOf(
        "title" to title,
        "content" to content,
    )
}
