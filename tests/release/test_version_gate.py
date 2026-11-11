import importlib.util
import os
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location('check_release_gate', ROOT / 'scripts/release/check_release.py')
check = importlib.util.module_from_spec(spec)
spec.loader.exec_module(check)

GRADLE = 'applicationId = "com.secreto.tvmanager"\nversionName = "{name}"\nversionCode = {code}\n'
NOTES = '# Secreto TV Manager changelog\n\n## {name} — released\n'


class MaintenancePathTests(unittest.TestCase):
    def test_recognized_maintenance_paths(self):
        for path in ['README.md', 'docs/DEVICE_TEST_EVIDENCE.md', 'docs/a/b.md',
                     'app/src/test/java/app/tellyscope/LauncherFlowTest.kt', 'app/src/test/resources/fixtures/launchers.json',
                     'adb/src/test/kotlin/app/tellyscope/adb/RemoteTest.kt', 'tests/release/test_version_gate.py',
                     'tools/test_data.py', '.github/workflows/ci.yml', '.github/workflows/release-check.yml',
                     'scripts/release/check_release.py', 'scripts/release/docs_only.py', 'scripts/verification/java25-tests.gradle']:
            self.assertTrue(check.maintenance_path(path), path)

    def test_release_affecting_and_unknown_paths(self):
        for path in ['app/src/main/java/app/tellyscope/MainActivity.kt', 'app/src/main/res/values/strings.xml',
                     'app/src/main/AndroidManifest.xml', 'app/src/main/resources/secreto-launcher/home.jpeg',
                     'app/src/github/java/app/tellyscope/Store.kt', 'app/src/play/java/app/tellyscope/Store.kt',
                     'app/src/helper/java/app/tellyscope/helper/Keys.java', 'adb/src/main/kotlin/app/tellyscope/adb/Remote.kt',
                     'cli/src/main/kotlin/Main.kt', 'app/build.gradle.kts', 'adb/build.gradle.kts', 'build.gradle.kts',
                     'settings.gradle.kts', 'gradle.properties', 'gradle/libs.versions.toml', 'gradle/wrapper/gradle-wrapper.properties',
                     'gradlew', 'app/proguard-rules.pro', 'release/SOURCE_AVAILABILITY.md', 'release/licenses/Apache-2.0.txt',
                     'LICENSE', 'NOTICE', 'SECRETO_CHANGELOG.md', 'CHANGELOG.md', 'data/launchers.json', 'tools/data.py',
                     'tools/data-keys/primary.pub', '.github/workflows/release.yml', '.github/CODEOWNERS',
                     'scripts/release/prepare_materials.py', 'scripts/release/package_final_signed_source.py',
                     'docs/NATIVE_GRAPHICS_PATH_PROVENANCE.json', 'docs/index.html', 'docs/assets/banner.jpg',
                     'app/src/testRelease/Foo.kt', 'app/src/tests/Foo.kt', 'tools/test_data.sh', 'CONTRIBUTING.md',
                     'unknown.txt', '', '/README.md', 'docs/../app/build.gradle.kts', 'app\\src\\main\\x.kt']:
            self.assertFalse(check.maintenance_path(path), path)


