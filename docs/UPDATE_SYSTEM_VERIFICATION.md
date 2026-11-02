# Secreto TV Manager update-system verification

Branch: `secreto-branding-v1`. Earlier uncommitted work was preserved. No commit, push, publication, installation, tag or release was performed.

## Audit and resulting behavior

| Area | Previous behavior | Current behavior |
|---|---|---|
| UpdateCheck | Upstream Tellyscope releases-list API; array parsing; optional raw APK asset links | Only `https://api.github.com/repos/mohamedlalah/SecretoTVManager/releases/latest`; single-object parsing; official fork release page only |
| MainActivity | Resume-triggered check and browser action for direct APK download | Same resume-triggered check; only user-initiated official release-page navigation |
| ViewModel | A missing release and failed request were indistinguishable | Separate status for no published release, current version, available version, disabled and failed checks; no-release status reaches Settings; disabling checks clears pending offers |
| Settings | Update/report/share descriptions and source referred to upstream | Localized descriptions and project actions identify the official fork |
| What's new | Bundled original Tellyscope history and upstream main changelog fetched over the network | Bundled `SECRETO_CHANGELOG.md` for this fork; newer notes come from the already-fetched official latest-release response; no secondary repository or fallback request |
| Notifications | In-app dialog with upstream title and raw APK link | Secreto TV Manager title in all eight languages; Check release and Cancel only |
| Bug reporting | Upstream issue tracker and Tellyscope report labels/details | Fork issue tracker, fork-branded report details and matching existing issue-template fields |
| Share | Upstream repository link | `https://github.com/mohamedlalah/SecretoTVManager` |
| Upstream credits | Entangled with project-action constants | Separate `UPSTREAM_REPO` retained for About/source attribution and dependency/list-format documentation |

There is no separate Android notification service in this flow: the existing update indication is an in-app dialog and What's new card. `ON_RESUME` checks opportunistically after the interval; no background scheduler or startup delay was added.

## Endpoint, cadence and no-release handling

