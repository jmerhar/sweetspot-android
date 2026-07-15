package today.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import today.sweetspot.model.Appliance
import today.sweetspot.model.ApplianceGrouping
import today.sweetspot.model.ApplianceSort
import today.sweetspot.model.ApplianceUsage
import today.sweetspot.model.EvPosition
import today.sweetspot.model.EvSpec
import today.sweetspot.model.SortCriterion
import today.sweetspot.model.SortKey

/**
 * Tests for the pure appliance ordering, collision detection, usage combining, and home merge.
 */
class ApplianceSortingTest {

    private fun app(id: String, name: String = id, h: Int = 1, m: Int = 0, icon: String? = "device") =
        Appliance(id = id, name = name, durationHours = h, durationMinutes = m, icon = icon)

    private fun ev(id: String, name: String) =
        Appliance(id = id, name = name, durationHours = 0, durationMinutes = 0, icon = null, ev = EvSpec(60.0, 11.0))

    private fun sort(vararg c: SortCriterion) = ApplianceSort(c.toList())

    private val noUsage = emptyMap<String, ApplianceUsage>()

    private fun ids(list: List<Appliance>) = list.map { it.id }

    @Test
    fun `custom sort returns stored order unchanged`() {
        val list = listOf(app("c"), app("a"), app("b"))
        assertEquals(listOf("c", "a", "b"), ids(sortAppliances(list, sort(SortCriterion(SortKey.CUSTOM)), noUsage)))
    }

    @Test
    fun `default ApplianceSort is custom`() {
        assertTrue(ApplianceSort().isCustom)
    }

    @Test
    fun `name ascending and descending`() {
        val list = listOf(app("2", "Banana"), app("1", "apple"), app("3", "Cherry"))
        assertEquals(listOf("1", "2", "3"), ids(sortAppliances(list, sort(SortCriterion(SortKey.NAME)), noUsage)))
        assertEquals(
            listOf("3", "2", "1"),
            ids(sortAppliances(list, sort(SortCriterion(SortKey.NAME, descending = true)), noUsage)),
        )
    }

    @Test
    fun `frequency descending puts most-used first`() {
        val list = listOf(app("a"), app("b"), app("c"))
        val usage = mapOf("a" to ApplianceUsage(2, 0), "b" to ApplianceUsage(9, 0), "c" to ApplianceUsage(5, 0))
        assertEquals(
            listOf("b", "c", "a"),
            ids(sortAppliances(list, sort(SortCriterion(SortKey.FREQUENCY, descending = true)), usage)),
        )
    }

    @Test
    fun `recency descending puts most-recent first`() {
        val list = listOf(app("a"), app("b"))
        val usage = mapOf("a" to ApplianceUsage(1, 100), "b" to ApplianceUsage(1, 500))
        assertEquals(
            listOf("b", "a"),
            ids(sortAppliances(list, sort(SortCriterion(SortKey.RECENCY, descending = true)), usage)),
        )
    }

    @Test
    fun `duration sorts EV as longest ascending and descending`() {
        val list = listOf(ev("car", "Kia"), app("short", h = 1), app("long", h = 5))
        assertEquals(
            listOf("short", "long", "car"),
            ids(sortAppliances(list, sort(SortCriterion(SortKey.DURATION)), noUsage)),
        )
        assertEquals(
            listOf("car", "long", "short"),
            ids(sortAppliances(list, sort(SortCriterion(SortKey.DURATION, descending = true)), noUsage)),
        )
    }

    @Test
    fun `type groups by icon and clusters EVs with no icon`() {
        val list = listOf(app("w1", icon = "washer"), ev("car", "Kia"), app("d1", icon = "dryer"), app("w2", icon = "washer"))
        val sorted = ids(sortAppliances(list, sort(SortCriterion(SortKey.TYPE)), noUsage))
        // EV (empty icon key) sorts before "dryer"/"washer"; washers stay together.
        assertEquals("car", sorted.first())
        assertEquals(listOf("d1", "w1", "w2"), sorted.drop(1))
    }

    @Test
    fun `multi-level type then name`() {
        val list = listOf(
            app("w2", "Zeta", icon = "washer"),
            app("d1", "Alpha", icon = "dryer"),
            app("w1", "Alpha", icon = "washer"),
        )
        assertEquals(
            listOf("d1", "w1", "w2"),
            ids(sortAppliances(list, sort(SortCriterion(SortKey.TYPE), SortCriterion(SortKey.NAME)), noUsage)),
        )
    }

