#!/usr/bin/env python3
"""Read-only gate: compare only stable tags that identify this fork, never upstream tags."""
import re
import subprocess
from pathlib import Path

APP_ID = 'com.secreto.tvmanager'


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
            previous.append((version(old_name), old_code))
    if previous:
        if current <= max(v for v, _ in previous) or code <= max(c for _, c in previous):
            raise ValueError('Version and code must advance past official fork tags')
    elif (name, code) != ('1.0.0', 1):
        raise ValueError('First official fork release must be 1.0.0 (1)')
    print(f'Secreto TV Manager {name} ({code}): gate passed; {len(previous)} official fork tags considered')


if __name__ == '__main__':
    try:
        validate()
    except (ValueError, subprocess.CalledProcessError) as error:
        raise SystemExit(str(error))
