import importlib.util
from pathlib import Path
import subprocess
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / 'scripts/release/docs_only.py'
spec = importlib.util.spec_from_file_location('docs_only', SCRIPT)
docs = importlib.util.module_from_spec(spec)
spec.loader.exec_module(docs)


class DocsOnlyTests(unittest.TestCase):
    def test_readme_and_markdown_docs_skip_the_version_gate(self):
        self.assertTrue(docs.docs_only(['README.md']))
        self.assertTrue(docs.docs_only(['README.md', 'docs/DEVICE_TEST_EVIDENCE.md', 'docs/sub/NOTE.md']))

    def test_release_inputs_keep_full_validation(self):
        for path in ['app/build.gradle.kts', 'app/src/main/java/app/tellyscope/MainActivity.kt', 'adb/build.gradle.kts',
                     'gradle/libs.versions.toml', 'gradle.properties', 'settings.gradle.kts', 'SECRETO_CHANGELOG.md',
                     'CHANGELOG.md', 'NOTICE', 'LICENSE', 'release/SOURCE_AVAILABILITY.md', 'release/RELEASE_NOTICE.md',
                     '.github/workflows/release-check.yml', '.github/workflows/ci.yml', 'scripts/release/check_release.py',
                     'scripts/release/docs_only.py', 'tests/release/test_docs_only.py', 'data/launchers.json',
                     'docs/NATIVE_GRAPHICS_PATH_PROVENANCE.json', 'docs/JAVA25_VERIFICATION_RESULTS.json',
                     'docs/index.html', 'app/README.md', 'CONTRIBUTING.md']:
            self.assertFalse(docs.docs_only([path]), path)
            self.assertFalse(docs.docs_only(['README.md', path]), 'mixed with ' + path)

    def test_empty_or_unsafe_lists_are_not_docs_only(self):
        self.assertFalse(docs.docs_only([]))
        self.assertFalse(docs.docs_only(['', '  ']))
        self.assertFalse(docs.docs_only(['docs/../app/build.gradle.kts']))
        self.assertFalse(docs.docs_only(['/README.md']))

    def test_rename_out_of_the_app_is_not_docs_only(self):
        # git diff --no-renames lists both sides; the removed app file keeps validation on.
        self.assertFalse(docs.docs_only(['app/src/main/java/app/tellyscope/Store.kt', 'docs/Store.md']))

    def test_command_line_output(self):
        def run(text):
            return subprocess.run([sys.executable, str(SCRIPT)], input=text, capture_output=True, text=True, check=True).stdout.strip()
        self.assertEqual(run('README.md\ndocs/A.md\n'), 'docs_only=true')
        self.assertEqual(run('README.md\napp/build.gradle.kts\n'), 'docs_only=false')
        self.assertEqual(run(''), 'docs_only=false')


if __name__ == '__main__':
    unittest.main()
