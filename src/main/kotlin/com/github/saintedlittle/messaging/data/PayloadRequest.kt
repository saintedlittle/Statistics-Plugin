package com.github.saintedlittle.messaging.data

import java.util.UUID

data class PayloadRequest(
    val payload: Payload,
    val playerId: UUID,
    val key: String,
    val data: Map<String, String>,
)

enum class Payload(val id: String) {
    GET_USERNAME("get_username"),
    GET_FULL_DATA("get_full_data"),
    UNKNOWN("unknown");

    companion object {
        fun of(id: String?): Payload? = entries.firstOrNull { it.id.equals(id, true) }
    }
}