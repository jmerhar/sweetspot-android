package si.merhar.sweetspot.model

import kotlinx.serialization.Serializable

@Serializable
data class Appliance(
    val id: String,
    val name: String,
    val duration: String,
    val icon: String = "bolt"
)
