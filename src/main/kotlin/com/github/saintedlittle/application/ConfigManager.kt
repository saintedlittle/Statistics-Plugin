package com.github.saintedlittle.application

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.util.*

class ConfigManager(dataFolder: File) {
    private var config: JsonObject

    val kafkaProducerConfig: Properties
        get() = getKafkaConfig("kafka-producer-config.json")

    init {
        val configFile = File(dataFolder, "config.json")
        if (!configFile.exists()) saveDefaultConfig(configFile)
        config = JsonParser.parseReader(configFile.reader()).asJsonObject
    }

    fun get(key: String): String? = config.get(key)?.asString

    private fun getKafkaConfig(fileName: String): Properties {
        val resourcePath = this::class.java.classLoader.getResource(fileName)
            ?: throw IllegalArgumentException("Kafka configuration file not found")
        val file = File(resourcePath.toURI())

        val configMap: Map<String, String> = JsonUtil.fromJson(file.readText())

        val props = Properties()
        configMap.forEach { (key, value) ->
            props[key] = value
        }
        return props
    }

    private fun saveDefaultConfig(file: File) {
        val defaultConfig = JsonObject().apply {
            addProperty("minDistanceBetweenMovements", 2.0)
        }
        file.writeText(defaultConfig.toString())
    }
}
