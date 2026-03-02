package si.merhar.sweetspot.model

import kotlinx.serialization.Serializable

@Serializable
data class Appliance(
    val id: String,
    val name: String,
    val durationHours: Int = 1,
    val durationMinutes: Int = 0,
    val icon: String = "bolt"
)
