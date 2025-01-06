package com.github.saintedlittle

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object ConfigManager {

    private lateinit var config: JsonObject

    fun init(file: File) {
        FileReader(file).use { reader ->
            config = JsonParser.parseReader(reader).asJsonObject
        }
    }

    fun get(key: String): String? {
        return config.get(key)?.asString
    }

    fun saveDefaultConfig(file: File) {
        val defaultConfig = JsonObject().apply {
            addProperty("minDistanceBetweenMovements", 2.0)
            addProperty("someOtherConfig", "defaultValue")
        }
        FileWriter(file).use { writer ->
            writer.write(defaultConfig.toString())
        }
    }

    val minDistanceBetweenMovements: Double
        get() = get("minDistanceBetweenMovements")?.toDoubleOrNull() ?: 2.0
}
