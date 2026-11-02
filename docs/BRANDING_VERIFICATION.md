# Secreto TV Manager branding verification

Branch: `secreto-branding-v1`. No commits or pushes were made.

## Result

- Secreto artwork is used by the adaptive launcher, compatibility splash, Android 12+ system splash, and welcome screen. The splash inset keeps the supplied wordmark inside the system mask.
- Visible app branding is Secreto TV Manager across English and the existing translations. References describing upstream releases, issue reporting, and project destinations still identify Tellyscope.
- Ocean Blue defaults to light backgrounds (`#F5F9FF`) and blue primary accents (`#0061A4`) on every Android version. Wallpaper colors and system night mode do not override the default. Explicit dark/dynamic theme parameters remain available; the dark palette also uses blue.
- Arabic and French each translate all 929 translatable strings and all 44 plural resources. Arabic includes all six cardinal categories using count-label phrasing. French includes one/many/other. All previous languages remain packaged and Android per-app language configuration includes both additions.
- RTL remains enabled. Arabic layout mirrors the navigation, while physical left/right D-pad controls retain their directions. Added a regression test for this behavior.
- About includes the original Tellyscope contributors’ copyright and GPL attribution. LICENSE, NOTICE, existing copyright notices, source links, applicationId, signing, SDK targets, Gradle and AGP versions were preserved. The only build configuration change is adding `ar` and `fr` to locale filters.
- Discovery, pairing, connection, remote, debloat, apps, files, tweaks and monitoring implementations remain intact; the only remote implementation change is correcting physical D-pad positioning in RTL.

## Validation

