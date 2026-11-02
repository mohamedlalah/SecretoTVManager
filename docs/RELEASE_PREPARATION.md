# Secreto TV Manager v1.0.0 release preparation

Prepared 2026-11-02, branch `secreto-branding-v1`, for review only. No keys generated, passwords requested/exposed, device operations, commits, pushes, tags, releases or publications occurred.

## Identity and preserved scope

- applicationId `com.secreto.tvmanager`; versionName `1.0.0`; versionCode `1` — unchanged.
- minSdk 26, target/compile SDK 37, AGP 9.4.1, Gradle 9.8.2 — unchanged.
- Existing external `RELEASE_KEYSTORE`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` signing configuration is unchanged. No credentials were added. Local variables are unset; release has no debug-signing fallback.
- Ocean Blue, eight languages, social links, About, Welcome, Splash, Settings, updater and all ADB/TV feature implementations were preserved. Earlier uncommitted work remains.
- Root LICENSE, NOTICE and original CHANGELOG.md are byte-for-byte preserved.

## Publishing configuration and release gate

`.github/workflows/release.yml` now only responds to workflow_dispatch. It builds unsigned review materials and uploads a temporary Actions artifact. There are no push/tag triggers, signing secret references, write permissions, git tag/push or GitHub release creation commands. This workflow was not run remotely.

Secreto TV Manager naming replaces inherited Tellyscope artifact/release naming in publishing configuration. The current artifact name is version-specific to 1.0.0. A later release must review/update that naming and version policy.

`check_release.py` reads the fork changelog and examines only stable v* tags with both the exact fork application ID and Secreto changelog marker. Upstream tags stay intact and are excluded. The current checkout passes as first official 1.0.0 (1), with zero fork tags; inherited v0.14.0/code26 does not block it. Future fork versions/codes must increase. The inherited dev-only merge restriction and upstream-changelog comparison were replaced by this fork-specific gate.

## Changelog

SECRETO_CHANGELOG.md has a dedicated `1.0.0 — prepared, not published` section with code/test-supported fork changes and explicitly retained inherited TV tools. It does not claim physical TV validation or publication. Original upstream history/attribution remain separate.

## License and source materials

The unsigned APK contains **60 legal asset entries** under assets/legal/: verbatim root LICENSE/NOTICE, full Apache-2.0 text, fork distribution/source information, resolved runtime inventory, extracted JAR/AAR notices (including nested classes.jar notices), and exact-tag Elisabeth MIT/BSD texts. Bouncy Castle META-INF exclusions no longer silently discard their texts: each module's original LICENSE is copied into legal assets before packaging. Original GPL and MIT notices were not rewritten.

The full Apache-2.0 license text was copied verbatim from a cached dependency's standard license. Elisabeth license texts came from each project's v0.1.0 LICENSE, including curve25519-dalek's BSD conditions. Applicable upstream terms remain their owners' terms; the GPL does not replace them. Apache distribution conditions include retaining applicable notices and providing a license copy: https://www.apache.org/licenses/LICENSE-2.0.txt .

`prepare_materials.py` creates `app/build/release-materials/` with:
- Secreto-TV-Manager-1.0.0-UNSIGNED.apk;
- Secreto-TV-Manager-1.0.0-project-source.tar.gz, including this checkout's uncommitted source, tests, Gradle/build/release scripts and legal materials;
- legal/ copied from the actual APK, including runtime-dependencies.txt;
- exact-version dependency source archives, DEPENDENCY_SOURCE_INVENTORY.json and COMPATIBILITY_SOURCE_MAPPING.json;
- PROVENANCE.json containing base commit, dirty state and per-project-file hashes;
- SHA256SUMS covering generated deliverables (excluding the checksum file itself).

The inventory covers 82 resolved external components with no unresolved entries in this local package. Two audited zero-class compatibility artifacts (core-ktx 1.19.1 and lifecycle-common-java8 2.11.0) map to the matching core/common implementation source archives. The spake2-java 1.1.1 source JAR is separately SHA-256 pinned. Other downloaded sources have recorded checksums, not independently authenticated pins. Private signing files are excluded and full PEM private-key blocks cause export refusal; a harmless redaction-test marker is not mistaken for a real key.

This is a **corresponding-source review package, not a legal-completeness certification**. Review transitive/native code, source/build completeness and upstream notices before distribution. Publish the exact reviewed source and dependency materials beside the binary with equivalent download access, record the immutable official source commit/checksums, and preserve GPL notices. A homepage link or unpublished local checkout alone is insufficient. No source was uploaded by this task.

## Unsigned release build and size budget

1. Initial unchanged release configuration passed R8, resource shrinking, release lint-vital and packaging: 3,497,927 bytes. It had only 2,073 bytes of headroom under 3,500,000.
2. After bundling legal materials, packaging/R8 succeeded but the inherited size finalizer failed at 3,712,701 bytes. Legal completeness must not be sacrificed to meet an upstream size claim.
3. The enforced ceiling was explicitly raised to **4,000,000 bytes**, retaining a strict limit. This is a measured fork budget, not removal of the check.
4. Final `./gradlew :app:assembleRelease`: **BUILD SUCCESSFUL**. Final APK: **3,715,654 bytes** (3.716 MB; 3.544 MiB), with 284,346 bytes remaining. Signing overhead must still be checked against this ceiling.

Exact APK: `<project>/app/build/outputs/apk/release/app-release-unsigned.apk`

SHA-256: `17c61753a23c4efa54c3df5c8fe7bd3fbe3fee784f8c37340365dfc01af7ffd5`

`apksigner verify` reports DOES NOT VERIFY / Missing META-INF/MANIFEST.MF, as expected for this unsigned artifact. It is not installable/distributable as an official signed release. No signing command was executed.

## Validation

- Release-preparation Python tests: **5 passed** (ignore coverage, upstream-tag exclusion, duplicate fork rejection, export filtering, manual-only non-publishing workflow).
- Focused app tests: **28 passed**, including fork changelog, legal assets/Bouncy Castle notices, project links and update parsing/version behavior.
- Existing full app suite: **701 tests, 700 passed, 1 failed, 0 errors/skips**. Sole failure: pre-existing LastKnownTest.offlineTvKeepsItsFactsAcrossARestartUntilForget; Kadb class version69 (Java25) cannot load on test JVM21/version65. No new failing tests. Daemon criteria still pin JDK21; setting JAVA_HOME to25 alone may not override them. Toolchain alignment for full green tests needs separate review; versions were not silently changed.
- `git diff --check`: passed.
- Build warnings include existing deprecated APIs/Gradle features and an unstripped androidx.graphics.path native library; no R8/resource-shrinking failure remains.

## Remaining blockers before distribution

1. Dedicated release keystore/certificate does not yet exist in the audited local setup; signed APK and certificate verification remain pending.
2. Review corresponding-source completeness, dependency/native notices and exact source provenance. Source archives were prepared locally only; equivalent public source availability still must be established at actual release time.
3. Resolve/review the pre-existing Kadb Java25 test-runtime mismatch and rerun affected tests on the intended toolchain.
4. Review the final signed APK's package/version/signature, size, checksums and functionality. No physical-device smoke test was performed by this task.
5. Publication requires a separately reviewed manual procedure; automatic publishing intentionally remains absent.

## Exact secure key-generation next steps (future authorization only)

1. Choose an owner-controlled, access-restricted directory outside this checkout and synced/public folders. Set its directory permissions to 700 and use umask077. Do not create the key inside the project, even though common secret filenames are now ignored.
2. Use trusted JDK keytool interactively. The following is a template **not executed**; choose the external path first:

```sh
keytool -genkeypair -storetype PKCS12 \
  -keystore /SECURE/EXTERNAL/PATH/secreto-tvmanager-release.p12 \
  -alias secretotools-tvmanager-release \
  -keyalg RSA -keysize 4096 -sigalg SHA256withRSA -validity 10000
