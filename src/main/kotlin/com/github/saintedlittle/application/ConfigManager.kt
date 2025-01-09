package com.github.saintedlittle.application

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

class ConfigManager(private val dataFolder: File) {
    val config: YamlConfiguration

    val kafkaProducerConfig: Properties
        get() = getKafkaConfig("kafka-producer-config.json")

    init {
        config = loadOrCreate("config.yml")
    }

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

    private fun loadOrCreate(fileName: String): YamlConfiguration {
        val file = File(dataFolder, fileName)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            this::class.java.getResourceAsStream("/$fileName")?.use {
                file.outputStream().use { out -> it.copyTo(out) }
            }
        }
        return YamlConfiguration.loadConfiguration(file)
    }

    fun reload() {
        config.load(File(dataFolder, "config.yml"))
    }
}
