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
    GET_METADATA("get_metadata"),
    GET_INVENTORY("get_inventory"),
    GET_ARMOR("get_armor"),
    GET_STATISTICS("get_statistics"),
    GET_ATTRIBUTES("get_attributes"),
    GET_POTION_EFFECTS("get_potion_effects"),
    GET_LOCATION("get_location"),
    GET_TOTAL_TIME("get_total_time"),
    GET_LEVEL("get_level"),
    GET_TOTAL_EXP("get_total_exp"),
    GET_CURRENT_EXP("get_current_exp"),
    GET_BLOCK_INTERACTIONS("get_block_interactions"),
    GET_BEDS("get_beds"),
    GET_MOVEMENTS("get_movements"),
    GET_FULL_DATA("get_full_data"),
    UNKNOWN("unknown");

    companion object {
        fun of(id: String?): Payload? = entries.firstOrNull { it.id.equals(id, true) }
    }
}