# Car Charging Support

## Idea

Add a dedicated screen (or tab) for EV charging that lets users enter their current and
target state of charge instead of a raw duration. The app calculates the required charging
time from the battery parameters and charger power, then runs the existing cheapest-window
algorithm.

## User flow

1. User enters **current SoC** (e.g. 20%) and **target SoC** (e.g. 80%)
2. App calculates charging duration from battery capacity and charger power:
   `hours = (targetSoC - currentSoC) / 100 * batteryCapacityKwh / chargerPowerKw`
3. Runs `findCheapestWindow()` with the calculated duration
4. Shows the same result screen (start/end time, cost breakdown, bar chart)

## Settings

- **Charger power (kW)** — home charger output (e.g. 3.7, 7.4, 11, 22 kW). Stored in
  `SettingsRepository`, synced to watch via Data Layer.
- **Battery capacity (kWh)** — total usable capacity of the EV battery (e.g. 60 kWh for
  a typical mid-range EV). Could offer presets for popular models or just a numeric input.

## UI

- New screen/tab alongside the existing duration picker, or a dedicated "EV" appliance
  type that opens the SoC input instead of the duration picker.
- Two sliders or number inputs for current and target SoC (0–100%).
- Display the calculated duration before searching so the user can verify it looks right.
- Could show estimated cost at current electricity prices as a preview.

## Considerations

- Charging is not perfectly linear — efficiency drops near 100% SoC. A simple linear
  calculation is a good starting point but may underestimate time for high target SoC
  values. Could add an efficiency factor (e.g. 90%) or warn when target > 80%.
- Some users may want to set a "ready by" deadline (e.g. charge to 80% by 7:00 AM).
  This would constrain the search window rather than searching all available prices.
- The charger power and battery capacity could also be saved per-vehicle if the user
  has multiple EVs.
- Watch app: could show a simplified version — tap an EV "appliance" that uses the
  last-used SoC values, or just show the duration-based result.
