#!/usr/bin/env python3
"""Package a verified existing signed APK and sanitized source; never build/sign/publish."""
import hashlib
import io
import json
from pathlib import Path
import re
import shutil
import subprocess
import tarfile
import zipfile

ROOT = Path(__file__).resolve().parents[2]
APK_SHA = 'f942f5fbe9731656ca4e944d0ce18761db8cf864a7ae7df063c787e164954e23'
CERT_SHA = '83d1c238040cc3dca9dbb3fe71ad5468a95eb4aaa09654f6bb7107d36c06ef61'
KEY = re.compile(rb'(?m)^-----BEGIN (?:RSA |EC |OPENSSH |ENCRYPTED )?PRIVATE KEY-----\r?\n(?:[A-Za-z0-9+/=]{16,}\r?\n)+-----END ')
TOKEN = re.compile(rb'(?:gh[pousr]_[A-Za-z0-9]{30,}|github_pat_[A-Za-z0-9_]{40,}|AKIA[A-Z0-9]{16})')


def sha(data):
    return hashlib.sha256(data).hexdigest()


def allowed(name):
    p = Path(name)
    n = p.name.lower()
    return not (set(p.parts) & {'.git', '.gradle', '.idea', '.codex', 'build', '__pycache__'} or
                p.suffix.lower() in {'.jks', '.keystore', '.p12', '.pfx', '.pyc'} or
                n in {'local.properties', 'key.properties', 'signing.properties', 'keystore.properties', '.env'} or
                n.startswith('.env.') and n not in {'.env.example', '.env.sample'} or
                n.startswith('signing-') and n.endswith('.properties') or
                n.endswith('.pem') and any(x in n for x in ['private', 'release-key', 'signing-key', '.key.pem', 'id_rsa', 'id_ecdsa', 'id_ed25519']))


def inspect(data, name):
    if KEY.search(data) or TOKEN.search(data) or str(Path.home()).encode() in data:
        raise RuntimeError('Sensitive material/local home path found; export refused: ' + name)


def inspect_archive(path):
    # Retain upstream relative links verbatim; never extract them into this workspace.
    if path.name.endswith('.tar.gz'):
        with tarfile.open(path) as archive:
            for m in archive:
                if m.name.startswith('/') or '..' in Path(m.name).parts:
                    raise RuntimeError('Unsafe upstream member name')
                if (m.issym() or m.islnk()) and m.linkname.startswith('/'):
                    raise RuntimeError('Absolute upstream symlink')
                if m.isfile():
                    inspect(archive.extractfile(m).read(), path.name + ':' + m.name)
    elif path.suffix == '.jar':
        with zipfile.ZipFile(path) as archive:
            for n in archive.namelist():
                if not n.endswith('/'):
                    inspect(archive.read(n), path.name + ':' + n)


