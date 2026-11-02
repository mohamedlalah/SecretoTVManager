# Final release audit — Secreto TV Manager 1.0.0

Current follow-up: see [RELEASE_BLOCKERS_RESOLUTION.md](RELEASE_BLOCKERS_RESOLUTION.md). The native gap and fresh local source bundle are now resolved; Java25 verification now PASS (997/997; see JAVA25_VERIFICATION.md); live remote publishing/source readiness remains BLOCKED. This document below is the earlier audit record.

Audit date: 2026-11-02. Branch: secreto-branding-v1. Outcome: **signed artifact verified; publication held for source-completeness closure and validation review**.

No APK build/signing, application/configuration modification, installation, commit, push, tag, GitHub Release or publication was performed. Changes in this audit are documentation and two copied screenshot assets only. No keystore or password was opened/requested. This report supersedes earlier reports' “not yet signed” statements; those reports remain historical preparation records.

## 1. Artifact identity and integrity

| Check | Verified result |
|---|---|
| Artifact | app/build/outputs/apk/release/Secreto-TV-Manager-1.0.0.apk |
| APK SHA-256 | f942f5fbe9731656ca4e944d0ce18761db8cf864a7ae7df063c787e164954e23 |
| Signing certificate SHA-256 | 83d1c238040cc3dca9dbb3fe71ad5468a95eb4aaa09654f6bb7107d36c06ef61 |
| apksigner | Verifies; v2 true, v3 true; one RSA-4096 signer |
| Other schemes | v1/v3.1/v3.2/v4 false; not required for this artifact's stated v2/v3 verification |
| Application ID / version / code | com.secreto.tvmanager / 1.0.0 / 1 |
| Size / enforced ceiling | 3,742,065 / 4,000,000 bytes; 257,935 bytes headroom |
| Developer physical test | User reports this signed APK tested on Samsung Galaxy A15; no detailed TV/path coverage log supplied |

The final APK and Gradle-produced app-release.apk have identical ZIP entry contents outside META-INF. This supports the final re-signing workflow without a changed executable/resource payload; it is not an independent source rebuild. Signature/fingerprint verification is on the exact supplied artifact, not a replacement build.

## 2. GPL and third-party notices

- Root LICENSE is the full GNU GPL v3 text; project headers specify GPL-3.0-only. LICENSE, NOTICE and original CHANGELOG.md are byte-for-byte unchanged from HEAD.
- The signed APK contains 60 legal asset entries. Bundled LICENSE and NOTICE exactly match the root files; bundled SOURCE_AVAILABILITY.md and RELEASE_NOTICE.md exactly match their current source counterparts.
- Original Copyright (C) 2026 Tellyscope contributors, original project URLs, Kadb/Dadb Apache-2.0 attribution, spake2-java GPL-3.0 and cleanyourtv MIT terms are retained. Fork attribution does not claim ownership of original code.
- Full Apache-2.0 text and Elisabeth MIT/BSD terms are included. Original Bouncy Castle license texts excluded as duplicate META-INF packaging resources are preserved per module under assets/legal/dependency-notices instead. This does not remove their obligations.
- The original NOTICE's statement about full third-party texts is supplemented by actual legal assets and release/licenses. It should not be interpreted as proof that every transitive/native component's notice is complete: that review remains open.
- Service logos and other artwork require applicable rights/attribution review. Root NOTICE preserves downloaded-logo provenance; documentation fonts have docs/site/OFL.txt. Added Secreto artwork/service-mark provenance was not independently authenticated in this audit.

GPL LICENSE section 1 defines Corresponding Source to include necessary source and scripts, including relevant shared-library source, with specified System Library/tool exceptions. Section 6(d) permits network binary distribution with equivalent source access and clear source directions. The planned GitHub release should provide exact source archives and build instructions next to the APK. Merely linking the project homepage does not demonstrate complete delivery. The official private signing key is not to be published; no assertion is made here about a hypothetical locked User Product distribution.

## 3. Source completeness — concrete blocker

The existing review package's dependency inventory contains 82 populated entries. That is an inventory-completeness result, **not corresponding-source completeness**.

Concrete gap: the signed APK includes libandroidx.graphics.path.so for arm64-v8a, armeabi-v7a, x86 and x86_64. The archived androidx.graphics:graphics-path:1.0.1 source JAR contains Kotlin interfaces/implementation files but **zero .c/.cc/.cpp/.h native source files** and no complete native build closure. The library is shipped in the app rather than obtained as a platform System Library. Its exact native source revision, build scripts and required subcomponents/notices must be identified and supplied or otherwise made equivalently accessible with durable, exact directions before declaring source completeness. Do not substitute a current branch snapshot and call it exact release source.

