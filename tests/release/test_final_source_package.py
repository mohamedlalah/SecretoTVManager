import importlib.util
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location('final_source', ROOT / 'scripts/release/package_final_signed_source.py')
package = importlib.util.module_from_spec(spec)
spec.loader.exec_module(package)

class FinalSourceTests(unittest.TestCase):
    def test_secrets_and_generated_outputs_are_excluded(self):
        for name in ['release.jks', 'release.P12', '.env.release', 'local.properties', 'signing-production.properties',
                     'key.properties', 'private-key.pem', 'build/generated/file.txt', '.git/config', 'app/build/apk.apk']:
            self.assertFalse(package.allowed(name), name)

    def test_public_inputs_remain_included(self):
        for name in ['LICENSE', 'NOTICE', 'app/src/main/res/values/strings.xml', 'gradle/wrapper/gradle-wrapper.jar',
                     'public-certificate.pem', '.env.example', 'scripts/release/check_release.py']:
            self.assertTrue(package.allowed(name), name)

    def test_private_key_and_local_home_bytes_are_rejected_without_exposing_content(self):
        with self.assertRaises(RuntimeError):
            package.inspect(str(Path.home()).encode() + b'/private-file', 'sample')
        marker = b'-----BEGIN ' + b'PRIVATE KEY-----\n'
        end = b'-----END ' + b'PRIVATE KEY-----\n'
        with self.assertRaises(RuntimeError):
            package.inspect(marker + b'QUFBQUFBQUFBQUFBQUFBQUFB\n' + end, 'sample')
        package.inspect(b'ordinary GPL source; no credential contents', 'sample')

if __name__ == '__main__': unittest.main()
