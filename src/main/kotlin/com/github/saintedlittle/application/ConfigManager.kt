package com.github.saintedlittle.application

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

class ConfigManager(dataFolder: File) {
    private var config: JsonObject

    init {
        val configFile = File(dataFolder, "config.json")
        if (!configFile.exists()) saveDefaultConfig(configFile)
        config = JsonParser.parseReader(configFile.reader()).asJsonObject
    }

    fun get(key: String): String? = config.get(key)?.asString

    private fun saveDefaultConfig(file: File) {
        val defaultConfig = JsonObject().apply {
            addProperty("minDistanceBetweenMovements", 2.0)
        }
        file.writeText(defaultConfig.toString())
    }
}