class VersionGateRepositoryTests(unittest.TestCase):
    """Runs the real gate against a throwaway repository tagged like an official release."""

    def setUp(self):
        self.old_cwd = os.getcwd()
        self.dir = tempfile.TemporaryDirectory()
        os.chdir(self.dir.name)
        self.git('init', '-q')
        self.write('app/build.gradle.kts', GRADLE.format(name='1.1.0', code=2))
        self.write('SECRETO_CHANGELOG.md', NOTES.format(name='1.1.0'))
        self.write('app/src/main/java/App.kt', 'class App\n')
        self.write('README.md', 'readme\n')
        self.commit('release')
        self.git('tag', '-a', 'v1.1.0', '-m', 'v1.1.0')

    def tearDown(self):
        os.chdir(self.old_cwd)
        self.dir.cleanup()

    def git(self, *args):
        subprocess.run(['git', '-c', 'user.name=t', '-c', 'user.email=t@example.invalid', '-c', 'commit.gpgsign=false',
                        '-c', 'tag.gpgsign=false', *args], check=True, capture_output=True)

    def write(self, path, text):
        Path(path).parent.mkdir(parents=True, exist_ok=True)
        Path(path).write_text(text)

    def commit(self, message):
        self.git('add', '-A')
        self.git('commit', '-q', '-m', message)

    def assert_passes(self):
        check.validate()

    def assert_rejected(self, path_fragment):
        with self.assertRaises(ValueError) as error:
            check.validate()
        self.assertIn('must advance', str(error.exception))
        self.assertIn(path_fragment, str(error.exception))

    def test_no_changes_since_release(self):
        self.assert_passes()

    def test_documentation_only(self):
        self.write('README.md', 'v1.1.0 readme\n')
        self.write('docs/NOTE.md', 'note\n')
        self.commit('docs')
        self.assert_passes()

    def test_test_only(self):
        self.write('app/src/test/java/AppTest.kt', 'class AppTest\n')
        self.write('tests/release/test_x.py', '\n')
        self.commit('tests')
        self.assert_passes()

    def test_workflow_only(self):
        self.write('.github/workflows/ci.yml', 'name: CI\n')
        self.write('.github/workflows/release-check.yml', 'name: check\n')
        self.commit('ci')
        self.assert_passes()

    def test_mixed_maintenance_and_production(self):
        self.write('README.md', 'changed\n')
        self.write('app/src/main/java/App.kt', 'class App { val x = 1 }\n')
        self.commit('mixed')
        self.assert_rejected('app/src/main/java/App.kt')

    def test_rename_of_production_file_into_docs(self):
        Path('docs').mkdir()
        self.git('mv', 'app/src/main/java/App.kt', 'docs/App.md')
        self.commit('rename')
        self.assert_rejected('app/src/main/java/App.kt')

    def test_deleted_production_file(self):
        self.git('rm', '-q', 'app/src/main/java/App.kt')
        self.commit('delete')
        self.assert_rejected('app/src/main/java/App.kt')

    def test_unknown_file(self):
        self.write('notes.txt', 'x\n')
        self.commit('unknown')
        self.assert_rejected('notes.txt')

    def test_uncommitted_and_untracked_release_changes(self):
        self.write('app/src/main/java/App.kt', 'class App2\n')
        self.assert_rejected('app/src/main/java/App.kt')
        self.git('checkout', '--', 'app/src/main/java/App.kt')
        self.write('app/src/main/res/new.xml', '<x/>\n')
        self.assert_rejected('app/src/main/res/new.xml')

    def test_release_metadata_and_build_changes(self):
        for path in ['SECRETO_CHANGELOG.md', 'NOTICE', 'gradle/libs.versions.toml', 'app/proguard-rules.pro', '.github/workflows/release.yml']:
            self.write(path, (Path(path).read_text() if Path(path).exists() else '') + '\n# change\n')
            self.commit(path)
            self.assert_rejected(path)

    def test_size_budget_only_change(self):
        budget = '            maxBytes = if (variant.name == "githubRelease") 4_200_000L else 4_000_000L\n'
        self.write('app/build.gradle.kts', GRADLE.format(name='1.1.0', code=2) + budget)
        self.commit('budget at release')
        self.git('tag', '-f', '-a', 'v1.1.0', '-m', 'v1.1.0')
        self.write('app/build.gradle.kts', GRADLE.format(name='1.1.0', code=2) + '            maxBytes = 4_200_000L\n')
        self.commit('budget')
        self.assert_passes()
        # Anything else in the same file still needs a version advance.
        self.write('app/build.gradle.kts', GRADLE.format(name='1.1.0', code=2) + '            maxBytes = 4_200_000L\nminSdk = 27\n')
        self.commit('budget and more')
        self.assert_rejected('app/build.gradle.kts')
        self.write('app/build.gradle.kts', GRADLE.format(name='1.1.0', code=2) + '            maxBytes = 4_200_000L // note\n')
        self.commit('budget line with extra text')
        self.assert_rejected('app/build.gradle.kts')

    def test_production_change_with_version_advance_passes(self):
        self.write('app/src/main/java/App.kt', 'class App { val x = 1 }\n')
        self.write('app/build.gradle.kts', GRADLE.format(name='1.1.1', code=3))
        self.write('SECRETO_CHANGELOG.md', NOTES.format(name='1.1.1') + NOTES.format(name='1.1.0'))
        self.commit('1.1.1')
        self.assert_passes()

    def test_partial_advance_is_still_rejected(self):
        self.write('README.md', 'docs only\n')
        self.write('app/build.gradle.kts', GRADLE.format(name='1.1.0', code=3))
        self.commit('code only')
        with self.assertRaises(ValueError):
            check.validate()


if __name__ == '__main__':
    unittest.main()
