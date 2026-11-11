#!/usr/bin/env python3
"""Read-only gate: compare only stable tags that identify this fork, never upstream tags."""
import re
import subprocess
from pathlib import Path

APP_ID = 'com.secreto.tvmanager'

# Paths that cannot change the distributed APK or its release/compliance materials. Allowlist only:
# every other path, including unknown ones, needs a version advance (fail closed).
MAINTENANCE_FILES = {
    '.github/workflows/ci.yml',
    '.github/workflows/release-check.yml',
    'scripts/release/check_release.py',
    'scripts/release/docs_only.py',
    'scripts/verification/java25-tests.gradle',
}
TEST_PREFIXES = ('app/src/test/', 'adb/src/test/', 'cli/src/test/', 'tests/release/')


def identity(text):
    values = []
    for key, pattern in [('applicationId', r'"([^"]+)"'), ('versionName', r'"([^"]+)"'), ('versionCode', r'(\d+)')]:
        match = re.search(r'^\s*' + key + r'\s*=\s*' + pattern, text, re.M)
        if not match:
            raise ValueError('Missing ' + key)
        values.append(match.group(1))
    return values[0], values[1], int(values[2])


def version(value):
    if not re.fullmatch(r'\d+\.\d+\.\d+', value):
        raise ValueError('Expected stable major.minor.patch version')
    return tuple(map(int, value.split('.')))


def git(*args):
    return subprocess.check_output(['git', *args], text=True, stderr=subprocess.DEVNULL)


def maintenance_path(path):
    """Documentation, tests and CI/gate maintenance only; anything else counts as a release change."""
    if not path or path.startswith('/') or '\\' in path or '..' in path.split('/'):
        return False
    return (path == 'README.md' or (path.startswith('docs/') and path.endswith('.md')) or
            path in MAINTENANCE_FILES or path.startswith(TEST_PREFIXES) or
            re.fullmatch(r'tools/test_[A-Za-z0-9_]+\.py', path) is not None)


def changed_since(tag):
    """Committed, staged, unstaged and untracked paths since tag; both sides of renames and deletions."""
    paths = git('diff', '--name-only', '--no-renames', tag).splitlines()
    paths += git('ls-files', '--others', '--exclude-standard').splitlines()
    return sorted({p for p in paths if p})


def validate():
    app_id, name, code = identity(Path('app/build.gradle.kts').read_text())
    if app_id != APP_ID:
        raise ValueError('Unexpected application ID')
    current = version(name)
    notes = Path('SECRETO_CHANGELOG.md').read_text()
    if not re.search(r'^## ' + re.escape(name) + r'(?:\s|$)', notes, re.M):
        raise ValueError('Fork changelog is missing the current version')
    previous = []
    for tag in git('tag', '--list', 'v*').splitlines():
        if not re.fullmatch(r'v\d+\.\d+\.\d+', tag):
            continue
        try:
            old_id, old_name, old_code = identity(git('show', tag + ':app/build.gradle.kts'))
            # Both markers are required; the upstream v* history is intentionally preserved.
            fork_notes = git('show', tag + ':SECRETO_CHANGELOG.md')
        except (ValueError, subprocess.CalledProcessError):
            continue
        if old_id == APP_ID and 'Secreto TV Manager' in fork_notes:
            if old_name != tag[1:]:
                raise ValueError('Fork tag/version mismatch: ' + tag)
            previous.append((version(old_name), old_code, tag))
    if previous:
        latest_version, latest_code, latest_tag = max(previous)
        if current == latest_version and code == latest_code and latest_code == max(c for _, c, _ in previous):
            # Same version as the latest official release: allowed only when nothing that ships changed since it.
            release_changes = [p for p in changed_since(latest_tag) if not maintenance_path(p)]
            if release_changes:
                raise ValueError('Version and code must advance past official fork tags; release-affecting changes since '
                                 + latest_tag + ': ' + ', '.join(release_changes[:20]))
            print(f'Secreto TV Manager {name} ({code}): maintenance-only changes since {latest_tag}; '
                  f'no version advance required; {len(previous)} official fork tags considered')
            return
        if current <= latest_version or code <= max(c for _, c, _ in previous):
            raise ValueError('Version and code must advance past official fork tags')
    elif (name, code) != ('1.0.0', 1):
        raise ValueError('First official fork release must be 1.0.0 (1)')
    print(f'Secreto TV Manager {name} ({code}): gate passed; {len(previous)} official fork tags considered')


if __name__ == '__main__':
    try:
        validate()
    except (ValueError, subprocess.CalledProcessError) as error:
        raise SystemExit(str(error))
