package com.github.saintedlittle.data

import com.google.gson.JsonObject

data class PotionEffectData(
    val type: String,
    val amplifier: Int,
    val duration: Int
) {
    fun toJson(): JsonObject {
        return JsonObject().apply {
            addProperty("type", type)
            addProperty("amplifier", amplifier)
            addProperty("duration", duration)
        }
    }
}