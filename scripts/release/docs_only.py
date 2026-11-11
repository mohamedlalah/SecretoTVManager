#!/usr/bin/env python3
"""Classify a pull request's changed paths: docs-only PRs need no version advance.

Reads paths (one per line) on stdin and prints docs_only=true or docs_only=false.
Only README.md and Markdown under docs/ count as documentation. Anything else,
including docs/ data files used by release scripts, workflows, Gradle files,
release/ notices and SECRETO_CHANGELOG.md, keeps the full release validation.
An empty list is not docs-only. Callers must pass both sides of a rename
(git diff --no-renames) so a file moved out of the app still counts.
"""
import sys


def is_documentation(path):
    path = path.strip()
    if not path or '..' in path.split('/') or path.startswith('/'):
        return False
    return path == 'README.md' or (path.startswith('docs/') and path.endswith('.md'))


def docs_only(paths):
    paths = [p.strip() for p in paths if p.strip()]
    return bool(paths) and all(is_documentation(p) for p in paths)


if __name__ == '__main__':
    print('docs_only=' + ('true' if docs_only(sys.stdin.read().splitlines()) else 'false'))
