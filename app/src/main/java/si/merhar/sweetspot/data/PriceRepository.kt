package si.merhar.sweetspot.data

import si.merhar.sweetspot.model.HourlyPrice
import si.merhar.sweetspot.util.AMSTERDAM
import java.time.LocalDate
import java.time.ZonedDateTime

class PriceRepository(private val cache: PriceCache) {

    fun getPrices(): List<HourlyPrice> {
        val todayAmsterdam = LocalDate.now(AMSTERDAM)
        val allPrices: List<HourlyPrice>

        if (cache.isFresh(todayAmsterdam)) {
            val cachedJson = cache.readCachedJson()
            if (cachedJson != null) {
                allPrices = EnergyZeroApi.parseJson(cachedJson)
            } else {
                allPrices = fetchAndCache(todayAmsterdam)
            }
        } else {
            allPrices = fetchAndCache(todayAmsterdam)
        }

        // Filter to next 24h from now
        val now = ZonedDateTime.now(AMSTERDAM)
        val end = now.plusHours(24)
        return allPrices.filter { it.time >= now.minusMinutes(30) && it.time < end }
    }

    private fun fetchAndCache(todayAmsterdam: LocalDate): List<HourlyPrice> {
        val rawJson = EnergyZeroApi.fetchRawJson()
        cache.write(rawJson, todayAmsterdam)
        return EnergyZeroApi.parseJson(rawJson)
    }
}