Other runtime source JARs likewise need inspection for generated/native code, required build inputs and bundled subprojects. Matching group/module/version and source-file presence do not prove these obligations are satisfied. Two inspected zero-class compatibility artifacts map to matching core/common implementation source archives; that mapping does not close native-library gaps elsewhere.

Before this audit, all 281 project files in the old PROVENANCE.json matched the checkout, with the recorded base commit matching HEAD. Thus the local project snapshot covered the uncommitted app/resources/Gradle changes at audit entry. It remains local/dirty provenance, not an independently reproduced build or a public immutable release-source commit. Since this audit sanitizes documentation and adds publication documents/screenshots, that source archive is now stale. It also contains earlier local path references and must not be uploaded as-is.

The app-specific build inputs are present: gradlew/gradlew.bat, wrapper JAR/properties/checksum, settings and root/module Gradle scripts, version catalog, app/ADB/CLI/helper sources, resource/icon assets, R8 rules, release scripts and legal inputs. Eight language resources are present. Tests and fixtures are retained. No clean/offline reproduction was attempted and no APK was rebuilt. Dependency locking/verification is not a proven bit-for-bit reproduction guarantee in this checkout.

## 4. Secrets, local information and repository hygiene

The current tracked plus non-ignored candidate content was checked for full PEM private-key blocks, common GitHub/AWS token forms, secret-style paths and user-specific local filesystem references. No actual key/token finding was produced. Tracked signing stores, .env/local/signing/key properties and staged signing files were absent; the Git index remains unchanged. The external keystore is outside the repository and was not read.

Seven earlier verification/preparation documents contained user-specific absolute paths. Those were sanitized to project-relative links/placeholders; local-only preview links were removed from publication content. The new README references two deliberately copied UI-test screenshots in docs/screenshots, with clear simulated-render captions. No user-specific local path remains in the scanned publication candidates.

Ignore rules exclude Gradle/build/IDE outputs, SDK local properties, common signing stores (including case variants), signing/key properties, .env variants and conventional private-PEM filenames. Public certificates and example configuration files are intentionally not broadly excluded. Ignore rules are not a universal secret detector: arbitrary secret filenames, full history, remote secrets/settings and hidden external integrations were not exhaustively audited. Use a full-history secret scanner before publishing an unknown/inherited history; never paste its secret values into reports. The scoped checks here are not certification of every Git object.

Do not publish raw daemon/Gradle logs, device reports, pairing codes, app data, local.properties, .idea, .gradle, signing files, or app/build/release-materials as-is. Historical ZIP/source bundles may still contain the old local paths even though the source documents are now sanitized.

## 5. Kadb Java 21/25 issue — evidence and limits

