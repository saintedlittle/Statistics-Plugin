package com.github.saintedlittle.messaging.data

import kotlinx.serialization.Serializable

@Serializable
data class PayloadResponse(
    val payload: String,
    val playerId: String,
    val key: String,
    val data: Map<String, String>,
    val status: String
)

enum class ResponseStatus(val id: String) {
    SUCCESS("success"),
    NOT_FOUND("not_found"),
    FAILED("error")
}