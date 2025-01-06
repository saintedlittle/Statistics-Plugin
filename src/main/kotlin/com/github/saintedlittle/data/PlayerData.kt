package com.github.saintedlittle.data

import com.github.saintedlittle.domain.MovementPoint
import com.github.saintedlittle.extensions.toJsonArray
import com.github.saintedlittle.extensions.toJsonObjectOwn
import com.github.saintedlittle.extensions.toMovementsJson
import com.google.gson.JsonObject

data class PlayerData(
    val inventory: List<ItemData>,
    val armor: Map<String, Any>,
    val statistics: Map<String, Int>,
    val attributes: Map<String, Double>,
    val potionEffects: List<PotionEffectData>,
    val location: LocationData,
    val totalTime: Long,
    val beds: List<LocationData>,
    val movements: Map<String, List<MovementPoint>>,
    @Transient val internalReference: Any? = null
) {
    fun toJson(): JsonObject {
        return JsonObject().apply {
            add("inventory", inventory.toJsonArray { it.toJson() })
            add("armor", armor.toJsonObjectOwn())
            add("statistics", statistics.toJsonObjectOwn())
            add("attributes", attributes.toJsonObjectOwn())
            add("potionEffects", potionEffects.toJsonArray { it.toJson() })
            add("location", location.toJson())
            addProperty("totalTime", totalTime)
            add("beds", beds.toJsonArray { it.toJson() })
            add("movements", movements.toMovementsJson())
        }
    }

}