1. The retained full-suite XML records 701 tests: 700 passed and one failed (LastKnownTest.offlineTvKeepsItsFactsAcrossARestartUntilForget). The failure is UnsupportedClassVersionError for AdbPairAuthException: class version69, JVM supports up to65.
2. A fresh isolated host class-loading probe on Java21.0.10 reproduces that same failure independently of Gradle/Robolectric.
3. Both cached kadb-jvm and kadb-android 2.1.5 artifacts contain that exception class with major69 (Java25). Source/target17 for the app does not change dependency class versions.
4. The signed APK contains classes.dex in DEX038 format and the Kadb exception descriptor. It does not ask the host Java21 VM to load that raw Java class. Android uses DEX class definitions: [Android DEX format](https://source.android.com/docs/core/runtime/dex-format).
5. The project's Connector references/catches this exception in connection and pairing paths. A successful package/signature check or app launch does not exercise every such path, TLS provider, certificate generation or API compatibility case.
6. gradle-daemon-jvm.properties selects21. Actions setup-java selects25, but this alone may not select25 for the Gradle/test process. Gradle documents daemon criteria precedence over JAVA_HOME/org.gradle.java.home: [Gradle daemon criteria](https://docs.gradle.org/current/userguide/gradle_daemon.html#sec:daemon_jvm_criteria).

Conclusion: **the observed failure is a proven host-JVM bytecode/runtime mismatch. An equivalent Android class-version failure is not demonstrated. Blanket “harmless in production” is also not demonstrated.** The user-reported Galaxy A15 test is positive device evidence but lacks specified TV/pairing coverage. A Java25 rerun and documented non-destructive real-TV connection/pairing checks remain validation gates before broad release-functionality claims. No suitable Java25 runtime was found in the inspected standard/Gradle-managed locations, so no Java25 rerun is claimed.

## 6. GitHub Actions and remote readiness

All three local workflows were reviewed:
- ci.yml builds/tests/lints and uploads failure reports; contents:read, no release writing.
- release-check.yml checks the fork gate on PR/manual invocation; contents:read, no publishing.
- release.yml is workflow_dispatch only, builds unsigned review materials and uploads an Actions artifact; no release creation/replacement, git tag/push, contents:write or signing-secret references.

No inherited automatic Tellyscope publishing command remains in these local workflow files. Fork gate tests pass with inherited upstream tags excluded; original upstream history remains. This is a local-file audit. Repository visibility, remote Actions settings, previously committed workflow versions, protected environments, GitHub Pages and external integrations were not authenticated remotely. The public repository fetch was unavailable in this session; public source availability is therefore still a pre-publication check. Do not assume local edits protect the remote until the reviewed workflow changes are actually committed/pushed under separate approval.

## 7. Changes and validation in this audit

- Professional fork-first README with supported phone requirements, OEM/TV caveats, features, simulated screenshots, installation/checksum/signature information, connection authorization, debloat/launcher/DNS/system-setting cautions, official links and original attribution.
- Prepared release/RELEASE_NOTES_1.0.0.md with exact signed artifact metadata and explicit unresolved publication prerequisites.
- Sanitized seven historical local-path-bearing reports; original legal texts were not changed.
- Added this report, PUBLICATION_CHECKLIST.md and the explicit release commit candidate manifest.
- Copied two existing rendered screenshots into public documentation assets; no application asset was replaced.
- Current Python release-preparation regression tests: **5 passed**. No app tests or builds rerun because this audit changes documentation only and preserves the supplied signed artifact. The prior 701-test result is historical evidence, not a new run.
- git diff --check: passed. Signed APK checksum/certificate were checked without modification; final hash is rechecked at completion.

### Exact files changed by this audit (14)

- `README.md`
- `docs/ABOUT_LAYOUT_VERIFICATION.md`
- `docs/BRANDING_VERIFICATION.md`
- `docs/DEVELOPER_BRANDING_VERIFICATION.md`
- `docs/FINAL_RELEASE_AUDIT.md`
- `docs/PUBLICATION_CHECKLIST.md`
- `docs/RELEASE_COMMIT_FILES.txt`
- `docs/RELEASE_PREPARATION.md`
- `docs/SOCIAL_BRANDING_VERIFICATION.md`
- `docs/UPDATE_SYSTEM_VERIFICATION.md`
- `docs/YOUTUBE_CHANNEL_VERIFICATION.md`
- `docs/screenshots/secreto-about.png`
- `docs/screenshots/secreto-welcome.png`
- `release/RELEASE_NOTES_1.0.0.md`

The full release commit candidate manifest contains 79 paths, including earlier work. Eight candidate paths are new/modified icon assets; none were removed. This list does not include generated outputs or the external signing store.

## 8. Release commit set and remaining blockers

[RELEASE_COMMIT_FILES.txt](RELEASE_COMMIT_FILES.txt) lists every modified/new non-ignored file proposed for this release, including earlier branding/update changes, all newly added icons/translations, tests, build/release/legal files and documentation. Existing unchanged source files remain part of Git and need no restaging. Generated APK/source bundles, caches, IDE/SDK settings and the private keystore are excluded. No files were staged.

Blocking publication items:
1. Exact native graphics-path corresponding-source/build closure and remaining dependency/source/notice completeness review.
2. Fresh sanitized source/provenance bundle linked to a reviewed immutable source commit, with equivalent public download access established at release time. Current unsigned-review bundle must not be uploaded unchanged.
3. Java25 affected-test rerun and explicit real-TV ADB/pairing validation sign-off; do not dismiss the existing failure without evidence.
4. Confirm remote workflow/settings safety, source visibility and artwork/third-party notice rights before publication.

The signed APK itself passes the requested integrity, signature, identity and size checks. Preserve it byte-for-byte while resolving documentation/source delivery. Follow [PUBLICATION_CHECKLIST.md](PUBLICATION_CHECKLIST.md) for the ordered future steps; each commit/push/tag/release operation still requires separate authorization.
