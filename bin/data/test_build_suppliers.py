#!/usr/bin/env python3

"""Unit tests for bin/data/build-suppliers.py (stdlib unittest, no network).

Covers the pure logic extracted from the network/IO paths: legend parsing, Frank tax derivation,
registry merge, surcharge differencing, normalisation, slugging, float coercion, and the
"unchanged except timestamp" write guard. Run: python3 bin/data/test_build_suppliers.py  (or: make test-suppliers)
"""

import importlib.util
import json
import os
import tempfile
import unittest
from datetime import datetime, timezone

# The module filename has a hyphen, so load it by path rather than `import`.
_SPEC = importlib.util.spec_from_file_location(
    "build_suppliers", os.path.join(os.path.dirname(os.path.abspath(__file__)), "build-suppliers.py"))
bs = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(bs)

NOW = datetime(2026, 7, 11, 12, 0, 0, tzinfo=timezone.utc)


class SlugifyTest(unittest.TestCase):
    def test_slugs_are_lowercase_alphanumeric(self):
        self.assertEqual(bs._slugify("Frank Energie"), "frankenergie")
        self.assertEqual(bs._slugify("Vrij op naam"), "vrijopnaam")
        self.assertEqual(bs._slugify("Energy Zero"), "energyzero")

    def test_empty_or_symbol_only_returns_none(self):
        self.assertIsNone(bs._slugify(""))
        self.assertIsNone(bs._slugify("  -- "))


class ToFloatTest(unittest.TestCase):
    def test_parses_numbers_and_numeric_strings(self):
        self.assertEqual(bs._to_float(0.15), 0.15)
        self.assertEqual(bs._to_float("0.140855"), 0.140855)
        self.assertEqual(bs._to_float(3), 3.0)

    def test_returns_none_for_unparseable(self):
        for bad in (None, "", "abc", {}, []):
            self.assertIsNone(bs._to_float(bad))


class ParseLegendaTest(unittest.TestCase):
    def test_parses_code_name_block(self):
        page = ("<div><p>intro</p><h3>Legenda</h3>"
                "<p>FR = Frank Energie</p><p>ZP = Zonneplan</p>"
                "<p>Zonpercentage</p><p>ignored = but after break-word</p></div>")
        legend = bs.parse_legenda(page)
        self.assertEqual(legend["FR"], "Frank Energie")
        self.assertEqual(legend["ZP"], "Zonneplan")
        self.assertNotIn("Zonpercentage", legend)

    def test_no_legenda_returns_empty(self):
        self.assertEqual(bs.parse_legenda("<html><body>no legend here</body></html>"), {})

    def test_html_entities_unescaped(self):
        self.assertEqual(bs.parse_legenda("<p>Legenda</p><p>VDB = A &amp; B</p>")["VDB"], "A & B")


class DeriveTaxBlockTest(unittest.TestCase):
    # marketPriceTax = marketPrice*0.21; energyTaxPrice = 0.09161*1.21 = 0.1108481
    PRICES = [
        {"marketPrice": 0.10, "marketPriceTax": 0.021, "energyTaxPrice": 0.1108481, "sourcingMarkupPrice": 0.01815},
        {"marketPrice": 0.20, "marketPriceTax": 0.042, "energyTaxPrice": 0.1108481, "sourcingMarkupPrice": 0.01815},
    ]

    def test_derives_vat_and_energy_tax_exvat(self):
        taxes, vat, energy_tax = bs.derive_tax_block(self.PRICES)
        self.assertEqual(vat, 0.21)
        self.assertEqual(energy_tax, 0.09161)
        self.assertEqual({t["id"]: (t["type"], t["value"], t["source"]) for t in taxes}, {
            "energyTax": ("perKwh", 0.09161, "frank"),
            "vat": ("percentage", 0.21, "frank"),
        })

    def test_ignores_near_zero_market_slots_for_vat(self):
        # A cheap hour (|marketPrice| <= 0.02) must not skew VAT even with a noisy tax field.
        prices = self.PRICES + [{"marketPrice": 0.001, "marketPriceTax": 0.5, "energyTaxPrice": 0.1108481}]
        _, vat, _ = bs.derive_tax_block(prices)
        self.assertEqual(vat, 0.21)

    def test_empty_raises(self):
        with self.assertRaises(bs.TariffError):
            bs.derive_tax_block([])

    def test_missing_fields_raise(self):
        with self.assertRaises(bs.TariffError):
            bs.derive_tax_block([{"marketPrice": 0.1}])  # no tax fields

    def test_implausible_vat_raises(self):
        with self.assertRaises(bs.TariffError):
            bs.derive_tax_block([{"marketPrice": 0.1, "marketPriceTax": 0.09, "energyTaxPrice": 0.11}])  # 90% VAT


