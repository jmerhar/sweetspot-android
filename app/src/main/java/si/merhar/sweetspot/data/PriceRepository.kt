package si.merhar.sweetspot.data

import si.merhar.sweetspot.model.HourlyPrice
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class PriceRepository(private val cache: PriceCache, private val zoneId: ZoneId) {

    fun getPrices(): List<HourlyPrice> {
        val today = LocalDate.now(zoneId)
        val allPrices: List<HourlyPrice>

        if (cache.isFresh(today)) {
            val cachedJson = cache.readCachedJson()
            if (cachedJson != null) {
                allPrices = EnergyZeroApi.parseJson(cachedJson, zoneId)
            } else {
                allPrices = fetchAndCache(today)
            }
        } else {
            allPrices = fetchAndCache(today)
        }

        // Filter to next 24h from now
        val now = ZonedDateTime.now(zoneId)
        val end = now.plusHours(24)
        return allPrices.filter { it.time >= now.minusMinutes(30) && it.time < end }
    }

    private fun fetchAndCache(today: LocalDate): List<HourlyPrice> {
        val rawJson = EnergyZeroApi.fetchRawJson(zoneId)
        cache.write(rawJson, today)
        return EnergyZeroApi.parseJson(rawJson, zoneId)
    }
}
