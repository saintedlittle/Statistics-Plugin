package com.github.saintedlittle.extensions

import com.github.saintedlittle.data.PotionEffectData
import com.github.saintedlittle.domain.MovementPoint
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.bukkit.Statistic
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect

// Сбор статистики игрока
fun Player.collectStatistics(): Map<String, Int> {
    return Statistic.entries.mapNotNull { statistic ->
        try {
            statistic.name to getStatistic(statistic)
        } catch (e: IllegalArgumentException) {
            null
        }
    }.toMap()
}

// Сбор атрибутов игрока
fun Player.collectAttributes(): Map<String, Double> {
    return Attribute.entries.mapNotNull { attribute ->
        getAttribute(attribute)?.value?.let { attribute.name to it }
    }.toMap()
}

// Сбор эффектов зелий игрока
fun Player.collectPotionEffects(): List<PotionEffectData> {
    return activePotionEffects.map { it.toPotionEffectData() }
}

// Преобразование зелья в Data-класс
fun PotionEffect.toPotionEffectData(): PotionEffectData {
    return PotionEffectData(
        type = type.name,
        amplifier = amplifier,
        duration = duration
    )
}

fun <T> List<T>.toJsonArray(transform: (T) -> JsonElement): JsonArray {
    return JsonArray().apply {
        this@toJsonArray.forEach { element ->
            add(transform(element)) // transform должен возвращать JsonElement
        }
    }
}

// Преобразование Map в JsonObject (с кастомной логикой значений)
fun Map<String, List<MovementPoint>>.toMovementsJson(): JsonObject {
    return JsonObject().apply {
        forEach { (world, points) ->
            val jsonArray = JsonArray()
            points.forEach { point ->
                jsonArray.add(JsonParser.parseString(point.toJsonObject())) // Преобразуем строку в JsonElement
            }
            add(world, jsonArray)
        }
    }
}

// Расширение для преобразования Map<String, Any> в JsonObject
fun Map<String, Any>.toJsonObjectOwn(): JsonObject {
    return JsonObject().apply {
        forEach { (key, value) ->
            when (value) {
                is Number -> addProperty(key, value)
                is String -> addProperty(key, value)
                is Boolean -> addProperty(key, value)
                else -> addProperty(key, value.toString()) // Обработка остальных типов
            }
        }
    }
}