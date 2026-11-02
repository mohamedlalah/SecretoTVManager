# Java 25 release verification — Secreto TV Manager 1.0.0

Date: 2026-11-02. Branch: secreto-branding-v1.

## Runtime and isolation

The user approved Eclipse Temurin25 installation and completed Homebrew's local administrator-authorized installation. Verified runtime: **Temurin OpenJDK25.0.5+8-LTS, arm64, Eclipse Adoptium**. No administrator password was requested in chat, recorded or provided to the audit agent.

Gradle version output confirms:
- Launcher JVM: JetBrains21.0.10.
- Daemon criteria: compatible with Java21, from gradle/gradle-daemon-jvm.properties.
- Actual Test workers: **Java25 / 25.0.5+8-LTS**, printed by both app/ADB Test tasks.

The checked-in daemon criteria, Java source/target, app/dependency versions, signing settings, applicationId and versionName/versionCode were not changed. Only an external temporary init script selected Test.javaLauncher. No assembleRelease, signing task or device operation was invoked.

## Exact temporary override

The following script was used outside the checkout. It is reproduced here for review/repeatability, not added to production Gradle configuration:

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
            logger.lifecycle("Verification test JVM: Java " + javaLauncher.get().metadata.languageVersion + " / " + javaLauncher.get().metadata.jvmVersion)
        }
    }
}
```

Commands executed (temporary script location shown as a portable placeholder):

```sh
./gradlew :app:testDebugUnitTest --tests '*LastKnownTest' \
  :adb:test --tests '*ConnectorTest' --tests '*PairingPortTest' \
  --init-script <temporary-jdk25-tests.gradle> \
  -Dorg.gradle.java.installations.auto-download=false \
  --no-configuration-cache --no-build-cache --continue --console=plain

./gradlew :app:testDebugUnitTest :adb:test \
  --init-script <temporary-jdk25-tests.gradle> \
  -Dorg.gradle.java.installations.auto-download=false \
  --no-configuration-cache --no-build-cache --continue --console=plain
```

## Results

Targeted run: BUILD SUCCESSFUL.

| Suite | Tests | Passed | Failed | Errors | Skipped |
|---|---:|---:|---:|---:|---:|
| App LastKnownTest | 1 | 1 | 0 | 0 | 0 |
| ADB ConnectorTest + PairingPortTest | 15 | 15 | 0 | 0 | 0 |

Full run: **BUILD SUCCESSFUL in 2m32s**.

| Full suite | Tests | Passed | Failed | Errors | Skipped |
|---|---:|---:|---:|---:|---:|
| App | 701 | 701 | 0 | 0 | 0 |
| ADB | 296 | 296 | 0 | 0 | 0 |
| **Total** | **997** | **997** | **0** | **0** | **0** |

The targeted 16 tests are a repeated subset and are not added to the 997 unique full-suite count. The eight separate Python release/export regression tests also pass. Machine-readable results are in [JAVA25_VERIFICATION_RESULTS.json](JAVA25_VERIFICATION_RESULTS.json). No remaining genuine host-test failure was observed in these suites.

The previously blocked assertions now execute under a compatible runtime rather than failing at Java21 class loading. No production code or test assertion was changed to obtain the targeted pass. The full-suite result above independently confirms zero failures across the app and ADB tests; no assertion was disabled or weakened.

## Artifact and release limits

Final signed APK SHA-256 must remain:

```text
f942f5fbe9731656ca4e944d0ce18761db8cf864a7ae7df063c787e164954e23
```

The APK is verified by hash before and after testing; it is not rebuilt or re-signed. Automated host tests do not substitute for an OEM/Android compatibility matrix or prove every real-device TLS/network path. The developer-reported Galaxy A15 and YOU-BOX/development evidence remains documented separately in DEVICE_TEST_EVIDENCE.md.

Robolectric emits a native-access warning on Java25 and Gradle reports existing deprecated features. Warnings are not test failures and were not silenced by changing production/test configuration.

## GitHub readiness

Authenticated recheck still finds live main at 442330d85a84d8bad0930883693e14a36fe6cd09, branding at 2bf8aa3ef24a9935ba7a5a545ea4039326057fff and no releases. The inherited push-triggered publisher with release-creation commands remains live. Local workflows are manual/non-publishing; local source additions are not yet all public.

The local CI workflow now prepares this same temporary Test launcher in RUNNER_TEMP and passes it to its build/test/lint command, preserving daemon21. The generated init-script body matches the one exercised locally. setup-java25 alone would not override daemon21. The full CI build/lint pipeline was not run remotely and no remote workflow/setting was changed; CI results must still be reviewed after a separately authorized push. No production Gradle configuration was changed.

Publication remains NO-GO pending separately approved live publisher/settings remediation, reviewed source commit/push and exact public source/provenance availability. Follow STAGED_PUBLICATION_PLAN.md. Test success does not authorize a commit, push, tag, release, website deployment or any remote mutation.

## Files updated locally in this verification

- `.github/workflows/ci.yml`
- `README.md`
- `docs/FINAL_RELEASE_AUDIT.md`
- `docs/JAVA25_VERIFICATION.md`
- `docs/JAVA25_VERIFICATION_RESULTS.json`
- `docs/PUBLICATION_CHECKLIST.md`
- `docs/RELEASE_BLOCKERS_RESOLUTION.md`
- `docs/RELEASE_COMMIT_FILES.txt`
- `docs/STAGED_PUBLICATION_PLAN.md`
- `release/RELEASE_NOTES_1.0.0.md`
- `scripts/release/package_final_signed_source.py`

The refreshed explicit release manifest contains 92 paths. The ignored signed-source bundle was regenerated to match these inputs and includes the Java25 verification receipt. Generated APK/source bundles and raw machine logs are not Git commit candidates. No files were staged.
