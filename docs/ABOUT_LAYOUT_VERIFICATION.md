# About & developer layout verification

Branch: `secreto-branding-v1`. This request changes only the About dialog, its optional compact social-button presentation, About-only resources/translations, and tests/documentation. All earlier uncommitted work was retained. No commit, push, installation, publication or release was performed.

## Layout

- Existing project asset `R.drawable.secreto_icon_foreground` is reused at 48 dp beside the app name, installed versionName and long versionCode, and localized “by SecretoTools”. The signature is “Mohamed LALAH | محمد لالاح”. Version/build still come from PackageManager.
- Follow SecretofNet appears immediately below the header. Existing colored YouTube and Instagram marks, exact URLs and app-first/browser-fallback behavior are unchanged.
- Buttons use a 48 dp minimum height and compact padding. At least 256 dp of inner card width and font scale at most 1.3 use a row; smaller widths/larger fonts stack. Existing shared-button presentation outside About retains its defaults.
- Website and fork GitHub links follow the social buttons with existing 48 dp minimum targets. Developer/channel descriptions follow these primary actions.
- “Credits & licenses” is collapsed initially. Its toggle has localized show/hide accessibility labels; opening it reveals every original credit, copyright statement and legal link. Expansion is saveable for configuration changes.
- The content scrolls; title and Close remain outside the scroll container. No navigation or startup behavior changes.

## Legal content and exact destinations

No existing legal/attribution resource value was changed. The original project description, copyright/GPL statement, cleanyourtv MIT credit and commit, Kadb Apache 2.0 and spake2-java GPL v3 credits remain in the expanded section. Original LICENSE and NOTICE files are byte-for-byte unchanged.

| Action | Preserved destination |
|---|---|
| YouTube | https://www.youtube.com/@SecretofNet |
| Instagram | https://www.instagram.com/secretofnet/ |
| SecretoTools | https://secretotools.com |
| Fork source | https://github.com/mohamedlalah/SecretoTVManager |
| Original source | https://github.com/man15h/tellyscope |
| Original site | https://man15h.github.io/tellyscope/ |
| Original demo | https://youtu.be/N_PeDw5GiCc |
| GPL-3.0 | https://www.gnu.org/licenses/gpl-3.0.html |
| NOTICE / third-party license texts | https://github.com/man15h/tellyscope/blob/main/NOTICE |

Full legal texts remain accessible through the same existing external links; this refinement does not add an offline license viewer. Tests expand the section, scroll to every original item and verify external link destinations without launching a real browser.

## Validation

- Debug APK: `./gradlew :app:assembleDebug` **succeeded**.
- Focused app tests: **45 passed**, zero failures/errors/skips. Coverage includes nine new dialog tests, existing social app-first/browser-fallback tests, all eight languages’ resource checks, and existing English/Arabic/French About renders.
- Full existing app unit suite: **700 tests, 699 passed, 1 failed, zero errors/skips**. The only failure is the pre-existing `LastKnownTest.offlineTvKeepsItsFactsAcrossARestartUntilForget`: Kadb `AdbPairAuthException` is compiled for class-file version 69 (Java 25), while the test JVM supports version 65 (Java 21). This is the same failure recorded before this refinement; no new failing tests were observed. Build/dependency versions were left unchanged.
- `git diff --check`: passed.
- Native Robolectric/Compose layouts reviewed at 360×800 dp phone, 320×640 dp narrow phone, 800×1280 dp tablet, 411×891 dp Arabic RTL and 800×360 dp landscape; narrow-phone large-font rendering uses system font scale 1.6. Both social controls are asserted visible before scrolling on normal/narrow phone and tablet cases. Tablet and Arabic suitable-width cases assert side-by-side placement and RTL ordering. Large font and landscape tests scroll to both social buttons and all the way to the final legal credit while checking Close stays visible.
- Tests reserve 56 dp of total phone dialog inset and a 560 dp maximum dialog width because Robolectric omits native dialog margins. Captures are simulated layout verification, not physical Galaxy A15 testing. No physical device was installed or operated.
- Exact outgoing project/legal intents and dynamic version/build are tested. Existing link tests cover missing/blocked social apps and browser fallback. Live social browsing was not repeated; verified URLs and handlers are unchanged.

## Preserved scope

Baseline hash comparisons confirm Welcome, Splash, Settings navigation, update implementation, ADB/TV implementations, theme, manifests, build configuration, package ID/signing/SDK configuration, icon/logo assets, LICENSE and NOTICE are unchanged by this request. All eight languages remain: English, Arabic, French, Spanish, Hindi, Japanese, Russian and Simplified Chinese. Four new localized strings per language cover About byline, credits heading and show/hide labels; the About-only maintainer signature is non-translatable. No dependency was added.

## Modified / added files for this request

- [app/src/main/java/app/tellyscope/ui/DeveloperBranding.kt](../app/src/main/java/app/tellyscope/ui/DeveloperBranding.kt)
- [app/src/main/java/app/tellyscope/ui/ListsCard.kt](../app/src/main/java/app/tellyscope/ui/ListsCard.kt)
- [app/src/main/res/values-ar/strings.xml](../app/src/main/res/values-ar/strings.xml)
- [app/src/main/res/values-es/strings.xml](../app/src/main/res/values-es/strings.xml)
- [app/src/main/res/values-fr/strings.xml](../app/src/main/res/values-fr/strings.xml)
- [app/src/main/res/values-hi/strings.xml](../app/src/main/res/values-hi/strings.xml)
- [app/src/main/res/values-ja/strings.xml](../app/src/main/res/values-ja/strings.xml)
- [app/src/main/res/values-ru/strings.xml](../app/src/main/res/values-ru/strings.xml)
- [app/src/main/res/values-zh-rCN/strings.xml](../app/src/main/res/values-zh-rCN/strings.xml)
- [app/src/main/res/values/branding.xml](../app/src/main/res/values/branding.xml)
- [app/src/main/res/values/strings.xml](../app/src/main/res/values/strings.xml)
- [app/src/test/java/app/tellyscope/AboutDialogTest.kt](../app/src/test/java/app/tellyscope/AboutDialogTest.kt)
- [docs/ABOUT_LAYOUT_VERIFICATION.md](../docs/ABOUT_LAYOUT_VERIFICATION.md)

## Rendered previews

- about_refined_arabic (local-only capture; not included)
- about_refined_credits (local-only capture; not included)
- about_refined_landscape (local-only capture; not included)
- about_refined_large_font (local-only capture; not included)
- about_refined_narrow (local-only capture; not included)
- about_refined_phone (local-only capture; not included)
- about_refined_tablet (local-only capture; not included)

## Debug artifact

- [app-debug.apk](../app/build/outputs/apk/debug/app-debug.apk)
- SHA-256: `995fd052fae47a45ad2a1173f954d43178b299315ff65b951d17938935b4470b`
