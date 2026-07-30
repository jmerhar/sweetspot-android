#!/usr/bin/env python3

"""Unit tests for bin/data/build-ev-db.py (stdlib unittest, no network).

Covers the pure logic — the normaliser's required-field filtering and rounding, the two
per-source adapters' field mapping, and the dedup key (including that distinct trims of the
same model-year are kept apart). Run: python3 bin/data/test_build_ev_db.py  (or: make test-ev-db)
"""

import importlib.util
import os
import unittest

# The module filename has a hyphen, so load it by path rather than `import`.
_SPEC = importlib.util.spec_from_file_location(
    "build_ev_db", os.path.join(os.path.dirname(os.path.abspath(__file__)), "build-ev-db.py"))
ev = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(ev)


class NormaliseTest(unittest.TestCase):
    def test_valid_entry_is_normalised_and_rounded(self):
        v = ev.normalise("Tesla", "Model 3", "Long Range", 2024, 57.53, 11.04)
        self.assertEqual(v["brand"], "Tesla")
        self.assertEqual(v["model"], "Model 3")
        self.assertEqual(v["variant"], "Long Range")
        self.assertEqual(v["year"], 2024)
        self.assertEqual(v["batteryKwh"], 57.5)   # rounded to 1 dp
        self.assertEqual(v["acMaxPowerKw"], 11.0)

    def test_missing_brand_or_model_dropped(self):
        self.assertIsNone(ev.normalise("", "Model 3", None, 2024, 57.5, 11.0))
        self.assertIsNone(ev.normalise("Tesla", None, None, 2024, 57.5, 11.0))

    def test_missing_or_nonpositive_battery_or_ac_dropped(self):
        self.assertIsNone(ev.normalise("Tesla", "Model 3", None, 2024, None, 11.0))
        self.assertIsNone(ev.normalise("Tesla", "Model 3", None, 2024, 0, 11.0))
        self.assertIsNone(ev.normalise("Tesla", "Model 3", None, 2024, 57.5, None))
        self.assertIsNone(ev.normalise("Tesla", "Model 3", None, 2024, 57.5, 0))

    def test_blank_variant_and_missing_year_become_none(self):
        v = ev.normalise("VW", "ID.3", "", None, 58.0, 11.0)
        self.assertIsNone(v["variant"])
        self.assertIsNone(v["year"])


class AdapterTest(unittest.TestCase):
    def test_kilowatt_skips_non_cars(self):
        self.assertIsNone(ev.adapt_kilowatt({"vehicle_type": "motorbike", "brand": "X", "model": "Y"}))

    def test_kilowatt_maps_fields(self):
        v = ev.adapt_kilowatt({
            "vehicle_type": "car", "brand": "Kia", "model": "EV6", "variant": "GT",
            "release_year": 2023, "usable_battery_size": 74.0,
            "ac_charger": {"max_power": 11.0},
        })
        self.assertEqual((v["brand"], v["model"], v["variant"], v["year"]), ("Kia", "EV6", "GT", 2023))
        self.assertEqual(v["batteryKwh"], 74.0)
        self.assertEqual(v["acMaxPowerKw"], 11.0)

    def test_openev_maps_nested_fields_and_trim_fallback(self):
        v = ev.adapt_openev({
            "make": {"name": "BMW"}, "model": {"name": "i4"}, "year": 2024,
            "trim": {"name": "eDrive40"},
            "battery": {"pack_capacity_kwh_net": 80.7},
            "charging": {"ac": {"max_power_kw": 11.0}},
        })
        self.assertEqual((v["brand"], v["model"], v["variant"], v["year"]), ("BMW", "i4", "eDrive40", 2024))
        self.assertEqual(v["batteryKwh"], 80.7)

    def test_openev_missing_specs_dropped(self):
        self.assertIsNone(ev.adapt_openev({"make": {"name": "BMW"}, "model": {"name": "i4"}}))


class DedupKeyTest(unittest.TestCase):
    def test_distinct_trims_of_same_model_year_do_not_collide(self):
        sr = ev.normalise("Tesla", "Model 3", "Standard Range", 2024, 57.5, 11.0)
        lr = ev.normalise("Tesla", "Model 3", "Long Range", 2024, 75.0, 11.0)
        self.assertNotEqual(ev.dedup_key(sr), ev.dedup_key(lr))

    def test_key_is_case_insensitive(self):
        a = ev.normalise("Tesla", "Model 3", "Long Range", 2024, 75.0, 11.0)
        b = ev.normalise("TESLA", "MODEL 3", "LONG RANGE", 2024, 75.0, 11.0)
        self.assertEqual(ev.dedup_key(a), ev.dedup_key(b))

    def test_same_trim_same_year_collides(self):
        a = ev.normalise("VW", "ID.4", None, 2023, 77.0, 11.0)
        b = ev.normalise("VW", "ID.4", None, 2023, 77.0, 7.4)
        self.assertEqual(ev.dedup_key(a), ev.dedup_key(b))


if __name__ == "__main__":
    unittest.main(verbosity=2)
