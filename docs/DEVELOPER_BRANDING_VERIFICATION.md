# SecretoTools developer branding verification

Branch: `secreto-branding-v1`. The existing uncommitted branding work was preserved. No commit, push, publication, release signing or release creation was performed.

## Implemented

- Enhanced the existing `AboutDialog`, reused from the device menu and a new prominent Settings entry. No duplicate About screen or new navigation route.
- Shows Secreto TV Manager; SecretoTools as this fork’s developer/maintainer; Mohamed LALAH / محمد لالاح; official website; fork source; upstream source; GPL-3.0 link; original NOTICE, website, demo and third-party credits.
- Reads versionName and versionCode from installed package metadata. Versions are not hardcoded.
- Uses scrollable content, bounded Material dialog width, wrapping link labels, 48 dp minimum link targets, accessible button semantics, light Ocean Blue surfaces and blue accents. French and Arabic were visually reviewed on the emulator.
- Added optional SecretofNet Visit and Subscribe links, a native play icon and localized channel description. URLs are configurable in `app/src/main/res/values/branding.xml`, independent of upstream update/report destinations.
- Uses user-initiated HTTPS ACTION_VIEW intents. YouTube is tried explicitly when requested; the implicit browsable intent is the fallback. Missing handlers and security exceptions are handled, with localized feedback if no handler exists. Subscription only opens the YouTube confirmation URL; there is no subscription API, automatic launch or feature gating.
- Enhanced the existing once-only welcome screen with SecretoTools credit, actual installed version/build, the feature overview and optional website/channel links. Continue and Skip remain in the fixed bottom bar. Existing onboarding and saved-device logic is unchanged.
- Added a lightweight native TV-and-gear vector to the existing AndroidX splash theme. It has extra circular-mask margin, contains no small text and introduces no delay. Existing official launcher, monochrome and foreground assets are unchanged and the full artwork remains in onboarding.
- Added all 18 new translatable strings in English, Arabic, French, Spanish, Hindi, Japanese, Russian and Simplified Chinese. Existing translations and plurals are preserved. Arabic remains RTL; physical D-pad direction fixes are preserved.
- README now distinguishes the SecretoTools fork from original Tellyscope documentation, credits contributors and explains source/distribution requirements. Historical upstream download badges now use explicit upstream URLs rather than relative fork-release paths.
- Added four link tests for the confirmation URL, YouTube/browser fallback, missing-handler feedback and unsafe-scheme rejection.
- Fixed screenshot-test infrastructure: APK fixtures are now lazy and cleaned after tests rather than allocating approximately 110 MB for every test instance. Removed only fixtures created by this request’s test run. Updated the report-screen branding assertion to resolve the translated resource while retaining its substring match for the combined notice paragraph.

## Build and test results

- `./gradlew :app:assembleDebug`: **BUILD SUCCESSFUL** (standalone final run; 43 tasks up-to-date).
- `./gradlew :app:testDebugUnitTest` (also run with assembleDebug): **668 tests executed; 667 passed, 1 failed, 0 skipped, 0 errors**. All new developer-link tests, all eight-language resource/argument tests and all screenshot/navigation tests pass.
- The sole remaining failure is the pre-existing `LastKnownTest.offlineTvKeepsItsFactsAcrossARestartUntilForget`: Kadb `AdbPairAuthException` is class-file version **69 (Java 25)**, while the available test runtime recognizes up to **65 (Java 21)**. No dependency, signing, SDK or build-tool version was changed to hide this mismatch.
- The full suite initially exhausted disk space because the existing screenshot fixture eagerly allocated approximately 110 MB for every test instance. Fixtures created during this request were cleaned, lazy allocation/teardown was added, corrupted generated test results were cleared, and the final standard suite completed without disk or heap failures.
- Two stale report-screen assertions from the earlier branding baseline were updated to use the current localized resource as a substring of the combined notice paragraph. Both now pass.
- Existing warnings remain: Gradle deprecations, existing Compose API deprecations and unused expressions/unnecessary safe calls in existing tests. They are separate from the Java-runtime test failure; no new production compiler error remains.
- `git diff --check`: **passed**.
- Final test report: [HTML results](../app/build/reports/tests/testDebugUnitTest/index.html).
- The APK was successfully installed only on `emulator-5554` after the final splash adjustment.

## Emulator verification

Only `emulator-5554` was targeted for installation or UI automation. Installed using:

```sh
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
```

Actual applicationId remains `com.secreto.tvmanager`; activity is `app.tellyscope.MainActivity`.

- Android 16 / API 36 system splash: verified full symbol is inside the mask, including antenna tips.
- Settings → About/Developer: opened and scrolled in Arabic and French. Version/build, developer identity, channel actions and upstream attribution display correctly. Also verified at a tablet-sized 1600×2560 resolution / 420 dpi (approximately 610 dp wide); the original 1080×2400 display size was restored.
- Website and channel visit controls opened an external browser on the emulator. Tests separately cover explicit YouTube preference and missing-YouTube fallback. No subscription was performed. A YouTube first-run notification prompt was left unanswered.
- First-run welcome: reviewed English, French and Arabic, including the scrollable feature and optional-link content and fixed Continue/Skip controls.
- To preview first run, existing emulator `devices.tsv` and `welcomed` were temporarily moved and restored in a `finally` block. SHA-256 outputs before and after matched byte-for-byte. Original per-app locale selection `[]` was restored and Secreto TV Manager was stopped afterward.
- Existing fake-device mode was used for navigation. No TV app was disabled, uninstalled, reset or installed, and no ADB authorization was revoked. No commands were sent to the connected physical TVs.
- Android 8–11 compatibility uses the existing AndroidX splash implementation; no older emulator was installed or used for this verification.

