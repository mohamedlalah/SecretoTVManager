# Secreto TV Manager 1.0.0 — release blocker follow-up

Current Java25 follow-up: [JAVA25_VERIFICATION.md](JAVA25_VERIFICATION.md). App701 and ADB296 now pass (997/997). The earlier Java21 results below are retained as historical evidence. Only remote publisher/public-source readiness and remote validation of the prepared CI remain open.

Date: 2026-11-02. Branch: secreto-branding-v1. **Publication recommendation: NO-GO.**

The original signed APK is preserved byte-for-byte: SHA-256 f942f5fbe9731656ca4e944d0ce18761db8cf864a7ae7df063c787e164954e23. It still verifies with v2/v3 signatures and certificate SHA-256 83d1c238040cc3dca9dbb3fe71ad5468a95eb4aaa09654f6bb7107d36c06ef61. Identity remains com.secreto.tvmanager / 1.0.0 / code1; size is 3,742,065 bytes. No APK was rebuilt or re-signed. No production code, app assets/languages, SDK/version/signing settings, external key, Git index or remote repository settings were modified. No install, commit, push, tag, release or website deployment occurred.

| Item | Status | Evidence / remaining action |
|---|---|---|
| 1. Native graphics-path origin, license and corresponding source | **PASS** for the identified gap | Published AAR's four .so files exactly match the signed APK. Official release-linked AndroidX revision 8a05a22af450d589ef911d772a001a49dcb05b71 provides native code, CMake/module/build plugin files and license; exact archives are bundled. No native rebuild/bit-identical reproducibility claim. |
| 2. Fresh sanitized final-signed-APK source/provenance package | **PASS (local package)** | app/build/final-signed-release-materials contains the exact signed APK, normalized project source archive, dependency/native source archives, APK-extracted legal files, provenance, component maps and verified SHA256SUMS. It is not yet public or an authorized publication. |
| 3. Kadb Java25-compatible verification | **PASS** | Temurin25.0.5+8-LTS test workers: targeted app1/ADB15 pass; full app701/ADB296 pass. Java21 daemon criteria and production/assertions unchanged. See JAVA25_VERIFICATION.md. |
| 4. Live GitHub release/source readiness | **BLOCKED** | Authenticated inspection succeeded: public repository, default main, 72 tags, no releases/v1.0.0 tag. Live main AND branding branches still contain inherited push-triggered publishing; required local release source/materials are not all present remotely. Settings requiring unsupported endpoints need manual verification. |

## Native source closure

See [NATIVE_GRAPHICS_PATH.md](NATIVE_GRAPHICS_PATH.md) and [NATIVE_GRAPHICS_PATH_PROVENANCE.json](NATIVE_GRAPHICS_PATH_PROVENANCE.json). Source revision was found through the official AndroidX 1.0.1 release changes, not inferred from a current branch. All four main Kotlin files match the published source JAR. All native local includes resolve to the supplied module; remaining headers and ELF dependencies belong to the standard NDK/platform tool/library set. Original AOSP Apache-2.0 headers, the full upstream license, compiler flags, linker script, CMake3.22.1 and NDK25.2.9519653 configuration are included. Original Tellyscope LICENSE/NOTICE are untouched.

Downloaded source is verbatim. The original buildSrc archive's relative symlinks and their build-log/root Gradle targets are retained, not replaced with invented files. The build-context subset is not claimed to be an entire standalone AndroidX source checkout; immutable whole-repository source directions are supplied. No independent native build was performed. The PASS addresses the concrete graphics-path source omission and does not certify every legal issue in every distribution context.

## Fresh source/provenance package

