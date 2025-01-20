package com.github.saintedlittle.application

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

class ConfigManager(private val dataFolder: File) {
    val config: YamlConfiguration

    val kafkaProducerConfig: Properties
        get() = getJSONConfig("kafka-producer-config.json")

    val kafkaConsumerConfig: Properties
        get() = getJSONConfig("kafka-consumer-config.json")

    val placeholderConfig: Properties
        get() = getJSONConfig("placeholder-config.json")

    init {
        config = loadOrCreate("config.yml")
    }

    private fun getJSONConfig(fileName: String): Properties {
        val file = ensureFileExists(fileName)
        val configMap: Map<String, String> = JsonUtil.fromJson(file.readText())

        val props = Properties()
        configMap.forEach { (key, value) ->
            props[key] = value
        }
        return props
    }

    private fun loadOrCreate(fileName: String): YamlConfiguration {
        val file = ensureFileExists(fileName)

        // Загружаем файл YAML
        return YamlConfiguration.loadConfiguration(file)
    }

    private fun ensureFileExists(fileName: String): File {
        val file = File(dataFolder, fileName)

        if (!file.exists()) {
            file.parentFile.mkdirs()

            val resource = this::class.java.classLoader.getResource(fileName)
                ?: throw IllegalArgumentException("Resource $fileName not found")

            resource.openStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return file
    }

    fun reload() {
        config.load(File(dataFolder, "config.yml"))
    }
}
