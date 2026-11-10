# Tellyscope v0.16.0 integration into Secreto TV Manager

## Source and scope

- Secreto baseline: `7c75a6e036011f887116c14c07f4af6c2e905e52` (latest published README on main).
- Integration branch: `codex/upstream-v0.16.0`.
- Exact upstream tag: `v0.16.0` at `f8a733a0dc15c2e258331b26dbfd14102671b3f4`.
- Common ancestor: `442330d85a84d8bad0930883693e14a36fe6cd09`.
- Initial working tree was clean. No applicable AGENTS.md files were found in the repository or parent directories.
- No default-branch merge, push, installation, signing-key generation or release publication is part of this update.

The official fork's release inventory contained v1.0.0 only. Its verified APK identifies
`com.secreto.tvmanager`, version 1.0.0, code 1. This development update is **1.1.0 (2)**;
it does not use upstream's 0.16.0 (29). SDK and Gradle/AGP versions remain unchanged.

## Integrated behavior

Includes the changes since the shared v0.14-era ancestor, including upstream v0.15.x:

- Pairing timeout and fresh-port retry, reconnects after wireless port changes, dropped-link handling, VPN/off-Wi-Fi guidance.
- Private DNS validation and rollback; reset and history restore original DNS, animation, app and launcher states.
- Signed data-list verification, monotonic-version checks and protection against unsigned replacement.
- Remote touchpad, installed streaming-app picker, volume behavior, fixed-volume guidance and remote navigation improvements.
- App-store actions, state-aware app management, picker/save fixes and Fire TV launcher/settings/animation compatibility fixes.
- Expanded monitoring and advanced shell. Changing shell commands ask for confirmation; shell commands have no one-tap History undo.

No Secreto Launcher APK download/promotion feature was introduced. Existing upstream
third-party launcher selection and installation remain; these are not Secreto Launcher promotion.

## Deliberate conflict resolutions

- Retained Secreto application ID, icon resources, splash, Ocean Blue theme, Welcome and About branding, developer/social links and RTL D-pad absolute alignment.
- Preserved the fork README layout, original documentation screenshots, contribution guidance and issue templates; only README build instructions were adapted to flavor names.
- Retained original GPL/copyright/license texts and fork legal materials. NOTICE incorporates upstream's additional icon attribution and updated data path.
- Kept the optional GitHub updater at the exact fork `/releases/latest` endpoint, stable-only parsing, 404/no-release state, interval/off switch, bounded metadata, cancellation and release-page-only navigation.
- Adapted the fork updater to upstream's `Updates` interface. Common metadata/constants live in main; network implementation and update UI live in github. Play uses no-updater stubs. No APK URL/install action was added to app updates.
- Legal assets and runtime dependency inventories are generated separately per variant, avoiding the removed unflavored releaseRuntimeClasspath and cross-variant output collisions.
- Kept external RELEASE_* signing configuration. Manual unsigned preparation remains contents:read only, with no automatic publisher. Preparation now uses the GitHub flavor and version-derived metadata/output names, preserving older release materials.
- Excluded upstream's data/issue automation and Play Console registration snippet: neither belongs to this fork's release/account configuration.
- The upstream signed catalog remains an upstream-owned data feed, with its original public verification keys. This does not change the app-update source. No private data-signing key is included.
- Added Arabic/French strings and updated changed arguments/resource types. Arabic plurals include all six categories; French/Spanish include many. Shell command/prompt rows stay LTR inside the otherwise localized UI.

## Regressions and validation method

The unchanged upstream Connector code and its new test initially failed on macOS/Java 25:
`aPortThatDropsTheHandshakeButStillAnswersIsNotAllowed` returned Unreachable("Connection reset")
instead of NotAllowed. The NIO implementation throws a plain IOException, not EOFException
or SocketException. The integration recognizes that exact reset error and retains the
second TCP probe: a vanished port remains Unreachable. Existing EOF/TLS/pairing tests remain enabled.

A diagnostic run also invalidated a running app test worker by rebuilding its shared adb.jar
concurrently. Its ZIP/class-loading errors were discarded; validation was repeated sequentially.
No tests were disabled or production behavior weakened to bypass failures.

Java 21 daemon criteria remain unchanged. Java 25 test workers are selected with:

```sh
./gradlew :app:assembleGithubDebug :app:assemblePlayDebug \
  :app:testGithubDebugUnitTest :adb:test :app:lintGithubDebug :app:lintPlayDebug \
  --init-script scripts/verification/java25-tests.gradle \
  --no-configuration-cache --console=plain
python3 -m unittest discover -s tests/release -v
python3 -m unittest discover -s tools -p 'test_*.py' -v
python3 scripts/release/check_release.py
git diff --check
```

