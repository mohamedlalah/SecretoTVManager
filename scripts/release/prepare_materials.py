#!/usr/bin/env python3
"""Create local review materials only. Never signs, tags, installs or publishes."""
from concurrent.futures import ThreadPoolExecutor
import argparse
import urllib.request
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tarfile
import zipfile

ROOT = Path(__file__).resolve().parents[2]


def secret_path(path):
    p = Path(path)
    name = p.name.lower()
    return (p.suffix.lower() in {'.jks', '.keystore', '.p12', '.pfx'} or
            name in {'local.properties', 'keystore.properties', 'signing.properties', 'key.properties', '.env'} or
            (name.startswith('.env.') and name not in {'.env.example', '.env.sample'} or name.startswith('signing-') and name.endswith('.properties')) or
            name.endswith('.pem') and any(x in name for x in ['private', 'release-key', 'signing-key', '.key.pem']))


def prepare(fetch_dependency_sources=False):
    os.chdir(ROOT)
    from check_release import identity
    app_id, version_name, version_code = identity((ROOT / 'app/build.gradle.kts').read_text())
    apk = ROOT / 'app/build/outputs/apk/github/release/app-github-release-unsigned.apk'
    if not apk.is_file():
        raise SystemExit('Build the unsigned release first; signed APKs are not accepted here')
    metadata = json.loads((apk.parent / 'output-metadata.json').read_text())
    if metadata.get('applicationId') != app_id or any(
            (e.get('versionName'), e.get('versionCode')) != (version_name, version_code) for e in metadata.get('elements', [])) or not metadata.get('elements'):
        raise SystemExit('APK metadata does not match the approved release identity/version')
    # Reject archives containing signing certificates; apksigner verification is also required before release.
    with zipfile.ZipFile(apk) as z:
        if any(re.match(r'META-INF/.*\.(RSA|DSA|EC)$', n) for n in z.namelist()):
            raise SystemExit('Unexpected signed APK')
        legal = {n.removeprefix('assets/legal/'): z.read(n) for n in z.namelist() if n.startswith('assets/legal/') and not n.endswith('/')}
    for required in ['LICENSE', 'NOTICE', 'SOURCE_AVAILABILITY.md', 'RELEASE_NOTICE.md', 'runtime-dependencies.txt', 'licenses/Apache-2.0.txt']:
        if required not in legal:
            raise SystemExit('Missing packaged legal material: ' + required)
    cache = Path(os.environ.get('GRADLE_USER_HOME', str(Path.home() / '.gradle'))) / 'caches/modules-2/files-2.1'
    sources = list((cache / 'com.github.Flyfish233/spake2-java/1.1.1').glob('*/*-sources.jar'))
    expected_source_hash = 'd4bc25723647f7b357c4115a3caced4f877e64c068ce9b0eef37676dc7645b45'
    if sources:
        source_data = sources[0].read_bytes()
    elif fetch_dependency_sources:
        url = 'https://jitpack.io/com/github/Flyfish233/spake2-java/1.1.1/spake2-java-1.1.1-sources.jar'
        with urllib.request.urlopen(url, timeout=60) as response:
            source_data = response.read(5_000_001)
    else:
        raise SystemExit('Missing exact spake2-java source; use --fetch-dependency-sources to fetch the pinned source JAR')
    if hashlib.sha256(source_data).hexdigest() != expected_source_hash:
        raise SystemExit('Dependency source checksum mismatch; do not distribute until reviewed')
    files = subprocess.check_output(['git', 'ls-files', '-co', '--exclude-standard', '-z']).decode().split('\0')
    snapshot = []
    for name in sorted(set(files) - {''}):
        p = ROOT / name
        if secret_path(name) or not p.is_file():
            continue
        if p.is_symlink():
            raise SystemExit('Review source symlink before export: ' + name)
        data = p.read_bytes()
        if re.search(rb'(?m)^-----BEGIN (?:RSA |EC |OPENSSH |ENCRYPTED )?PRIVATE KEY-----\r?\n(?:[A-Za-z0-9+/=]{16,}\r?\n)+-----END (?:RSA |EC |OPENSSH |ENCRYPTED )?PRIVATE KEY-----', data):
            raise SystemExit('Private key detected; refusing source export (contents withheld)')
        snapshot.append((name, hashlib.sha256(data).hexdigest()))
    out = ROOT / ('app/build/release-materials-' + version_name)
    if out.exists():
        shutil.rmtree(out)  # dedicated generated output only
    out.mkdir(parents=True)
    shutil.copy2(apk, out / f'Secreto-TV-Manager-{version_name}-UNSIGNED.apk')
    with tarfile.open(out / f'Secreto-TV-Manager-{version_name}-project-source.tar.gz', 'w:gz') as tar:
        for name, _ in snapshot:
            tar.add(ROOT / name, arcname=f'SecretoTVManager-{version_name}/' + name, recursive=False)
    for name, data in legal.items():
        target = out / 'legal' / name
        if '..' in Path(name).parts or Path(name).is_absolute():
            raise SystemExit('Unsafe legal entry')
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    dep = out / 'dependency-source'
    dep.mkdir()
    (dep / 'spake2-java-1.1.1-sources.jar').write_bytes(source_data)
    source_inventory = {}
    download_cache = ROOT / 'app/build/release-source-cache'
    download_cache.mkdir(parents=True, exist_ok=True)
    def dependency_source(coordinate):
        group, module, version = coordinate.split(':')
        filename = coordinate.replace(':', '_') + '-sources.jar'
        jars = sorted((cache / group / module / version).glob('*/*-sources.jar'))
        target_cache = download_cache / filename
        candidate = jars[0] if jars else target_cache
        if not candidate.is_file() and fetch_dependency_sources:
            repository = 'https://dl.google.com/dl/android/maven2/' if group.startswith(('androidx.', 'com.android.tools.')) else 'https://repo.maven.apache.org/maven2/'
            if group.startswith('com.github.'):
                repository = 'https://jitpack.io/'
            url = repository + group.replace('.', '/') + '/' + module + '/' + version + '/' + module + '-' + version + '-sources.jar'
            try:
                with urllib.request.urlopen(url, timeout=20) as response:
                    data = response.read(20_000_001)
                import io
                with zipfile.ZipFile(io.BytesIO(data)) as source_zip:
                    if not any(n.endswith(('.java', '.kt', '.c', '.h', '.cpp')) for n in source_zip.namelist()):
                        raise ValueError('No recognizable source files')
                target_cache.write_bytes(data)
                candidate = target_cache
            except Exception:
                return coordinate, None  # recorded as a blocker; never claim a substitute is corresponding source
        if candidate.is_file():
            shutil.copy2(candidate, dep / filename)
            return coordinate, filename
        return coordinate, None
    coordinates = [c for c in sorted(set(legal['runtime-dependencies.txt'].decode().splitlines())) if len(c.split(':')) == 3]
    with ThreadPoolExecutor(max_workers=6) as pool:
        for coordinate, filename in pool.map(dependency_source, coordinates):
            source_inventory[coordinate] = filename
    source_inventory['com.github.Flyfish233:spake2-java:1.1.1'] = 'spake2-java-1.1.1-sources.jar'
    compatibility_sources = {
        'androidx.core:core-ktx:1.19.1': 'androidx.core:core:1.19.1',
        'androidx.lifecycle:lifecycle-common-java8:2.11.0': 'androidx.lifecycle:lifecycle-common-jvm:2.11.0',
    }
    # These exact compatibility artifacts were inspected: no .class entries, including nested classes.jar.
    # Their implementation lives in the matching core/common source archive, not an invented version.
    for compatibility, implementation in compatibility_sources.items():
        if source_inventory.get(compatibility) is None:
            source_inventory[compatibility] = source_inventory.get(implementation)
    (out / 'COMPATIBILITY_SOURCE_MAPPING.json').write_text(json.dumps(compatibility_sources, indent=2) + '\n')
    (out / 'DEPENDENCY_SOURCE_INVENTORY.json').write_text(json.dumps(source_inventory, indent=2) + '\n')
    provenance = {
        'status': 'unsigned review only; not published',
        'commit': subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip(),
        'dirty': bool(subprocess.check_output(['git', 'status', '--porcelain'])),
        'applicationId': 'com.secreto.tvmanager', 'versionName': version_name, 'versionCode': version_code,
        'projectSourceFiles': dict(snapshot),
        'dependencySourceReview': 'See legal/SOURCE_AVAILABILITY.md; full transitive-source review remains required',
    }
    (out / 'PROVENANCE.json').write_text(json.dumps(provenance, indent=2) + '\n')
    checksums = []
    for p in sorted(out.rglob('*')):
        if p.is_file():
            checksums.append(hashlib.sha256(p.read_bytes()).hexdigest() + '  ' + p.relative_to(out).as_posix())
    (out / 'SHA256SUMS').write_text('\n'.join(checksums) + '\n')
    print('Dependency source archive entries still missing:', sum(v is None for v in source_inventory.values()))
    print('Unsigned review materials prepared in a versioned app/build/release-materials directory; no publication performed')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--fetch-dependency-sources', action='store_true')
    prepare(parser.parse_args().fetch_dependency_sources)