- Exact endpoint: `https://api.github.com/repos/mohamedlalah/SecretoTVManager/releases/latest`.
- The public [GitHub latest-release API](https://docs.github.com/en/rest/releases/releases#get-the-latest-release) returns a release object. The parser rejects arrays, drafts, prereleases, malformed version tags and release-page links outside the exact official repository/tag path.
- The optional switch remains persisted in the existing properties file. Stable builds retain the 30-minute interval; existing alpha builds retain their 5-minute interval, but use the same stable `/latest` endpoint rather than a separate prerelease feed.
- A 404 becomes **No published updates available**. It is cached/throttled like a successful check and survives restarts. There is never a request to upstream as a fallback.
- Existing upstream `checked` timestamps are ignored unless the stored source matches the new endpoint, so migration does not suppress the first fork check. The existing user-off preference is preserved.
- Offline, rate-limit, malformed-response and other failures remain distinct from “no release”; cancellation propagates. Failed requests may retry at a later foreground check, preserving the previous failure-retry behavior.
- Transport uses the fixed HTTPS endpoint, explicit metadata Accept/User-Agent headers, no redirects, 10-second connect/read timeouts, a 30-second response budget and a 1 MiB metadata bound. It never downloads an APK.

## APK installation policy

The direct Download APK action, APK asset field and MainActivity download callback were removed. API asset URLs are ignored even if present. Neither package installation nor APK download is offered by the update UI, and the app does not silently replace itself.

Consequently this change makes no claim to have inspected or verified an update binary. Any future in-app installer must first verify the APK’s applicationId, version/versionCode, signing certificate/compatible signing lineage and user confirmation before installation is offered. For this task the safer release-page-only approach is used; app versionName comparison is only for a metadata notification.

## Changelog, attribution and project actions

- New `SECRETO_CHANGELOG.md` contains an Unreleased fork section; it does not fabricate a published release. The existing bundle task packages this file under the existing changelog asset name.
- Original `CHANGELOG.md`, LICENSE, NOTICE and original attribution remain byte-for-byte unchanged. Original history is retained separately, not shown as published Secreto releases.
- App-facing changelog PR links now resolve on the official fork. Newer release notes require no additional network source.
- About’s upstream repository/site/demo links are preserved as attribution. The custom debloat-list guide remains an upstream dependency-format documentation link. cleanyourtv reporting and device-list/launcher dependency URLs remain appropriate third-party sources.
- The current README explains the fork-only update source, manual release-page policy and distinction from historical upstream badges/documentation.
- Existing release CI still reads original `CHANGELOG.md` for release notes/version checks. No release workflow was triggered or modified. Before a future official publication, align that publishing workflow with the fork changelog and reviewed release/version/signing process; this is separate from the runtime update source fixed here.

## Preserved application behavior

Hash comparisons against this request’s baseline confirm that Welcome/Intro, social UI, system splash, theme, remote implementation, manifest/permissions, version catalog, Gradle wrapper, legal files and original upstream changelog were preserved. No ADB discovery/pairing, app/debloat, transfer, monitoring or tweak implementation was modified. The only app Gradle change is the fork changelog input/comment; applicationId, signing, SDK and Gradle/AGP versions are unchanged.

All update/status, reporting, sharing and notification copy is localized in English, Arabic, French, Spanish, Hindi, Japanese, Russian and Simplified Chinese. The existing About layout and branding are unchanged; only its upstream URL constant was decoupled from the fork’s release constant.

## Validation

- Focused checks: **88 passed**, zero failures/errors/skips, covering app update/version parsing, request URLs, 404/no-release states, cache migration, throttling and switch persistence, ViewModel status/disabled-offer behavior, official project actions, fork changelog bundling, localized resources, rendering and the ADB module’s report-link tests.
- Rendered Settings and What's new no-release states and the release-page-only dialog were visually reviewed. Historical body text in the existing dialog test fixture is test data, not a published fork release.
- `./gradlew :app:assembleDebug`: **BUILD SUCCESSFUL**.
- Full app suite: **691 tests, 690 passed, 1 failed, 0 skipped**. The sole failure is the pre-existing `LastKnownTest.offlineTvKeepsItsFactsAcrossARestartUntilForget`: Kadb `AdbPairAuthException` is class-file version 69 (Java 25), while the test runtime supports version 65 (Java 21). No new failing tests were observed. Dependency, SDK and Gradle versions were not changed to mask this issue.
- `git diff --check`: passed.
- No live APK download, installation, TV command, issue submission or release publication was performed. Metadata regression tests use injected responses; this report does not assert that a live official release currently exists.

## Changed and added files for this request

- [.github/ISSUE_TEMPLATE/device_report.yml](../.github/ISSUE_TEMPLATE/device_report.yml)
- [README.md](../README.md)
- [SECRETO_CHANGELOG.md](../SECRETO_CHANGELOG.md)
- [adb/src/main/kotlin/app/tellyscope/adb/Reports.kt](../adb/src/main/kotlin/app/tellyscope/adb/Reports.kt)
- [adb/src/test/kotlin/app/tellyscope/adb/ReportsTest.kt](../adb/src/test/kotlin/app/tellyscope/adb/ReportsTest.kt)
- [app/build.gradle.kts](../app/build.gradle.kts)
- [app/src/main/java/app/tellyscope/MainActivity.kt](../app/src/main/java/app/tellyscope/MainActivity.kt)
- [app/src/main/java/app/tellyscope/data/UpdateCheck.kt](../app/src/main/java/app/tellyscope/data/UpdateCheck.kt)
- [app/src/main/java/app/tellyscope/ui/ListsCard.kt](../app/src/main/java/app/tellyscope/ui/ListsCard.kt)
- [app/src/main/java/app/tellyscope/ui/SettingsScreen.kt](../app/src/main/java/app/tellyscope/ui/SettingsScreen.kt)
- [app/src/main/java/app/tellyscope/ui/TellyscopeViewModel.kt](../app/src/main/java/app/tellyscope/ui/TellyscopeViewModel.kt)
- [app/src/main/java/app/tellyscope/ui/UpdateDialog.kt](../app/src/main/java/app/tellyscope/ui/UpdateDialog.kt)
- [app/src/main/java/app/tellyscope/ui/WhatsNewScreen.kt](../app/src/main/java/app/tellyscope/ui/WhatsNewScreen.kt)
- [app/src/main/res/values-ar/strings.xml](../app/src/main/res/values-ar/strings.xml)
- [app/src/main/res/values-es/strings.xml](../app/src/main/res/values-es/strings.xml)
- [app/src/main/res/values-fr/strings.xml](../app/src/main/res/values-fr/strings.xml)
- [app/src/main/res/values-hi/strings.xml](../app/src/main/res/values-hi/strings.xml)
- [app/src/main/res/values-ja/strings.xml](../app/src/main/res/values-ja/strings.xml)
- [app/src/main/res/values-ru/strings.xml](../app/src/main/res/values-ru/strings.xml)
- [app/src/main/res/values-zh-rCN/strings.xml](../app/src/main/res/values-zh-rCN/strings.xml)
- [app/src/main/res/values/strings.xml](../app/src/main/res/values/strings.xml)
- [app/src/test/java/app/tellyscope/ChangelogTest.kt](../app/src/test/java/app/tellyscope/ChangelogTest.kt)
- [app/src/test/java/app/tellyscope/ProjectActionsTest.kt](../app/src/test/java/app/tellyscope/ProjectActionsTest.kt)
- [app/src/test/java/app/tellyscope/ScreenshotTest.kt](../app/src/test/java/app/tellyscope/ScreenshotTest.kt)
- [app/src/test/java/app/tellyscope/UpdateCheckTest.kt](../app/src/test/java/app/tellyscope/UpdateCheckTest.kt)
- [app/src/test/java/app/tellyscope/UpdateFlowTest.kt](../app/src/test/java/app/tellyscope/UpdateFlowTest.kt)
- [docs/UPDATE_SYSTEM_VERIFICATION.md](../docs/UPDATE_SYSTEM_VERIFICATION.md)

## UI captures

- 99_settings_no_published (local-only capture; not included)
- 99_whats_new_no_published (local-only capture; not included)

## Debug artifact

- Exact path: `<project>/app/build/outputs/apk/debug/app-debug.apk`
- Size: 19,811,491 bytes
- SHA-256: `5faecfa505ab460941fa3ca97f238b745a5c0a88ce8b367839bde6203ef3afc9`

## Remaining limitations

- The previously documented Kadb Java 25 bytecode / Java 21 test-runtime mismatch is tracked separately from this update fix.
- GitHub/network availability and future release publication remain external requirements; a missing release is handled without upstream fallback.
- Only the latest stable official release is queried. Alpha builds do not discover prereleases through this endpoint.
- Review publishing CI’s changelog input before a future release; nothing was published or installed by this task.
- Existing verification documents refer to earlier updater behavior; this document supersedes their statements that runtime update/report/share actions still target upstream.
