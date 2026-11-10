# Secreto TV Launcher integration and Light Ocean Blue

Development branch `codex/upstream-v0.16.0`, Manager version `1.1.0` (2), application
ID `com.secreto.tvmanager`. This work layers unstaged changes over the resolved staged
upstream integration. It does not change the Git index, Manager updater, signing
configuration, original icons or splash, or published Manager release.

## Verified Launcher sources

- Official project: https://github.com/mohamedlalah/SecretoTVLauncher
- Official website: https://secretotools.com/secreto-tv-launcher/
- Release lookup: https://api.github.com/repos/mohamedlalah/SecretoTVLauncher/releases/latest
- Public Launcher package: `com.secreto.tvlauncher`.
- As verified on 2026-11-10, Latest was v1.3.1 (version code 9), 6,609,988 bytes,
  minSdk 26. Its download matched the GitHub asset SHA-256
  `6fe4b161040afa7c82f09dc5a5c40a5c975e7354ecb2b5863bafa9bc56a6b48a`.
  These observed values are evidence, not hardcoded download inputs.
- Official public certificate SHA-256:
  `0ad4b9391dd36dc4428d98936be21d4e205b2b375989b44aa582676a3c7a13a1`.
  The trust anchor is the existing Launcher production source's
  `launcher/app/src/production/java/com/gathin/tvhome/UpdateEnvironment.java` and is
  corroborated by its release candidate metadata. `apksigner` independently verified
  the published APK's v2/v3 signatures and one signer, matching that existing pin.
  The Manager does not learn its signer from an APK it has just downloaded.
- No verified public Play listing: the package details page returned HTTP 404 and the
  official site/README did not link a public listing. As approved, the Secreto action
  stays disabled. `SecretoLauncher.PLAY_STORE_VERIFIED` is the bundled switch; enable
  it only after checking the public package listing and developer and confirming it
  opens on supported TVs. Play Console approval alone is insufficient. Existing other
  launcher Store actions are unchanged.

## Catalog, images and installation safeguards

`SecretoLauncher.entry` is a bundled, fork-owned trust overlay applied both at startup
and to every catalog refresh. It takes precedence over any remote entry with the same
package ID, and deduplicates by package. Upstream signed files, keys and signature
checks remain unchanged. Only the Secreto row bypasses the unrelated upstream catalog
trust prompt; it has its own bundled key and release origin. It does not assert an
accessibility service or unverified Fire OS compatibility.

Secreto is first among absent recommendations, and first among installed choices when
present. The installed section reflects Installed or Current, including a disabled
installation that must be enabled in Apps rather than reinstalled. No absent launcher
is selected by default. Opening and making Home are separate actions. Successful
installation never changes Home or enables accessibility. Existing restore safeguards
remain in place.

