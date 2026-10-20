#!/usr/bin/env python3

"""Unit tests for bin/quality/check-lockfile.sh (stdlib unittest, no network).

A lockfile may resolve packages from the public npm registry only. Covers a clean lockfile, a
proxied host (reported by host only, once), a lookalike of the public registry, a missing file,
and the repository's own tracked lockfiles. Run: python3 bin/quality/test_check_lockfile.py
(or: make test-check-lockfile)
"""

import json
import os
import subprocess
import tempfile
import unittest

SCRIPT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "check-lockfile.sh")
PUBLIC = "https://registry.npmjs.org/"


def check(*paths):
    """Run the check on the given lockfiles (none: every tracked lockfile) and return the result."""
    return subprocess.run([SCRIPT, *paths], capture_output=True, text=True, check=False, timeout=30)


class CheckLockfileTest(unittest.TestCase):
    def setUp(self):
        self._tmp = tempfile.TemporaryDirectory()
        self.dir = self._tmp.name

    def tearDown(self):
        self._tmp.cleanup()

    def lockfile(self, *resolved):
        """Write a minimal v3 lockfile whose packages resolve to the given URLs; return its path."""
        path = os.path.join(self.dir, "package-lock.json")
        packages = {f"node_modules/p{i}": {"version": "1.0.0", "resolved": url}
                    for i, url in enumerate(resolved)}
        with open(path, "w") as f:
            json.dump({"lockfileVersion": 3, "packages": packages}, f, indent=2)
        return path

    def test_a_lockfile_on_the_public_registry_passes(self):
        result = check(self.lockfile(f"{PUBLIC}a/-/a-1.0.0.tgz", f"{PUBLIC}@s/b/-/b-2.0.0.tgz"))
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn("only to the public registry", result.stdout + result.stderr)

    def test_any_other_host_fails_and_is_named_once_without_its_paths(self):
        proxied = "https://npm.example.test/artifactory/api/npm/npm/a/-/a-1.0.0.tgz"
        result = check(self.lockfile(f"{PUBLIC}b/-/b-1.0.0.tgz", proxied, proxied))
        self.assertEqual(result.returncode, 1)
        self.assertIn("  https://npm.example.test/…", result.stderr)
        self.assertEqual(result.stderr.count("npm.example.test"), 1)
        self.assertNotIn("artifactory", result.stderr)

    def test_a_registry_merely_starting_like_the_public_one_fails(self):
        result = check(self.lockfile("https://registry.npmjs.org.example.test/a/-/a-1.0.0.tgz"))
        self.assertEqual(result.returncode, 1)

    def test_a_dot_is_not_a_wildcard_in_the_public_prefix(self):
        result = check(self.lockfile("https://registryXnpmjs.org/a/-/a-1.0.0.tgz"))
        self.assertEqual(result.returncode, 1)

    def test_a_missing_lockfile_fails(self):
        result = check(os.path.join(self.dir, "package-lock.json"))
        self.assertEqual(result.returncode, 1)
        self.assertIn("is missing", result.stderr)

    def test_the_repositorys_own_lockfiles_pass(self):
        result = check()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertNotIn(" 0 lockfile", result.stdout + result.stderr)


if __name__ == "__main__":
    unittest.main(verbosity=2)
