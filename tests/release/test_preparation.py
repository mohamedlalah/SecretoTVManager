import importlib.util
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]

def load(name):
    spec = importlib.util.spec_from_file_location(name, ROOT / f'scripts/release/{name}.py')
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module

check = load('check_release')
materials = load('prepare_materials')

class ReleaseTests(unittest.TestCase):
    def test_ignore_patterns(self):
        def ignored(name):
            return subprocess.run(['git', 'check-ignore', '--no-index', '-q', name], cwd=ROOT).returncode == 0
        for name in ['x.jks', 'x.keystore', 'x.p12', 'x.pfx', '.env', '.env.release', 'signing.properties',
                     'key.properties', 'keystore.properties', 'x-private.pem', 'private_key.pem', 'release-key.pem']:
            self.assertTrue(ignored(name), name)
        for name in ['certificate.pem', 'fixtures/public.pem', '.env.example', 'gradle.properties', 'LICENSE', 'NOTICE']:
            self.assertFalse(ignored(name), name)

    def validate_with(self, tags):
        current = 'applicationId = "com.secreto.tvmanager"\nversionName = "1.0.0"\nversionCode = 1'
        def git(*args):
            if args[0] == 'tag': return '\n'.join(tags)
            tag, path = args[1].split(':', 1)
            if path.endswith('build.gradle.kts'): return tags[tag]
            if 'com.secreto.tvmanager' not in tags[tag]: raise subprocess.CalledProcessError(1, 'git')
            return '# Secreto TV Manager changelog'
        with patch.object(check, 'git', side_effect=git), patch.object(Path, 'read_text', autospec=True, side_effect=lambda *a, **k: current if a[0].name == 'build.gradle.kts' else '## 1.0.0 — prepared'):
            check.validate()

    def test_upstream_tags_do_not_block_first_fork_release(self):
        self.validate_with({'v0.14.0': 'applicationId = "app.tellyscope"\nversionName = "0.14.0"\nversionCode = 26'})

    def test_official_fork_tags_prevent_duplicate_or_downgrade(self):
        with self.assertRaises(ValueError):
            self.validate_with({'v1.0.0': 'applicationId = "com.secreto.tvmanager"\nversionName = "1.0.0"\nversionCode = 1'})

    def test_secret_export_filter(self):
        for name in ['a.jks', 'release.p12', '.env.production', 'private-key.pem', 'signing.properties']:
            self.assertTrue(materials.secret_path(name), name)
        self.assertFalse(materials.secret_path('public-certificate.pem'))

    def test_workflow_has_no_automatic_publication(self):
        workflow = (ROOT / '.github/workflows/release.yml').read_text()
        self.assertIn('workflow_dispatch:', workflow)
        for forbidden in ['  push:', '  release:', 'contents: write', 'gh release', 'git push', 'git tag', 'secrets.', 'Tellyscope-']:
            self.assertNotIn(forbidden, workflow)

if __name__ == '__main__': unittest.main()