class MergeRegistryTest(unittest.TestCase):
    def test_keeps_existing_id_refreshes_name_adds_new(self):
        committed = {"FR": {"id": "frank", "name": "Frank (old)"}}
        legend = {"FR": "Frank Energie", "XY": "New Co"}
        merged = bs.merge_registry(committed, legend)
        self.assertEqual(merged["FR"], {"id": "frank", "name": "Frank Energie"})  # id kept, name refreshed
        self.assertEqual(merged["XY"], {"id": "newco", "name": "New Co"})         # new -> slug id

    def test_does_not_mutate_committed(self):
        committed = {"FR": {"id": "frank", "name": "Frank (old)"}}
        bs.merge_registry(committed, {"FR": "Frank Energie"})
        self.assertEqual(committed["FR"]["name"], "Frank (old)")

    def test_empty_legend_returns_copy(self):
        committed = {"FR": {"id": "frank", "name": "Frank Energie"}}
        self.assertEqual(bs.merge_registry(committed, {}), committed)


class SurchargeFromRowsTest(unittest.TestCase):
    VAT, ETAX = 0.21, 0.09161

    def test_recovers_surcharge_by_differencing(self):
        # allin/(1+vat) - spot - energyTax; Frank widget row reproduces 0.015.
        rows = [{"prijs": "0.006482", "prijsFR": "0.136894"}]
        self.assertAlmostEqual(bs.surcharge_from_rows(rows, "prijsFR", self.VAT, self.ETAX), 0.015043, places=5)

    def test_median_across_rows_and_skips_bad_cells(self):
        rows = [
            {"prijs": "0.10", "prijsXX": "0.25"},
            "not-a-dict",                       # skipped
            {"prijs": "0.10", "prijsXX": "n/a"},  # unparseable -> skipped
            {"prijs": "0.10", "prijsXX": "0.25"},
        ]
        val = bs.surcharge_from_rows(rows, "prijsXX", self.VAT, self.ETAX)
        self.assertAlmostEqual(val, 0.25 / 1.21 - 0.10 - self.ETAX, places=6)

    def test_no_usable_rows_returns_none(self):
        self.assertIsNone(bs.surcharge_from_rows([{"prijs": "0.10"}], "prijsFR", self.VAT, self.ETAX))


class NormaliseTest(unittest.TestCase):
    def test_valid_supplier_rounds_and_shapes(self):
        s = bs.normalise("frank", "Frank Energie", 0.0150049, source="frank", warnings=[])
        self.assertEqual(s, {"id": "frank", "name": "Frank Energie", "surchargePerKwh": 0.01500,
                             "fixedMonthlyFee": None, "source": "frank"})

    def test_out_of_range_returns_none_and_warns(self):
        warns = []
        self.assertIsNone(bs.normalise("x", "X", 0.10, source="enever", warnings=warns))  # 10 ct too high
        self.assertTrue(warns and "outside plausible range" in warns[0])

    def test_none_surcharge_returns_none(self):
        self.assertIsNone(bs.normalise("x", "X", None, warnings=[]))

    def test_boundaries_inclusive(self):
        self.assertIsNotNone(bs.normalise("a", "A", bs.SURCHARGE_MAX, warnings=[]))
        self.assertIsNotNone(bs.normalise("b", "B", bs.SURCHARGE_MIN, warnings=[]))
        self.assertIsNone(bs.normalise("c", "C", bs.SURCHARGE_MAX + 0.001, warnings=[]))