Generator: scripts/release/package_final_signed_source.py. Output: **app/build/final-signed-release-materials/** (ignored generated output, not a Git commit candidate).

Contents:
- Secreto-TV-Manager-1.0.0.apk: byte-for-byte copy of the final signed artifact.
- Secreto-TV-Manager-1.0.0-project-source.tar.gz: all current tracked and non-ignored project inputs, plus this documentation and packaging scripts/tests; no Git internals, build/cache/IDE outputs or signing/local configuration.
- dependency-source/: exact-version source archives carried forward from the earlier review package; no substitute versions.
- native-source/: exact immutable AndroidX module/buildSrc/Gradle archives, original root/build context, published AAR/POM, revision metadata and ELF dependency receipt.
- legal/: verbatim files extracted from this signed APK, including original LICENSE, NOTICE, additional license texts and actual runtime inventory.
- dependency/compatibility mappings, PROVENANCE.json and SHA256SUMS.

The generator verifies original artifact hash/size/v2/v3/certificate before packaging; it compares app/build/resource/legal inputs with the prior recorded pre-signing source snapshot. Current docs are added/sanitized; no independent clean reproduction is claimed. The source archive uses relative names, zero uid/gid/mtime and empty owner names. It is read back and every project file hash is checked. Every final deliverable checksum and the original APK hash are rechecked. Source/JAR/native-archive contents are scanned for actual local home paths, full PEM private-key blocks and common token forms. Signing files, .env/local SDK/signing properties and generated machine outputs are excluded. Upstream source's generic examples/relative links are not rewritten.

The bundle still records a dirty working tree and base commit, because committing is prohibited here. Its local provenance does not establish public availability. Before publication, review/commit the exact source under separate authorization and establish immutable public source/asset links. Re-export if any included source/document changes after final packaging. Keep the old app/build/release-materials private: that unsigned-review bundle remains stale and is not the final signed release package.

## Exact test findings and limits

Runtime search was restricted to standard JDK/IDE/Homebrew and Gradle-managed runtime locations. The IDE JBR is Java21.0.10; no standard Java installation, Homebrew JDK25 or Gradle-managed JDK25 was found. No JDK was downloaded or installed.

Fresh runs, with no assembleRelease task:

```sh
./gradlew :app:testDebugUnitTest --tests '*LastKnownTest' :adb:test --tests '*ConnectorTest' --tests '*PairingPortTest' --no-configuration-cache --no-build-cache --console=plain
./gradlew :adb:test --tests '*ConnectorTest' --tests '*PairingPortTest' --no-configuration-cache --no-build-cache --console=plain
```

- App LastKnownTest: **1 test, 1 failed**, UnsupportedClassVersionError for AdbPairAuthException (major69 requires Java25; host supports65/Java21). The first invocation stops at that failure before its adb test task.
- Separate adb targeted run: **15 tests, 7 passed, 8 failed**, zero errors/skips. All eight failures carry the same AdbPairAuthException class-version69 versus65 cause, before their intended connection/pairing assertions can establish behavior.
- Independent Java21 class-loading probe: same error, confirming the host bytecode/runtime cause without relying on Gradle.
- Release/source-package Python regression suite: **8 passed**, including three new sensitive-file/export-policy tests.
- Packaging integration: signed hash/signature/size, source archive contents/metadata, recorded build-input matches and all deliverable checksums pass.
- git diff --check: passed.

No compatible-JDK run is claimed. Both kadb-jvm and kadb-android artifacts contain major69 class files; the signed APK contains converted DEX instead. This explains the observed host failure mechanism, not universal Android API/TLS compatibility. The failed assertions have not been proved to pass. The user's device evidence is valuable but is not a replacement for those unexecuted assertions.

Next testing step: provide a trusted installed JDK25 and select it explicitly for test JVMs in an isolated verification setup, preserving production inputs and the final APK. The checked-in daemon criteria select21 and override JAVA_HOME; merely setting JAVA_HOME is insufficient. Rerun the exact filtered tests above, record actual test JVM/version and evaluate any remaining failures. Do not change production classes/dependencies or suppress assertions to make the report green.

## Real-device evidence

See [DEVICE_TEST_EVIDENCE.md](DEVICE_TEST_EVIDENCE.md):
- Samsung Galaxy A15: final signed APK installed/working, explicitly identified by developer.
- YOU-BOX: discovery/connection/remote control tested; exact build/hash for these cases not explicitly supplied, so signed-release attribution remains unspecified.
- File transfers, app management, launcher management and TV tweaks: development/debug-build evidence, not individually confirmed on the final signed release.

No device test was repeated by the audit agent. No wireless-pairing/error-recovery/TV-firmware matrix is invented.

## Live repository blocker and safe order of operations

See [LIVE_GITHUB_AUDIT.md](LIVE_GITHUB_AUDIT.md) and its JSON evidence. Authenticated read access worked through the connector even though the local gh CLI token is invalid. No secret values were retrieved.

Do not push/merge/tag expecting the safe local workflow to protect remote main/dev: the inherited publisher is still present there. Under separate authorization, first disable/review the live publisher and verify Actions/Environment/release/tag policies, then commit/push reviewed non-publishing workflows and exact source. Confirm all required files and source bundles are publicly downloadable at immutable identifiers. Only after compatible-JDK test sign-off and separate approval should a manual release be considered. No remote remediation was performed in this task.

## Files and recommendation

Exact changed/new repository files for this follow-up are listed in [RELEASE_BLOCKER_FILES.txt](RELEASE_BLOCKER_FILES.txt). The refreshed full release commit candidate set remains [RELEASE_COMMIT_FILES.txt](RELEASE_COMMIT_FILES.txt). Generated source/provenance/native downloads are under ignored app/build directories, not staged or added to Git.

**NO-GO for publication:** native-source, local packaging and Java25 verification now PASS. The live inherited publisher/public-source state remains BLOCKED; Prepared local CI now selects Java25 through an external temporary launcher; its remote run still needs review. The signed APK's verified physical test does not authorize bypassing those gates. No signing key or production app change is needed to address them.