    @Test
    fun `stable sort keeps stored order for equal keys`() {
        val list = listOf(app("a", icon = "x"), app("b", icon = "x"), app("c", icon = "x"))
        assertEquals(listOf("a", "b", "c"), ids(sortAppliances(list, sort(SortCriterion(SortKey.TYPE)), noUsage)))
    }

    @Test
    fun `hasCollisions true when a key leaves ties, false when distinct`() {
        val list = listOf(app("a", icon = "x"), app("b", icon = "x"), app("c", icon = "y"))
        assertTrue(hasCollisions(list, listOf(SortCriterion(SortKey.TYPE)), noUsage))
        val distinct = listOf(app("a", "Alpha"), app("b", "Beta"))
        assertFalse(hasCollisions(distinct, listOf(SortCriterion(SortKey.NAME)), noUsage))
    }

    @Test
    fun `hasCollisions false for fewer than two or no orderable criteria`() {
        assertFalse(hasCollisions(listOf(app("a")), listOf(SortCriterion(SortKey.NAME)), noUsage))
        assertFalse(hasCollisions(listOf(app("a"), app("b")), listOf(SortCriterion(SortKey.CUSTOM)), noUsage))
        assertFalse(hasCollisions(listOf(app("a"), app("b")), emptyList(), noUsage))
    }

    @Test
    fun `nextAssignableKeys excludes custom and used keys`() {
        val keys = nextAssignableKeys(listOf(SortCriterion(SortKey.TYPE), SortCriterion(SortKey.NAME)))
        assertFalse(keys.contains(SortKey.CUSTOM))
        assertFalse(keys.contains(SortKey.TYPE))
        assertFalse(keys.contains(SortKey.NAME))
        assertTrue(keys.containsAll(listOf(SortKey.FREQUENCY, SortKey.RECENCY, SortKey.DURATION)))
    }

    @Test
    fun `combineUsage sums counts and maxes recency across ids`() {
        val phone = mapOf("a" to ApplianceUsage(2, 100), "b" to ApplianceUsage(1, 50))
        val watch = mapOf("a" to ApplianceUsage(3, 80), "c" to ApplianceUsage(4, 900))
        val combined = combineUsage(phone, watch)
        assertEquals(ApplianceUsage(5, 100), combined["a"])
        assertEquals(ApplianceUsage(1, 50), combined["b"])
        assertEquals(ApplianceUsage(4, 900), combined["c"])
    }

    @Test
    fun `mergeForHome interleaved sorts everything into one flat run`() {
        val all = listOf(ev("car", "Kia"), app("z", "Zeta"), app("a", "Alpha"))
        val layout = mergeForHome(all, sort(SortCriterion(SortKey.NAME)), noUsage, EvPosition.INTERLEAVED, separate = false)
        assertTrue(layout is HomeChipLayout.Flat)
        assertEquals(listOf("a", "car", "z"), ids((layout as HomeChipLayout.Flat).items))
    }

    @Test
    fun `mergeForHome first and last place vehicle block, name-ordered`() {
        val all = listOf(ev("c2", "Zoe"), app("a", "Appliance"), ev("c1", "Ariya"))
        val first = mergeForHome(all, sort(SortCriterion(SortKey.NAME)), noUsage, EvPosition.FIRST, separate = false)
        assertEquals(listOf("c1", "c2", "a"), ids((first as HomeChipLayout.Flat).items))
        val last = mergeForHome(all, sort(SortCriterion(SortKey.NAME)), noUsage, EvPosition.LAST, separate = false)
        assertEquals(listOf("a", "c1", "c2"), ids((last as HomeChipLayout.Flat).items))
    }

    @Test
    fun `mergeForHome separate yields two sections with vehiclesFirst flag`() {
        val all = listOf(ev("car", "Kia"), app("a", "Appliance"))
        val sectioned = mergeForHome(all, sort(SortCriterion(SortKey.NAME)), noUsage, EvPosition.FIRST, separate = true)
        assertTrue(sectioned is HomeChipLayout.Sectioned)
        sectioned as HomeChipLayout.Sectioned
        assertTrue(sectioned.vehiclesFirst)
        assertEquals(listOf("car"), ids(sectioned.first))
        assertEquals(listOf("a"), ids(sectioned.second))
    }

