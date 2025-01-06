package com.github.saintedlittle.data

import com.google.gson.JsonObject

@JvmInline
value class ItemData(
    private val data: Map<String, Any?>
) {
    fun toJson(): JsonObject {
        return JsonObject().apply {
            data.forEach { (key, value) ->
                when (value) {
                    is String -> addProperty(key, value)
                    is Int -> addProperty(key, value)
                    is Double -> addProperty(key, value)
                    is Boolean -> addProperty(key, value)
                    is Long -> addProperty(key, value)
                    null -> addProperty(key, "null") // Обработка null
                    else -> addProperty(key, value.toString())
                }
            }
        }
    }

    companion object {
        fun from(
            type: String,
            minecraftId: String,
            amount: Int,
            displayName: String? = null
        ): ItemData {
            return ItemData(
                mapOf(
                    "type" to type,
                    "minecraft_id" to minecraftId,
                    "amount" to amount,
                    "displayName" to (displayName ?: "unknown")
                )
            )
        }
    }
}