```

Enter the store password directly in keytool's secure prompt (not chat, source, shell arguments or history); use the same store/key password for the existing configuration. Enter SecretoTools / Mohamed LALAH as appropriate certificate identity metadata. Never reuse the debug or upstream key.

3. Inspect the public certificate with keytool -list -v using the interactive password prompt. Record the public SHA-256 certificate fingerprint and expiration. Keep the private key secret; export only a public certificate if needed.
4. Make at least two encrypted offline backups in separate protected locations, store the password separately in a password manager, and verify restore/readability before using the key. Loss of the signing identity can block compatible updates.
5. Under later authorization, supply existing RELEASE_* environment variables securely without echoing values or adding files to the repository. Build with `--no-configuration-cache`, verify via apksigner --print-certs and verify package/version and size. Current debug-signed installs use a different certificate; do not silently replace them or uninstall them without an explicit migration decision.
6. Only after review: establish exact corresponding-source availability and publish the reviewed signed artifact manually. No such action is authorized or performed here.

Android key custody/signing guidance: https://developer.android.com/studio/publish/app-signing .

## Modified / added files for this task

- [.github/workflows/release-check.yml](../.github/workflows/release-check.yml)
- [.github/workflows/release.yml](../.github/workflows/release.yml)
- [.gitignore](../.gitignore)
- [CONTRIBUTING.md](../CONTRIBUTING.md)
- [README.md](../README.md)
- [SECRETO_CHANGELOG.md](../SECRETO_CHANGELOG.md)
- [app/build.gradle.kts](../app/build.gradle.kts)
- [app/src/test/java/app/tellyscope/ProjectActionsTest.kt](../app/src/test/java/app/tellyscope/ProjectActionsTest.kt)
- [docs/RELEASE_PREPARATION.md](../docs/RELEASE_PREPARATION.md)
- [release/RELEASE_NOTICE.md](../release/RELEASE_NOTICE.md)
- [release/SOURCE_AVAILABILITY.md](../release/SOURCE_AVAILABILITY.md)
- [release/licenses/Apache-2.0.txt](../release/licenses/Apache-2.0.txt)
- [release/licenses/curve25519-elisabeth-0.1.0-LICENSE.txt](../release/licenses/curve25519-elisabeth-0.1.0-LICENSE.txt)
- [release/licenses/ed25519-elisabeth-0.1.0-LICENSE.txt](../release/licenses/ed25519-elisabeth-0.1.0-LICENSE.txt)
- [scripts/release/check_release.py](../scripts/release/check_release.py)
- [scripts/release/prepare_materials.py](../scripts/release/prepare_materials.py)
- [tests/release/test_preparation.py](../tests/release/test_preparation.py)
