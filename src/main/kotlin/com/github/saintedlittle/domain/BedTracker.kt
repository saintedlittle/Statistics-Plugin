package com.github.saintedlittle.domain

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.Location
import org.bukkit.entity.Player
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.MemoryUnit
import java.io.File
import java.util.*

class BedTracker(pluginFolder: String) {

    private val gson = Gson()

    private val cacheManager: CacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(CacheManagerBuilder.persistence(File(pluginFolder, "ehcache_beds")))
        .withCache(
            "playerBeds",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                UUID::class.java,
                String::class.java,
                ResourcePoolsBuilder.heap(7000).disk(5, MemoryUnit.GB, true)
            )
        )
        .build(true)

    private val playerBeds: Cache<UUID, String> = cacheManager.getCache("playerBeds", UUID::class.java, String::class.java)

    fun addBed(player: Player, location: Location) {
        val playerId = player.uniqueId
        val beds = getBeds(player).toMutableList()
        beds.add(location)
        playerBeds.put(playerId, gson.toJson(beds))
    }

    fun getBeds(player: Player): List<Location> {
        val playerId = player.uniqueId
        val bedsJson = playerBeds.get(playerId) ?: "[]"
        val type = object : TypeToken<List<Location>>() {}.type
        return gson.fromJson(bedsJson, type) ?: emptyList()
    }

    fun clearBeds(player: Player) {
        playerBeds.remove(player.uniqueId)
    }

    fun close() {
        cacheManager.close()
    }
}