    @Test
    fun `mergeForHome with no vehicles is flat regardless of separate`() {
        val all = listOf(app("a", "Alpha"), app("b", "Beta"))
        val layout = mergeForHome(all, sort(SortCriterion(SortKey.NAME)), noUsage, EvPosition.FIRST, separate = true)
        assertTrue(layout is HomeChipLayout.Flat)
    }

    @Test
    fun `mergeForHome with only vehicles stays flat even when separate`() {
        val all = listOf(ev("c1", "Ariya"), ev("c2", "Zoe"))
        val layout = mergeForHome(all, sort(SortCriterion(SortKey.NAME)), noUsage, EvPosition.LAST, separate = true)
        assertTrue(layout is HomeChipLayout.Flat) // no empty "appliances" section
        assertEquals(listOf("c1", "c2"), ids((layout as HomeChipLayout.Flat).items))
    }

    @Test
    fun `mergeForHome custom with interleaved falls through to appending vehicles last`() {
        val all = listOf(ev("car", "Kia"), app("b"), app("a"))
        val layout = mergeForHome(all, ApplianceSort(), noUsage, EvPosition.INTERLEAVED, separate = false)
        // Custom keeps appliance order (b, a); interleaved is undefined under custom → vehicles last.
        assertEquals(listOf("b", "a", "car"), ids((layout as HomeChipLayout.Flat).items))
    }

    // --- Grouping by type ---

    private val grouped = listOf(
        app("d1", "eco", icon = "dishwasher"),
        app("w1", "cotton", icon = "washing_machine"),
        app("d2", "quick", icon = "dishwasher"),
        ev("car", "Kia"),
        app("w2", "darks", icon = "washing_machine"),
    )

    @Test
    fun `groupForHome buckets by type, group and within-group order follow the sort`() {
        val layout = groupForHome(grouped, sort(SortCriterion(SortKey.NAME)), noUsage, columns = false)
        // Name order is cotton, darks, eco, Kia, quick → washing_machine seen first, then dishwasher, then EV.
        assertEquals(listOf("washing_machine", "dishwasher", null), layout.groups.map { it.iconId })
        assertEquals(listOf("w1", "w2"), ids(layout.groups[0].items))
        assertEquals(listOf("d1", "d2"), ids(layout.groups[1].items))
    }

    @Test
    fun `groupForHome folds vehicles into a group when not a separate section`() {
        val layout = groupForHome(grouped, sort(SortCriterion(SortKey.NAME)), noUsage, columns = false)
        val evGroup = layout.groups.single { it.isVehicles }
        assertEquals(null, evGroup.iconId)
        assertEquals(listOf("car"), ids(evGroup.items))
        assertTrue(layout.vehicles.isEmpty()) // no separate block
    }

    @Test
    fun `groupForHome lifts a separate EV section into a block, placed by position`() {
        val below = groupForHome(grouped, sort(SortCriterion(SortKey.NAME)), noUsage, columns = true, EvPosition.LAST, separate = true)
        // Vehicles are out of the type groups and in the block, below the grid.
        assertFalse(below.groups.any { it.isVehicles })
        assertEquals(listOf("dishwasher", "washing_machine"), below.groups.map { it.iconId }.sortedBy { it })
        assertEquals(listOf("car"), ids(below.vehicles))
        assertFalse(below.vehiclesFirst)
        // FIRST places the same block above the grid.
        val above = groupForHome(grouped, sort(SortCriterion(SortKey.NAME)), noUsage, columns = true, EvPosition.FIRST, separate = true)
        assertTrue(above.vehiclesFirst)
        assertEquals(listOf("car"), ids(above.vehicles))
    }

    @Test
    fun `groupForHome ignores a separate section under Interleaved (no block side)`() {
        val layout = groupForHome(grouped, sort(SortCriterion(SortKey.NAME)), noUsage, columns = true, EvPosition.INTERLEAVED, separate = true)
        assertTrue(layout.vehicles.isEmpty())
        assertTrue(layout.groups.any { it.isVehicles }) // folds in instead
    }

    @Test
    fun `groupForHome treats an unset icon as the electricity type`() {
        val layout = groupForHome(listOf(app("x", "X", icon = null)), sort(SortCriterion(SortKey.NAME)), noUsage, columns = false)
        assertEquals(listOf("electricity"), layout.groups.map { it.iconId })
    }

