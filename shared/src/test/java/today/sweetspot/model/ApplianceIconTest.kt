package today.sweetspot.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Iron
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [applianceIconFor] icon resolution.
 */
class ApplianceIconTest {

    @Test
    fun `known ID returns correct icon`() {
        assertEquals(Icons.Outlined.Iron, applianceIconFor("iron"))
    }

    @Test
    fun `unknown ID returns bolt fallback icon`() {
        assertEquals(Icons.Outlined.Bolt, applianceIconFor("nonexistent"))
    }

    @Test
    fun `empty ID returns bolt fallback icon`() {
        assertEquals(Icons.Outlined.Bolt, applianceIconFor(""))
    }
}