def package():
    apk = ROOT / 'app/build/outputs/apk/release/Secreto-TV-Manager-1.0.0.apk'
    binary = apk.read_bytes()
    if sha(binary) != APK_SHA or len(binary) >= 4_000_000:
        raise RuntimeError('Final signed artifact identity/size mismatch')
    sdk_lines = (ROOT / 'local.properties').read_text().splitlines()
    sdk = Path(next(x.split('=', 1)[1] for x in sdk_lines if x.startswith('sdk.dir=')))
    verify = subprocess.run([str(sdk / 'build-tools/37.0.0/apksigner'), 'verify', '--verbose', '--print-certs', str(apk)], capture_output=True, text=True)
    if verify.returncode or CERT_SHA not in verify.stdout or not all(f'Verified using v{v} scheme (APK Signature Scheme v{v}): true' in verify.stdout for v in [2, 3]):
        raise RuntimeError('Signature verification failed')
    old = ROOT / 'app/build/release-materials'
    native = ROOT / 'app/build/final-release-audit/native'
    native_receipt = json.loads((ROOT / 'docs/NATIVE_GRAPHICS_PATH_PROVENANCE.json').read_text())
    for record in native_receipt['files']:
        if sha((native / record['path']).read_bytes()) != record['sha256']:
            raise RuntimeError('Native source download differs from audited receipt')
    old_checksums = dict((line.split('  ', 1)[1], line.split('  ', 1)[0]) for line in (old / 'SHA256SUMS').read_text().splitlines())
    original = json.loads((old / 'PROVENANCE.json').read_text())
    # Build inputs must still match the recorded pre-signing project snapshot.
    inputs = {n: h for n, h in original['projectSourceFiles'].items() if n.startswith(('app/src/', 'adb/src/', 'cli/src/', 'gradle/')) or n in ['app/build.gradle.kts', 'adb/build.gradle.kts', 'cli/build.gradle.kts', 'settings.gradle.kts', 'build.gradle.kts', 'gradle.properties', 'app/proguard-rules.pro', 'LICENSE', 'NOTICE', 'SECRETO_CHANGELOG.md', 'release/SOURCE_AVAILABILITY.md', 'release/RELEASE_NOTICE.md'] or n.startswith('release/licenses/')}
    for n, h in inputs.items():
        if sha((ROOT / n).read_bytes()) != h:
            raise RuntimeError('Recorded app/build/legal input changed: ' + n)
    names = subprocess.check_output(['git', 'ls-files', '-co', '--exclude-standard', '-z'], cwd=ROOT).decode().split('\0')
    snapshot = {}
    for n in sorted(set(names) - {''}):
        p = ROOT / n
        if not allowed(n) or not p.is_file():
            continue
        if p.is_symlink():
            raise RuntimeError('Review project symlink before export: ' + n)
        data = p.read_bytes()
        inspect(data, n)
        snapshot[n] = sha(data)
    out = ROOT / 'app/build/final-signed-release-materials'
    if out.exists():
        shutil.rmtree(out)  # Only this dedicated generated package; never the original APK.
    out.mkdir(parents=True)
    (out / apk.name).write_bytes(binary)
    with tarfile.open(out / 'Secreto-TV-Manager-1.0.0-project-source.tar.gz', 'w:gz') as archive:
        for n in snapshot:
            data = (ROOT / n).read_bytes()
            info = tarfile.TarInfo('SecretoTVManager-1.0.0/' + n)
            info.size, info.mode, info.mtime = len(data), (0o755 if n in ['gradlew'] else 0o644), 0
            info.uid = info.gid = 0
            info.uname = info.gname = ''
            archive.addfile(info, io.BytesIO(data))
    with zipfile.ZipFile(apk) as archive:
        for n in archive.namelist():
            if n.startswith('assets/legal/') and not n.endswith('/'):
                name = n.removeprefix('assets/legal/')
                if name.startswith('/') or '..' in Path(name).parts:
                    raise RuntimeError('Unsafe legal entry')
                p = out / 'legal' / name
                p.parent.mkdir(parents=True, exist_ok=True)
                data = archive.read(n)
                inspect(data, n)
                p.write_bytes(data)
    for folder, source in [('dependency-source', old / 'dependency-source'), ('native-source', native)]:
        for p in sorted(source.rglob('*')):
            if not p.is_file():
                continue
            inspect_archive(p)
            data = p.read_bytes()
            if folder == 'dependency-source' and sha(data) != old_checksums.get('dependency-source/' + p.relative_to(source).as_posix()):
                raise RuntimeError('Dependency source differs from recorded preparation checksum')
            if p.suffix in {'.json', '.pom', '.txt', '.toml', '.gradle', '.properties'} or p.name == 'gradlew':
                inspect(data, p.name)
            target = out / folder / p.relative_to(source)
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
    for name in ['DEPENDENCY_SOURCE_INVENTORY.json', 'COMPATIBILITY_SOURCE_MAPPING.json']:
        shutil.copy2(old / name, out / name)
    provenance = {
        'status': 'local sanitized source package; publication NOT authorized; see RELEASE_BLOCKERS_RESOLUTION.md',
        'applicationId': 'com.secreto.tvmanager', 'versionName': '1.0.0', 'versionCode': 1,
        'apk': {'filename': apk.name, 'sha256': APK_SHA, 'bytes': len(binary), 'certificate_sha256': CERT_SHA, 'v2': True, 'v3': True},
        'base_commit': subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip(),
        'working_tree_dirty': bool(subprocess.check_output(['git', 'status', '--porcelain'], cwd=ROOT)),
        'project_files_sha256': snapshot, 'recorded_build_inputs_verified': len(inputs),
        'source_relationship': 'app/build/resource/legal inputs match the earlier recorded pre-signing snapshot; documentation added/sanitized; no clean reproduction claimed',
        'archive_metadata': 'relative paths, zero mtime/uid/gid and empty owner names',
        'native_source_revision': '8a05a22af450d589ef911d772a001a49dcb05b71',
        'test_verification': json.loads((ROOT / 'docs/JAVA25_VERIFICATION_RESULTS.json').read_text()),
        'remaining_gates': ['remote verification of prepared CI compatible test-JVM selection', 'remote publisher safety and exact public source availability'],
    }
    (out / 'PROVENANCE.json').write_text(json.dumps(provenance, indent=2) + '\n')
    checksums = []
    for p in sorted(out.rglob('*')):
        if p.is_file():
            checksums.append(sha(p.read_bytes()) + '  ' + p.relative_to(out).as_posix())
    (out / 'SHA256SUMS').write_text('\n'.join(checksums) + '\n')
    # Read the finished archive back rather than relying only on the write path.
    with tarfile.open(out / 'Secreto-TV-Manager-1.0.0-project-source.tar.gz') as archive:
        assert len(archive.getmembers()) == len(snapshot)
        for n, h in snapshot.items():
            m = archive.getmember('SecretoTVManager-1.0.0/' + n)
            assert m.uid == m.gid == m.mtime == 0 and m.uname == m.gname == ''
            assert sha(archive.extractfile(m).read()) == h
    for line in checksums:
        digest, name = line.split('  ', 1)
        assert sha((out / name).read_bytes()) == digest
    assert sha(apk.read_bytes()) == APK_SHA
    print(f'PASS: {len(snapshot)} sanitized project files; {len(checksums)} deliverable hashes verified; original signed APK unchanged')


if __name__ == '__main__':
    package()