    @Test
    fun `groupForHome on empty input yields no groups`() {
        assertTrue(groupForHome(emptyList(), ApplianceSort(), noUsage, columns = true).groups.isEmpty())
    }

    @Test
    fun `mergeForHome ROWS and COLUMNS group by type and set the columns flag`() {
        val rows = mergeForHome(grouped, sort(SortCriterion(SortKey.NAME)), noUsage, EvPosition.FIRST, separate = true, ApplianceGrouping.ROWS)
        assertTrue(rows is HomeChipLayout.Grouped)
        assertFalse((rows as HomeChipLayout.Grouped).columns)
        val cols = mergeForHome(grouped, sort(SortCriterion(SortKey.NAME)), noUsage, EvPosition.FIRST, separate = true, ApplianceGrouping.COLUMNS)
        assertTrue((cols as HomeChipLayout.Grouped).columns)
    }

    @Test
    fun `mergeForHome NONE keeps the flat or sectioned placement`() {
        val layout = mergeForHome(grouped, sort(SortCriterion(SortKey.NAME)), noUsage, EvPosition.INTERLEAVED, separate = false, ApplianceGrouping.NONE)
        assertFalse(layout is HomeChipLayout.Grouped)
    }

    // --- Sort-control edit helpers ---

    @Test
    fun `withPrimary resets to a single criterion with the key's default direction`() {
        val start = sort(SortCriterion(SortKey.TYPE), SortCriterion(SortKey.NAME))
        // Usage keys default to descending (most-used / most-recent first); others ascending.
        assertEquals(listOf(SortCriterion(SortKey.FREQUENCY, descending = true)), start.withPrimary(SortKey.FREQUENCY).criteria)
        assertEquals(listOf(SortCriterion(SortKey.NAME, descending = false)), start.withPrimary(SortKey.NAME).criteria)
        assertTrue(start.withPrimary(SortKey.CUSTOM).isCustom)
    }

    @Test
    fun `default direction is descending only for usage keys`() {
        assertTrue(defaultDescending(SortKey.FREQUENCY))
        assertTrue(defaultDescending(SortKey.RECENCY))
        assertFalse(defaultDescending(SortKey.NAME))
        assertFalse(defaultDescending(SortKey.DURATION))
        assertFalse(defaultDescending(SortKey.TYPE))
    }

    @Test
    fun `picking Most used as primary orders most-used first`() {
        val list = listOf(app("a"), app("b"), app("c"))
        val usage = mapOf("a" to ApplianceUsage(2, 0), "b" to ApplianceUsage(9, 0), "c" to ApplianceUsage(5, 0))
        val chosen = ApplianceSort().withPrimary(SortKey.FREQUENCY)
        assertEquals(listOf("b", "c", "a"), ids(sortAppliances(list, chosen, usage)))
    }

    @Test
    fun `withLevelKey and withAddedTiebreaker apply the key's default direction`() {
        val start = sort(SortCriterion(SortKey.TYPE), SortCriterion(SortKey.NAME, descending = true))
        assertEquals(SortCriterion(SortKey.DURATION, descending = false), start.withLevelKey(1, SortKey.DURATION).criteria[1])
        assertEquals(SortCriterion(SortKey.RECENCY, descending = true), start.withLevelKey(1, SortKey.RECENCY).criteria[1])
        assertEquals(SortCriterion(SortKey.RECENCY, descending = true), start.withAddedTiebreaker(SortKey.RECENCY).criteria.last())
    }

    @Test
    fun `withToggledDirection flips only the given level`() {
        val start = sort(SortCriterion(SortKey.TYPE), SortCriterion(SortKey.NAME))
        val toggled = start.withToggledDirection(0)
        assertTrue(toggled.criteria[0].descending)
        assertFalse(toggled.criteria[1].descending)
    }

    @Test
    fun `withAddedTiebreaker appends and withoutLevel removes`() {
        val start = sort(SortCriterion(SortKey.TYPE))
        val added = start.withAddedTiebreaker(SortKey.NAME)
        assertEquals(listOf(SortKey.TYPE, SortKey.NAME), added.criteria.map { it.key })
        assertEquals(listOf(SortKey.TYPE), added.withoutLevel(1).criteria.map { it.key })
    }
}