## Channel verification status

The requested candidate `https://www.youtube.com/@SecretofNet` could not be independently verified as the intended official channel. Direct web retrieval failed and searches did not establish ownership. The [SecretoTools website](https://secretotools.com) resolves, but that alone does not establish this channel’s ownership. Both candidate channel URLs remain configurable and the About section explicitly says confirmation is pending. Confirm the channel URL and ownership before production release.

## Legal attribution and source status

- Original LICENSE and NOTICE are byte-for-byte unchanged from the start of this request, as are original source copyright/SPDX headers.
- The About text distinguishes SecretoTools customizations/maintenance from the original Tellyscope contributors’ code and preserves original GPL and third-party credits.
- New Kotlin files carry `GPL-3.0-only` SPDX headers. No new library, tracker, analytics or advertising dependency was added.
- Before binary distribution, make the corresponding source for the exact distributed build and its modifications available under the applicable GPL terms. The source links do not replace that obligation.
- applicationId/package namespace, permissions, SDK levels, signing, Gradle/AGP versions, locale filters and all existing supplied icon files are unchanged from this request’s baseline.

## Separate Secreto release channel

`UpdateCheck.kt` remains on `https://github.com/man15h/tellyscope/releases` and `https://api.github.com/repos/man15h/tellyscope/releases?per_page=20`; newer changelog lookup remains on upstream `main`. Existing report actions and source-list links remain upstream as before. These were not redirected to the fork.

Before a separate Secreto channel is enabled, update the release-page/API constants and APK URL allow-list together, provide a fork-specific changelog/version sequence, validate compatible applicationId and signing continuity, review the existing APK-size policies, and test update selection/download links against actual fork release artifacts. Keep deliberate upstream contribution/report links separate from fork bug reporting. No Secreto release artifacts were published by this task.

## Changed and added files in this request

- [README.md](../README.md)
- [app/src/main/java/app/tellyscope/ui/DeveloperBranding.kt](../app/src/main/java/app/tellyscope/ui/DeveloperBranding.kt)
- [app/src/main/java/app/tellyscope/ui/DeveloperLinks.kt](../app/src/main/java/app/tellyscope/ui/DeveloperLinks.kt)
- [app/src/main/java/app/tellyscope/ui/ListsCard.kt](../app/src/main/java/app/tellyscope/ui/ListsCard.kt)
- [app/src/main/java/app/tellyscope/ui/SettingsScreen.kt](../app/src/main/java/app/tellyscope/ui/SettingsScreen.kt)
- [app/src/main/java/app/tellyscope/ui/WelcomeScreen.kt](../app/src/main/java/app/tellyscope/ui/WelcomeScreen.kt)
- [app/src/main/res/drawable/secreto_splash_icon.xml](../app/src/main/res/drawable/secreto_splash_icon.xml)
- [app/src/main/res/values-ar/strings.xml](../app/src/main/res/values-ar/strings.xml)
- [app/src/main/res/values-es/strings.xml](../app/src/main/res/values-es/strings.xml)
- [app/src/main/res/values-fr/strings.xml](../app/src/main/res/values-fr/strings.xml)
- [app/src/main/res/values-hi/strings.xml](../app/src/main/res/values-hi/strings.xml)
- [app/src/main/res/values-ja/strings.xml](../app/src/main/res/values-ja/strings.xml)
- [app/src/main/res/values-ru/strings.xml](../app/src/main/res/values-ru/strings.xml)
- [app/src/main/res/values-zh-rCN/strings.xml](../app/src/main/res/values-zh-rCN/strings.xml)
- [app/src/main/res/values/branding.xml](../app/src/main/res/values/branding.xml)
- [app/src/main/res/values/strings.xml](../app/src/main/res/values/strings.xml)
- [app/src/test/java/app/tellyscope/DeveloperLinksTest.kt](../app/src/test/java/app/tellyscope/DeveloperLinksTest.kt)
- [app/src/test/java/app/tellyscope/ScreenshotTest.kt](../app/src/test/java/app/tellyscope/ScreenshotTest.kt)
- [docs/DEVELOPER_BRANDING_VERIFICATION.md](../docs/DEVELOPER_BRANDING_VERIFICATION.md)

## Screenshots

- splash (local-only capture; not included)
- settings-ar (local-only capture; not included)
- about-ar (local-only capture; not included)
- about-links-ar (local-only capture; not included)
- about-fr (local-only capture; not included)
- welcome-en (local-only capture; not included)
- welcome-fr (local-only capture; not included)
- welcome-ar (local-only capture; not included)
- welcome-links-fr (local-only capture; not included)
- welcome-links-ar (local-only capture; not included)

- about-tablet-ar (local-only capture; not included)

## APK

- Exact path: `<project>/app/build/outputs/apk/debug/app-debug.apk`
- Size: 19,486,763 bytes
- SHA-256: `5f1f834da2b4e353fe6448e0c79e465b69610d4b5c6294a6116a1f95b04823ff`

## Before production release

- Confirm the SecretofNet address and ownership; remove the pending-confirmation notice only after confirmation.
- Resolve the pre-existing Kadb Java 25 bytecode versus Java 21 test-runtime mismatch and obtain a clean full-suite run.
- Confirm source availability, GPL notices and artifact/signing continuity for the exact release.
- Configure and verify a separate Secreto release channel if desired; current update links remain upstream.
- Review long-form translations with native speakers and test a smaller phone and an older supported Android version.
- Preserve the existing real YOU-BOX/remote validation baseline; do further non-destructive hardware regression testing before production.
- Review these local changes before committing, publishing or creating a release.