- `./gradlew :app:assembleDebug`: **BUILD SUCCESSFUL**.
- Focused unit/UI regression run: **54 tests passed, 0 failures, 0 errors, 0 skipped**.
- Tests cover all existing and new language resources, format arguments and plurals, English copy, wireless connection, remote repeat, transfers/undo, debloat state, DNS rollback, and physical remote directions in RTL.
- Command: `./gradlew :app:testDebugUnitTest --tests '*StringsTest' --tests '*CopyTest' --tests '*WirelessConnectTest' --tests '*RemoteRepeatTest' --tests '*TransfersAndUndoTest' --tests '*DebloatStateTest' --tests '*DnsRollbackTest' --tests '*ScreenshotTest.remoteDirectionsStayPhysicalInRtl' :app:assembleDebug`.
- `git diff --check`: passed.
- Installed successfully with `adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk`. No APK was installed on any other device.
- Emulator API 36 / Android 16: visually checked the system splash and welcome in English, French and Arabic; Arabic text and navigation mirror correctly. Verified fake discovery/connection and opened Overview, Apps, Files, Tweaks, Remote, advanced monitoring and Settings. No TV command, app removal, reset, access revocation or other destructive operation was executed.
- Existing emulator saved devices and onboarding marker were temporarily moved for welcome verification and restored before navigation checks. Original per-app language preference was restored. The app was stopped after verification. Navigation used the existing `scenario=fake` implementation.
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`, 19,470,135 bytes.
- APK SHA-256: `9740ec0990772481c29f8dbb25efe96db1ce7e2d826a46dfaf44c8c0fae70e90`.

## Modified and added files for this work

- [app/build.gradle.kts](../app/build.gradle.kts)
- [app/src/main/java/app/tellyscope/ui/Copy.kt](../app/src/main/java/app/tellyscope/ui/Copy.kt)
- [app/src/main/java/app/tellyscope/ui/DeviceTabs.kt](../app/src/main/java/app/tellyscope/ui/DeviceTabs.kt)
- [app/src/main/java/app/tellyscope/ui/RemoteScreen.kt](../app/src/main/java/app/tellyscope/ui/RemoteScreen.kt)
- [app/src/main/java/app/tellyscope/ui/WelcomeScreen.kt](../app/src/main/java/app/tellyscope/ui/WelcomeScreen.kt)
- [app/src/main/java/app/tellyscope/ui/theme/Theme.kt](../app/src/main/java/app/tellyscope/ui/theme/Theme.kt)
- [app/src/main/res/drawable/secreto_splash_icon.xml](../app/src/main/res/drawable/secreto_splash_icon.xml)
- [app/src/main/res/values-ar/strings.xml](../app/src/main/res/values-ar/strings.xml)
- [app/src/main/res/values-es/strings.xml](../app/src/main/res/values-es/strings.xml)
- [app/src/main/res/values-fr/strings.xml](../app/src/main/res/values-fr/strings.xml)
- [app/src/main/res/values-hi/strings.xml](../app/src/main/res/values-hi/strings.xml)
- [app/src/main/res/values-ja/strings.xml](../app/src/main/res/values-ja/strings.xml)
- [app/src/main/res/values-night/colors.xml](../app/src/main/res/values-night/colors.xml)
- [app/src/main/res/values-ru/strings.xml](../app/src/main/res/values-ru/strings.xml)
- [app/src/main/res/values-zh-rCN/strings.xml](../app/src/main/res/values-zh-rCN/strings.xml)
- [app/src/main/res/values/colors.xml](../app/src/main/res/values/colors.xml)
- [app/src/main/res/values/strings.xml](../app/src/main/res/values/strings.xml)
- [app/src/main/res/values/themes.xml](../app/src/main/res/values/themes.xml)
- [app/src/test/java/app/tellyscope/ScreenshotTest.kt](../app/src/test/java/app/tellyscope/ScreenshotTest.kt)
- [app/src/test/java/app/tellyscope/TranslationsTest.kt](../app/src/test/java/app/tellyscope/TranslationsTest.kt)
- [docs/BRANDING_VERIFICATION.md](../docs/BRANDING_VERIFICATION.md)

## Pre-existing uncommitted files preserved

These were already modified or untracked when work began. Icon content and the adaptive-icon mapping were retained. The daemon JVM configuration was not edited.

- [app/src/main/res/drawable-nodpi/secreto_icon_foreground.png](../app/src/main/res/drawable-nodpi/secreto_icon_foreground.png)
- [app/src/main/res/drawable-nodpi/secreto_icon_monochrome.png](../app/src/main/res/drawable-nodpi/secreto_icon_monochrome.png)
- [app/src/main/res/drawable/secreto_icon_background.xml](../app/src/main/res/drawable/secreto_icon_background.xml)
- [app/src/main/res/mipmap-anydpi/ic_launcher.xml](../app/src/main/res/mipmap-anydpi/ic_launcher.xml)
- [app/src/main/res/mipmap-hdpi/ic_launcher.png](../app/src/main/res/mipmap-hdpi/ic_launcher.png)
- [app/src/main/res/mipmap-mdpi/ic_launcher.png](../app/src/main/res/mipmap-mdpi/ic_launcher.png)
- [app/src/main/res/mipmap-xhdpi/ic_launcher.png](../app/src/main/res/mipmap-xhdpi/ic_launcher.png)
- [app/src/main/res/mipmap-xxhdpi/ic_launcher.png](../app/src/main/res/mipmap-xxhdpi/ic_launcher.png)
- [app/src/main/res/mipmap-xxxhdpi/ic_launcher.png](../app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)
- [gradle/gradle-daemon-jvm.properties](../gradle/gradle-daemon-jvm.properties)

## Screenshots

- Android 16 system splash (local-only capture; not included)
- English welcome (local-only capture; not included)
- French welcome (local-only capture; not included)
- Arabic RTL welcome (local-only capture; not included)
- Arabic connected fake TV (local-only capture; not included)
- Arabic apps (local-only capture; not included)
- Arabic files (local-only capture; not included)
- Arabic tweaks (local-only capture; not included)
- Arabic remote (local-only capture; not included)
- Arabic advanced monitoring (local-only capture; not included)
- Arabic settings (local-only capture; not included)

## Remaining limitations

- Existing Gradle/Compose deprecation warnings remain; they do not prevent the build.
- Live hardware pairing and operations that change a TV were not exercised. Safe navigation used simulated devices; the focused regression suite checks the relevant behavior without modifying TVs.
- Visual verification was on API 36; the compatibility splash was wired for older supported Android versions but was not run on a separate older emulator.
- Translations have full resource and placeholder coverage and were visually checked on onboarding/navigation. A native-speaker editorial review of every long-form message is still advisable.
- Upstream release checks, changelog history and original Tellyscope project destinations remain in place. This work does not create a separate Secreto release/update service.
