package si.merhar.sweetspot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.merhar.sweetspot.model.BreakdownSlot
import si.merhar.sweetspot.ui.theme.BarTrackGray
import si.merhar.sweetspot.ui.theme.SubtitleGray
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun BreakdownTable(breakdown: List<BreakdownSlot>, modifier: Modifier = Modifier) {
    // Header
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BarTrackGray)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text("Time slot", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SubtitleGray, modifier = Modifier.weight(1.2f))
        Text("Price (\u20AC/kWh)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SubtitleGray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        Text("Fraction", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SubtitleGray, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
        Text("Cost (\u20AC)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SubtitleGray, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
    }

    breakdown.forEachIndexed { index, slot ->
        if (index > 0) {
            HorizontalDivider(color = BarTrackGray)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            val start = slot.time.format(timeFormatter)
            val end = slot.time.plusHours(1).format(timeFormatter)
            Text("$start\u2013$end", fontSize = 13.sp, modifier = Modifier.weight(1.2f))
            Text(String.format("%.4f", slot.price), fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text(
                if (slot.fraction == 1.0) "1" else String.format("%.2f", slot.fraction),
                fontSize = 13.sp,
                modifier = Modifier.weight(0.7f),
                textAlign = TextAlign.End
            )
            Text(String.format("%.4f", slot.cost), fontSize = 13.sp, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
        }
    }
}