Unsigned release flavor validation additionally unsets RELEASE_KEYSTORE, RELEASE_KEY_ALIAS
and RELEASE_KEY_PASSWORD, then runs assembleGithubRelease and assemblePlayRelease.
No official signing credentials are needed. The existing 4,000,000-byte release budget
and Play release no-GitHub-API checks remain enabled.

## Device-test checklist

Use a test device/emulator: a debug certificate cannot update an official signed installation.
Do not remove an official installation without backing up its settings and obtaining approval.

1. Check first-run Welcome, splash, About/social links, EN/FR/AR layouts, Arabic D-pad directions and touchpad.
2. Discover, authorize, pair and reconnect to Android TV/Google TV; repeat after a port change and Wi-Fi interruption. Verify pairing timeout without destructive actions.
3. Exercise remote arrows/OK/back/home/text/apps and fixed-volume Fire TV behavior.
4. Install/stop/open a disposable test app; transfer a harmless file and save a renamed screenshot/file.
5. On a recoverable test TV only, test DNS rejection/rollback, reversible debloat, launcher changes and history restore. Avoid factory reset or system-app removal on production TVs.
6. Check advanced monitoring, read-only shell commands, confirmation cancellation and fork-only update/settings links.

Device tests have not been performed by this integration. Previous v1.0.0 device evidence
is retained in DEVICE_TEST_EVIDENCE.md and does not prove the new version's behavior.

## Final local results (2026-11-10)

| Check | Result |
| --- | --- |
| Full GitHub app suite | 844 tests, zero failures/errors/skips |
| Full ADB suite | 354 tests, zero failures/errors/skips |
| Focused final-resource/UI suite | 62 tests, zero failures/errors/skips |
| Release-preparation tests | 8 passed |
| Upstream data/signature tooling tests | 16 passed |
| Fork version/release gate | Passed, only the official fork v1.0.0 tag considered |
| GitHub and Play debug APKs | Build passed |
| GitHub and Play unsigned release APKs | R8/resource shrinking passed |
| Play release AAB | Build passed |
| Play APK and AAB GitHub API exclusion | Both passed |
| GitHub debug lint | Zero errors; 63 warnings |
| Play debug lint | Zero errors; 71 warnings |
| Working/staged whitespace checks | Passed |

The full suite preceded final plural-category/literal-format refinements. The subsequent
62-test focused run covered all language resources, updater/project actions, About,
physical RTL D-pad direction and EN/AR/FR shell rendering. Both APK flavors, both unsigned
release APKs, Play AAB and lint were rebuilt/rechecked against the final source.
Arabic/French shell renders were also visually inspected; these are simulated UI renders,
not new physical-device screenshots.

Unsigned sizes: GitHub **3,905,422 bytes**, Play **3,878,786 bytes**. Both fit the unchanged
4,000,000-byte budget. Final signed size must be measured again when a future release is approved.
The Play AAB task is named signPlayReleaseBundle internally, but release signing variables
were explicitly unset and no official key was supplied.

### Debug device-testing artifact

`app/build/outputs/apk/github/debug/app-github-debug.apk`

- Application ID: `com.secreto.tvmanager`.
- Version: `1.1.0`, code `2`; minSdk 26 / targetSdk 37.
- Size: 20,913,925 bytes (debug tooling/unshrunk build).
- SHA-256: `0a22fb5688d6f60c994ba8ef138982a7a7a7bda85b4c0a83dcc8d6375b727d9c`.
- Android apksigner verification passed; this is a debug-signed build, not an official release.

The original final signed v1.0.0 APK remains byte-for-byte unchanged:
`f942f5fbe9731656ca4e944d0ce18761db8cf864a7ae7df063c787e164954e23`.
The original v1.0.0 tag, main branch, signing configuration and published assets were not changed.

### Remaining limitations

- Device testing of this integration is still required. Existing fake-device, Robolectric and socket tests cannot establish OEM/Fire TV hardware compatibility.
- Lint warnings remain (unused resources, deprecations/version hints, icon/vector/layout advice and dependency TLS trust-manager findings). The TLS findings are in unchanged Kadb/Bouncy Castle artifacts; this integration does not claim a new security audit or suppress those warnings.
- No failing automated tests remain. The initially observed upstream/macOS reset-classification failure was fixed as documented above.
- A future signed release requires separate review, signing/size verification and corresponding-source packaging for that exact binary. The old v1.0.0 archives are historical and must not be presented as source for 1.1.0.
- The merge is resolved and staged but intentionally uncommitted for review. MERGE_HEAD retains the exact upstream tag commit so the eventual merge commit will record both histories. Do not abort/reset the merge if keeping these reviewed changes.

Local evidence and the full changed-file manifest are under `app/build/upstream-v0.16-review/`.
The reproducible Java 25 test init script is `scripts/verification/java25-tests.gradle`;
it does not change daemon criteria or shell defaults.