class UnchangedExceptGeneratedTest(unittest.TestCase):
    def test_detects_timestamp_only_change(self):
        with tempfile.TemporaryDirectory() as d:
            path = os.path.join(d, "nl.json")
            with open(path, "w") as f:
                json.dump({"generated": "A", "suppliers": [1, 2]}, f)
            self.assertTrue(bs._unchanged_except_generated(path, {"generated": "B", "suppliers": [1, 2]}))
            self.assertFalse(bs._unchanged_except_generated(path, {"generated": "B", "suppliers": [1, 3]}))

    def test_missing_file_is_changed(self):
        self.assertFalse(bs._unchanged_except_generated("/no/such/file.json", {"generated": "B"}))


class RegistryFileRoundTripTest(unittest.TestCase):
    def test_write_then_load_and_skip_when_unchanged(self):
        with tempfile.TemporaryDirectory() as d:
            bs.ENEVER_REGISTRY_PATH = os.path.join(d, "enever-suppliers.json")
            suppliers = {"FR": {"id": "frank", "name": "Frank Energie"}}
            bs._write_enever_registry_file(suppliers, NOW)
            self.assertEqual(bs._load_enever_registry_file(), suppliers)
            # Unchanged content must not rewrite (mtime stays put).
            mtime = os.path.getmtime(bs.ENEVER_REGISTRY_PATH)
            bs._write_enever_registry_file(suppliers, datetime(2027, 1, 1, tzinfo=timezone.utc))
            self.assertEqual(os.path.getmtime(bs.ENEVER_REGISTRY_PATH), mtime)


class BuildCountryTest(unittest.TestCase):
    """Orchestration in build_country: tax sourcing, multi-source supplier merge, shape."""

    @staticmethod
    def _cfg(tax_source, supplier_sources, currency="EUR"):
        return {"currency": currency, "tax_source": tax_source, "supplier_sources": supplier_sources}

    def test_merges_later_source_wins_sorts_and_collects_warnings(self):
        taxes = [{"type": "perKwh", "amount": 0.1, "source": "test"}]
        def tax_source(ctx):
            return taxes
        def src_a(ctx, warnings):
            warnings.append("a-warn")
            return [{"id": "zeta", "surchargePerKwh": 0.02}, {"id": "alpha", "surchargePerKwh": 0.01}]
        def src_b(ctx, warnings):  # 'alpha' collides — later source must win
            return [{"id": "alpha", "surchargePerKwh": 0.09}]

        obj = bs.build_country("NL", self._cfg(tax_source, [src_a, src_b]), NOW)

        self.assertEqual(obj["country"], "NL")
        self.assertEqual(obj["currency"], "EUR")
        self.assertTrue(obj["usable"])
        self.assertEqual(obj["errors"], [])
        self.assertEqual(obj["warnings"], ["a-warn"])
        self.assertEqual(obj["taxes"], taxes)
        self.assertEqual(obj["schemaVersion"], bs.SCHEMA_VERSION)
        self.assertEqual(obj["generated"], "2026-07-11T12:00:00Z")
        self.assertEqual([s["id"] for s in obj["suppliers"]], ["alpha", "zeta"])  # sorted by id
        alpha = next(s for s in obj["suppliers"] if s["id"] == "alpha")
        self.assertEqual(alpha["surchargePerKwh"], 0.09)  # src_b won the collision

    def test_tax_source_error_propagates(self):
        def tax_source(ctx):
            raise bs.TariffError("no essentials")
        with self.assertRaises(bs.TariffError):
            bs.build_country("NL", self._cfg(tax_source, [lambda ctx, w: []]), NOW)

    def test_nl_ctx_date_is_amsterdam_local(self):
        seen = {}
        def tax_source(ctx):
            seen["date"] = ctx["date"]
            return []
        bs.build_country("NL", self._cfg(tax_source, [lambda ctx, w: []]), NOW)
        self.assertEqual(seen["date"], "2026-07-11")  # 12:00 UTC → 14:00 CEST, same day


if __name__ == "__main__":
    unittest.main(verbosity=2)
