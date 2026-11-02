# YouTube and Instagram branding verification

Branch: `secreto-branding-v1`. Earlier uncommitted changes were preserved. No commit, push, tag, release, publication or device installation was performed.

## Social UI

- Replaced the About section’s old Visit/Subscribe arrangement with equally weighted **YouTube** and **Instagram** outlined actions. The SecretofNet heading and description remain, alongside unchanged developer details, website, GitHub and original project/GPL credits.
- Added the same compact “Follow SecretofNet” section directly beneath the existing Welcome introduction. Existing title, SecretoTools byline, bilingual signature, version, introduction, setup steps, feature copy, website link and fixed Continue/Skip controls remain.
- Uses local Android vector drawables: a red play mark with white triangle and a gradient Instagram camera/app mark. These are platform marks, not a SecretofNet avatar. No channel image was downloaded, inferred or generated.
- Icons are 32 dp, retain their platform colors and are explicitly not auto-mirrored. Ocean Blue surfaces, typography and outlines remain consistent with the app.
- Actions have 56 dp minimum height and localized accessibility descriptions. Widths are equal in a row at 320 dp or wider with normal font scale; narrow containers and font scales above 1.3 use a full-width vertical stack. RTL reorders the layout without reflecting the logos.
- No new onboarding page, launch delay, automatic navigation, subscription/follow call or state reset. MainActivity’s existing once-only onboarding gate is byte-for-byte unchanged. No other Welcome flow was added or modified.
- Removed the subscription action and its intent handler/config URL. Existing unused localized Subscribe copy is retained to avoid unnecessary translation churn; it is not shown by the interface.

## Confirmed URLs and intent handling

Both URLs were supplied and confirmed by the developer. Resource-backed tests assert the exact strings:

| Platform | URL | Preferred handler |
|---|---|---|
| YouTube | `https://www.youtube.com/@SecretofNet` | `com.google.android.youtube` |
| Instagram | `https://www.instagram.com/secretofnet/` | `com.instagram.android` |

Each action sends the original HTTPS URL through ACTION_VIEW, first to its matching platform app, then as an unscoped CATEGORY_BROWSABLE HTTPS intent if the app is absent or launch is blocked. Unsupported schemes are not launched. If no handler exists, localized feedback is shown. No package visibility queries, permissions, SDKs or dependencies were added. Android’s default-handler/App Links behavior still governs the fallback.

Tests cover both resource URLs, preferred packages, missing-app fallback, security exceptions, missing handlers, invalid schemes and avoiding forced Instagram routing for unrelated hosts. No live account follow or subscription was performed. Network/profile availability is not guaranteed by an intent test; URL ownership/identity relies on the developer’s confirmation.

## Languages and preserved functionality

The new heading and both accessible action descriptions are translated into English, Arabic, French, Spanish, Hindi, Japanese, Russian and Simplified Chinese. Platform names are non-translatable resource values so every language shows the requested brand labels. All previous translations and plural resources remain.

Hash checks against this request’s starting state confirm that the system splash, theme, onboarding state gate, remote implementation, manifest/permissions, build configuration, version catalog, LICENSE and NOTICE were preserved. ADB pairing/discovery, connection, transfers, app management/debloat, monitoring and tweaks were not edited. Original copyright and Tellyscope attribution remain in place.

## Validation

- Focused build and tests: **43 passed**, zero failures/errors/skips. Includes all locale-resource checks, social intent behavior, About renders in English/Arabic/French, and Welcome phone/tablet/large-font renders.
- English/Arabic/French About and Welcome captures were visually inspected. Tablet rendering shows balanced equal-width actions; Arabic layouts mirror while the play triangle and camera marks remain unmirrored. At larger fonts, scrolling preserves access to the content while Continue/Skip remain fixed and visible.
- `./gradlew :app:assembleDebug`: **BUILD SUCCESSFUL**.
- `./gradlew :app:testDebugUnitTest`: **681 executed; 680 passed, 1 failed, 0 skipped, 0 errors**.
- Sole pre-existing failure: `LastKnownTest.offlineTvKeepsItsFactsAcrossARestartUntilForget`. Kadb `AdbPairAuthException` uses class-file version 69 (Java 25); the current Java 21 runtime supports up to version 65. All social-link, localization, rendering and remaining feature tests pass. No new regression was found.
- Existing Gradle/Compose/test deprecation warnings remain separately from that Java compatibility failure. No dependency or runtime version was changed to mask it.
- `git diff --check`: passed.
- Screenshots use Robolectric native rendering. No emulator installation was necessary; the physical Samsung tablet and connected Android TVs were not installed or operated.

## Platform mark references

The [YouTube brand resource site](https://brand.youtube/) was consulted for the platform identity. Meta’s [Instagram brand resource page](https://about.meta.com/brand/resources/instagram/instagram-brand/) was requested but requires login in the available web view. Local vector platform representations preserve recognizable proportions, clear spacing, colors and logo orientation; no claim is made that these drawables are downloaded official asset files or SecretofNet artwork. Recheck platform trademark/asset requirements as part of production artwork review.

## Modified and added files

- [README.md](../README.md)
- [app/src/main/java/app/tellyscope/ui/DeveloperBranding.kt](../app/src/main/java/app/tellyscope/ui/DeveloperBranding.kt)
- [app/src/main/java/app/tellyscope/ui/DeveloperLinks.kt](../app/src/main/java/app/tellyscope/ui/DeveloperLinks.kt)
- [app/src/main/java/app/tellyscope/ui/ListsCard.kt](../app/src/main/java/app/tellyscope/ui/ListsCard.kt)
- [app/src/main/java/app/tellyscope/ui/WelcomeScreen.kt](../app/src/main/java/app/tellyscope/ui/WelcomeScreen.kt)
- [app/src/main/res/drawable/social_instagram.xml](../app/src/main/res/drawable/social_instagram.xml)
- [app/src/main/res/drawable/social_youtube.xml](../app/src/main/res/drawable/social_youtube.xml)
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
- [docs/SOCIAL_BRANDING_VERIFICATION.md](../docs/SOCIAL_BRANDING_VERIFICATION.md)

## Screenshots

- 83_about (local-only capture; not included)
- 83_about_arabic (local-only capture; not included)
- 83_about_french (local-only capture; not included)
- 00_welcome (local-only capture; not included)
- 00_welcome_arabic (local-only capture; not included)
- 00_welcome_french (local-only capture; not included)
- 00_welcome_tablet (local-only capture; not included)
- 00_welcome_tablet_arabic (local-only capture; not included)
- 00_welcome_large_font (local-only capture; not included)

## Final debug APK

- Path: `<project>/app/build/outputs/apk/debug/app-debug.apk`
- Size: 19,488,570 bytes
- SHA-256: `780d79b45500a359842abc4505c923ebc34f619d5cc15ef414099323b8227eb3`

## Remaining limitations

- The existing Kadb Java 25 bytecode / Java 21 test-runtime compatibility issue is tracked separately from social changes.
- OS link handling, network availability and remote platform behavior can differ by device. No automatic following/subscription is attempted.
- Physical-tablet validation was intentionally deferred; no device was installed or modified.
- Historical verification documents refer to earlier social-button states; this report describes the current YouTube/Instagram interface.
