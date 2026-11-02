# Secreto TV Manager v1.0.0 — staged publication plan

Status: **HOLD for live GitHub publisher/source readiness**. Temurin25 installation was explicitly approved and completed by the user. The previously blocked tests now pass with isolated Java25 test workers; see JAVA25_VERIFICATION.md for exact current full-suite results. The remote inherited publisher remains a blocker. This plan is not permission to commit, push, tag, publish, change GitHub settings or deploy a site. No such action was performed.

## Phase A: approved installation and isolated verification

This is an Apple Silicon/arm64 Mac with Homebrew available. The earlier inspection found only Android Studio's Java21.0.10; the user has now installed Temurin25.0.5+8-LTS arm64 and it was verified. The checked-in daemon criteria select21. Kadb's class69 bytecode requires a Java25 test JVM; source/target17 does not change that dependency requirement.

Installation command previously approved and completed (reference only; no repeat installation is needed):

```sh
HOMEBREW_NO_AUTO_UPDATE=1 /opt/homebrew/bin/brew install --cask temurin@25
```

This uses the [official Homebrew Temurin25 cask](https://formulae.brew.sh/cask/temurin%4025), which installs the Eclipse Adoptium distribution and selects the host architecture. It installs/registers a system JDK and may require local administrator authorization. No password should be sent in chat. Homebrew auto-update is disabled for this invocation to avoid unrelated updates. No shell startup file, project toolchain file, daemon criteria or signing configuration needs to be edited.

After installation, verify locally:

```sh
/usr/libexec/java_home -V
JDK25=$(/usr/libexec/java_home -v 25)
"$JDK25/bin/java" -version
```

Select Java25 for Test tasks through a temporary external Gradle init script, preserving the Java21 daemon and production build configuration. Example init script (keep outside the repository):

```groovy
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

allprojects {
    tasks.withType(Test).configureEach {
        def tools = project.extensions.getByType(JavaToolchainService)
        javaLauncher.set(tools.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(25))
        })
        doFirst {
            logger.lifecycle("Verification test JVM: Java " + javaLauncher.get().metadata.languageVersion)
        }
    }
}
```

Gradle documents [per-Test JavaLauncher selection](https://docs.gradle.org/current/userguide/toolchains.html#sec:configuring_toolchain_specifications). The isolated override was exercised successfully; both Test tasks printed Java25.0.5+8-LTS. See JAVA25_VERIFICATION.md for all 997 passing tests. Disable auto-download so a missing installation fails rather than silently installing a runtime.

Previously blocked test commands, now verified with Java25 test workers:

```sh
./gradlew :app:testDebugUnitTest --tests '*LastKnownTest' \
  --init-script /path/to/temporary-jdk25-tests.gradle \
  -Dorg.gradle.java.installations.auto-download=false \
  --no-configuration-cache --no-build-cache --console=plain

./gradlew :adb:test --tests '*ConnectorTest' --tests '*PairingPortTest' \
  --init-script /path/to/temporary-jdk25-tests.gradle \
  -Dorg.gradle.java.installations.auto-download=false \
  --no-configuration-cache --no-build-cache --console=plain
```

These are test tasks, not assembleRelease. Recheck the final APK hash afterward. Record any genuine remaining failure instead of suppressing tests. Prior Java21 results: app 1/1 failed; ADB 7/15 passed and 8 failed, all nine failures carrying the class69/JVM65 cause. The independent Java21 probe reproduced the same error. Eight Python preparation/export tests passed. Current targeted Java25 result: app 1/1 and ADB 15/15 passed. Full-suite results are recorded in JAVA25_VERIFICATION.md; earlier Java21 failures below are historical.

## Phase B: verified preparation and live-state discrepancy

At this phase's entry, the fresh source bundle had zero drift: all 88 proposed commit paths existed in its 296-file source manifest and every deliverable checksum matched. Adding this plan requires refreshing the local manifest/source package; that refresh is not a build or publication.

- Origin points to the official mohamedlalah/SecretoTVManager repository.
- Local ci.yml, release-check.yml and release.yml contain no release-writing commands or contents:write permission. Local release.yml is workflow_dispatch-only and produces unsigned review artifacts.
- Authenticated live read confirms public visibility, default main and no releases. Main remains 442330d85a84d8bad0930883693e14a36fe6cd09; remote branding remains 2bf8aa3ef24a9935ba7a5a545ea4039326057fff.
- Live main still has inherited push-triggered Tellyscope publishing commands. Required local source/legal/release-preparation additions are not all public. Public visibility is not proof of exact source availability.
- LICENSE/NOTICE and Tellyscope attribution are preserved. Native graphics-path exact source/build closure is prepared; README, source directions and checksum-bearing release notes are present. Release notes intentionally retain prerequisites until the blockers pass.
- Targeted publication-candidate checks found no actual full private-key blocks, common token forms or actual local home path. Signing stores/credentials/local SDK files and generated materials are excluded. These checks do not replace full-history/remote-setting review.

The signed APK remains com.secreto.tvmanager / 1.0.0 / code1, SHA-256 f942f5fbe9731656ca4e944d0ce18761db8cf864a7ae7df063c787e164954e23. The signing certificate remains 83d1c238040cc3dca9dbb3fe71ad5468a95eb4aaa09654f6bb7107d36c06ef61. Do not rebuild or re-sign it.

## Approval gate 1: live publisher safety (settings change, not yet approved)

Before any publication-related push/merge/tag, the owner must separately approve disabling the inherited live Release workflow and inspecting queued/in-progress runs, Actions token defaults, Environments and tag/release protections. See LIVE_GITHUB_AUDIT.md for exact UI checks. No setting was changed. Do not assume absent secrets or zero observed runs makes the old publisher safe.

The local gh CLI authentication was invalid during the earlier audit; connected GitHub reads work. Do not use a token in command arguments/chat. Any local CLI reauthentication is a separate user-controlled operation. Git pushes may use the user's configured credential helper; never embed a credential in the remote URL.

## Approval gate 2: reviewed local commit (not yet approved)

Prerequisites: compatible tests reviewed, final prerequisites reflected accurately in docs, source/export checks pass, final APK hash unchanged, and explicit approval to stage/commit this exact set. Regenerate docs/RELEASE_COMMIT_FILES.txt if any intended file was added/removed. Inspect every proposed file; no blanket git add . or -f.

Proposed commands, **not executed**:

```sh
cd ~/AndroidStudioProjects/SecretoTVManager
test "$(git branch --show-current)" = secreto-branding-v1
git remote get-url origin
git diff --check
cat docs/RELEASE_COMMIT_FILES.txt

# Run these only after staging/commit approval.
git add --pathspec-from-file=docs/RELEASE_COMMIT_FILES.txt
git diff --cached --check
git diff --cached --stat
git diff --cached
# Review the actual staged content before approving this commit command.
git commit -m "Prepare Secreto TV Manager v1.0.0 release source"
git rev-parse HEAD
```

Stop on unexpected staged files. Never stage ignored app/build output, source bundles, APKs, keystores, .env files, local.properties, SDK/IDE configuration or machine logs. All existing new icon assets and eight languages are in the explicit candidate set. Record the new immutable source commit; committing source does not authorize any push.

After the approved commit, regenerate the ignored signed-source package using the exact committed inputs (it copies the existing APK and does not build it):

```sh
python3 scripts/release/package_final_signed_source.py
```

Verify PROVENANCE.json shows the reviewed commit and clean intended source state; verify SHA256SUMS. If subsequent source/document changes are needed, review them and re-export rather than silently shipping a mismatched package.

## Approval gate 3: push reviewed source branch (public change, not yet approved)

First independently confirm the inherited publisher is disabled/remediated, origin is the official repository and the remote branding head is still the reviewed expected commit. Read-only remote refresh commands below update local tracking only; do not force-push or automatically resolve divergence.

```sh
git fetch origin --no-tags
git log --oneline --left-right origin/secreto-branding-v1...HEAD
git diff --stat origin/secreto-branding-v1...HEAD

# Only after explicit approval of this exact public source push:
git push origin HEAD:refs/heads/secreto-branding-v1
```

If remote changes are unexpected, stop and review. No --force. Re-fetch the remote source at the pushed commit and verify exact app, helper, resource, build, legal, release-script and documentation contents. Do not merge main/dev or create a PR without separate approval for that public action.

## Approval gate 4: reviewed main integration (public change, not yet approved)

Use a reviewed PR from secreto-branding-v1 into main, or another explicitly approved branch strategy. The prepared local release-check no longer requires inherited dev-only integration. Review the actual safe workflows on the proposed merge tree and require appropriate CI results, including documented Java25 test execution. The prepared local CI now creates the same tested temporary launcher in RUNNER_TEMP; verify its actual remote run after an approved push. setup-java25 alone does not override daemon21.

After separate merge approval, confirm default-branch README/source/license/release files and manual-only workflow are actually live. Do not re-enable an inherited publisher. No website deployment is part of this plan. If commit identity/source tree changes during integration, regenerate provenance against the final reviewed source commit while preserving the APK.

## Approval gate 5: release assets/tag/release (separate later approval)

GitHub's automatic source ZIP contains only committed repository files. It does not include ignored final package materials. Attach separately (or provide equally accessible exact immutable downloads with clear directions next to the APK):

1. **Secreto-TV-Manager-1.0.0.apk**, the preserved signed bytes.
2. **SHA256SUMS** for the final assets.
3. **Secreto-TV-Manager-1.0.0-project-source.tar.gz**, plus PROVENANCE.json.
4. **dependency-source/** and **native-source/**, including immutable AndroidX archives/build context, AAR/POM origin evidence, all required dependency source and notices. For a user-friendly release, create a separately checksummed archive of these complete directories without discarding files.
5. **legal/** extracted from the actual signed APK, including LICENSE, NOTICE, licenses, runtime-dependencies.txt and source/release directions.
6. DEPENDENCY_SOURCE_INVENTORY.json and COMPATIBILITY_SOURCE_MAPPING.json, native provenance documentation and final source/build instructions.

The full generated file list and hashes are in app/build/final-signed-release-materials/SHA256SUMS. Keep the old unsigned-review package separate; do not attach it as this signed release. Do not commit binaries/build outputs to the source repository.

After all gates pass, obtain separate approval for a v1.0.0 tag at the final source commit, a draft GitHub Release, final asset uploads, and making the draft public. Update release/RELEASE_NOTES_1.0.0.md with final immutable source links and asset hashes; remove prerequisite text only when actually resolved. Preserve original Tellyscope credits and all third-party terms. Never upload signing keys or passwords. Verify the publicly downloaded APK/source hashes afterward.

## Current decision

**NO-GO until live publisher/public-source readiness and the prepared CI workflow are verified remotely. Java25 verification results are in JAVA25_VERIFICATION.md.** System installation approval is distinct from commit, push, settings, merge, tag or publication approval. None is inferred from another.
