# Appliance Power Rating (kW)

## Idea

Add an optional **power (kW)** field to appliances. The app already calculates and
displays total cost and per-slot costs, but assumes a 1 kW load (as noted in the
disclaimer: "Costs shown are per 1 kW load"). With an explicit power field, the cost
would reflect the appliance's actual consumption — e.g. a 2 kW dishwasher would show
double the current cost figure, while a 0.9 kW washing machine would show 90%.

This makes the cost display immediately useful without mental math.

## What changes

Currently, `computeWindowCost()` computes `price * slotHours` per slot — effectively
`price * time * 1 kW`. With a `powerKw` value, this becomes `price * time * powerKw`.

When `powerKw` is `null` (not set), the app behaves exactly as today: costs per 1 kW,
with the existing disclaimer.

When `powerKw` is set, costs are scaled by the power rating and the disclaimer changes
to reflect the actual load (e.g. "Costs shown are for a 2.0 kW load").

## Model change

`Appliance` data class gains an optional field:

```kotlin
@Serializable
data class Appliance(
    val id: String,
    val name: String,
    val durationHours: Int,
    val durationMinutes: Int,
    val icon: String,
    val powerKw: Double? = null,  // optional power rating in kilowatts
)
```

## Where the multiplication happens

Two options:

### Option A: Multiply in the result display (UI only)
Keep `CheapestWindowFinder` untouched — it always computes per-1-kW costs. The UI
multiplies `totalCost * powerKw` and `slot.cost * powerKw` when displaying. Simple,
no algorithm changes, easy to test.

### Option B: Pass powerKw into the algorithm
`findCheapestWindow()` takes a `powerKw` parameter and includes it in the cost
calculation. Costs in `WindowResult` and `BreakdownSlot` reflect the actual load.
Cleaner data model (what you see is what you get), but changes the core algorithm's
contract.

Option A is probably better — the cheapest *window* doesn't change regardless of load
(the minimum-cost window at 1 kW is the same at 2 kW), so the algorithm doesn't need
to know about power. It's purely a display concern.

## UI changes

### Appliance editor (settings)
- Add a "Power (kW)" text field below the duration picker, with a numeric keyboard.
- Hint text: "optional". Leave empty to skip.
- Validate: must be positive if set. Reasonable range: 0.01–50 kW.

### Result screen
- When `powerKw` is set: multiply displayed costs by the power rating, and update the
  disclaimer to say "Costs shown are for a {powerKw} kW load" (or drop the qualifier
  entirely since costs now reflect reality).
- When `powerKw` is null: no change — "Costs shown are per 1 kW load" as today.

### Watch result screen
- No change needed — the watch only shows start/end times, not costs.

## Considerations

- **Backwards compatibility** — the field defaults to `null`, so existing appliances are
  unaffected. Serialization handles missing fields gracefully.
- **Accuracy** — this is still an estimate. Real consumption varies (motor startup surges,
  heating element cycling, standby phases). The disclaimer should keep "estimated" language.
- **Variable-power appliances** — some appliances (e.g. heat pumps, ovens) don't draw
  constant power. The kW value represents an average. Could note this in help text, but
  keeping it simple is fine for v1.
- **All-in pricing interaction** — if all-in pricing is implemented later (adding taxes,
  grid fees), the cost estimate should use the all-in price per slot. The multiplication
  approach (Option A) works the same regardless.