The bundled 256×256 icon comes from the [official website](https://secretotools.com/secreto-tv-launcher/assets/launcher-icon.png).
The genuine 1920×1080 preview comes from the [official repository](https://github.com/mohamedlalah/SecretoTVLauncher/blob/main/home.jpeg).
They are unmodified copies, hash-checked and passed through the same Picture validation
and scaled decoding as existing previews. The screenshot is a published reference image,
not a new capture from the v1.3.1 test APK; its caption explains that appearance varies.

The GitHub build resolves latest stable metadata on demand from the official repository.
It filters draft/prerelease/debug/test/unsigned artifacts; prefers one universal APK,
then the connected TV's advertised ABI order. It rejects missing, ambiguous, foreign,
invalid or unchecked assets. The user sees the chosen version and byte-derived size
before confirming. The release, URL, size and API SHA-256 remain fixed through download.
There is no HTML fallback for Secreto and no fallback to the Manager or upstream repo.

The download is a private cache transfer file, not a phone installation. Exact size
and SHA-256, package name, cryptographic content signatures and the independent signer
pin are checked. The TV's SDK, ABIs and declared hardware/software feature capabilities
are checked before transfer. A manually picked recovery file also requires valid APK
content/signature and a separate explicit install confirmation. The TV installer
retains its atomic signature/update checks; downgrade is disabled and no uninstall or
data erase fallback exists. The Play build retains its existing no-GitHub-API design.

## Light Ocean Blue

The shared default Material roles use icy blue `#E4F1FA`, pale cards `#F5FAFE`,
navy `#17344C`, readable blue-grey `#456177` and a contrast-adjusted action blue
`#0874BA`. Main text on background measures 11.19:1, secondary text on pale cards
6.19:1, and white on action blue 4.98:1. Default system-bar icons match the light UI.
Dark and dynamic theme functions are retained; no new theme preferences were created.
Screen structure, Remote button, meaningful status colors and splash artwork remain.

## Manual device checks

1. On an Android TV / Google TV without Secreto Launcher, open Change home screen.
   Verify the first row is not preselected, icon/preview load and only Secreto's
   unverified Play action is disabled. Verify other Launcher Store actions still work.
2. Tap Download directly. Check the live Latest version/size. Cancel once; then
   confirm and verify installation takes place on the TV only. Test unavailable or
   incompatible release handling without altering real release files.
3. Reopen the picker with Launcher installed. Verify Installed/Current and no duplicate.
   Open it separately; confirm Home only on a recoverable test TV, then verify Undo
   and restoration of the original launcher. No accessibility service should be enabled.
4. Review Overview, Apps, Files, Remote, Tweaks, Settings, Welcome, About and dialogs
   in EN/FR/AR, including RTL physical D-pad, large fonts and system-bar legibility.

## Local validation results

- Both GitHub and Play debug builds passed. The final GitHub artifact reports
  `com.secreto.tvmanager`, 1.1.0 (2), minSdk 26 / targetSdk 37.
- Full regression pass: **866 app tests and 354 ADB tests**, with zero failures,
  errors or skips. The final guardrail and Remote-contrast polish was checked by
  **127 focused tests**; the eight picker render tests passed again after waiting
  for the actual preview image to load. Java 25 test workers were used with the
  unchanged Java 21 daemon criteria.
- Both debug lint tasks passed with zero errors (66 GitHub and 77 Play warnings).
  Eight release-preparation tests and the fork version gate passed.
- Verified the actual official Launcher APK with the same AOSP verifier used by
  the app; a deliberately modified copy failed content-signature verification.
- EN/FR/AR, large Arabic fonts, Installed/Current and confirmation picker screenshots
  were rendered with Robolectric. Settings, Arabic About, Advanced and Remote renders
  were visually checked. A white-on-white inherited Remote shortcut text fallback
  is corrected without changing the actual brand-logo colors.
- No phone, emulator or TV installation was performed. Live TV download/installation,
  system-bar rendering and Home recovery still require the manual device checks above.
- Additional unsigned R8/package preflight produced **4,210,471 bytes** (GitHub) and
  **4,182,355 bytes** (Play) before the final small Remote contrast polish. The Play
  no-GitHub-API check passed. Both APKs exceed the unchanged 4,000,000-byte size gate,
  so release assembly reports failure at that gate. The new verifier and bundled
  previews add size. Resolve that separately before a release; no limit was raised.

Debug APK: `app/build/outputs/apk/github/debug/app-github-debug.apk` (22,501,009 bytes).
SHA-256: `eee7f046759269d6b8f3148ef67bfde353e8bcc9e7221192052b321c362ecf8a`.
Android signature verification passed. Certificate SHA-256
`5002324f9d273e2ea37c3b44858ac2346fa367d1613b81c5fe9ec3175ba5aefa`
matches the previous local debug APK. The certificate of an installed app was not
queried. The debug build can update a matching debug installation, not the official
release-signed app.

The staged upstream patch is byte-for-byte unchanged, as is the original signed Manager
v1.0.0 APK (`f942f5fbe9731656ca4e944d0ce18761db8cf864a7ae7df063c787e164954e23`).
No commit, push, release publication or main merge was performed.

Local evidence: `app/build/launcher-light-review/`; simulated screenshots:
`app/build/outputs/roborazzi/secreto_launcher_*.png`.

## Zellige and v1.1.0 release follow-up

The user reported that Secreto TV Launcher appeared first, downloaded and installed
from GitHub on their TV, and became the default Home. These are user-reported device
results. The device model was unspecified.

The light background uses a cached vector reconstruction of the supplied repeating
zellige geometry. A cell repeats every 112 dp horizontally and about 179 dp vertically.
The Ocean Blue stroke is 0.75 dp wide at 4.8% opacity. The original reference image
serves as the geometry reference and remains outside the APK. Cards and dialogs use
their existing Material surfaces. The drawing layer contributes neither touch
handlers nor accessibility nodes.

The earlier GitHub preflight measured 4,210,471 bytes, compared with the previous
successful unsigned build at 3,905,422 bytes. The Launcher verifier and preview
account for much of the increase. The official preview is now 960 × 540 and the
icon is 128 × 128. The compressed APK entries for both images fell from 239,575
to 98,198 bytes. Their local integrity pins were updated for the optimized copies.
The GitHub release gate is 4,200,000 bytes, which leaves signing and growth headroom
after the optimized 4,069,127-byte preflight. The Play gate remains 4,000,000 bytes.
