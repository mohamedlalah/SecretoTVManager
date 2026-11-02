# Secreto TV Manager v1.0.0

Prepared release notes — publication pending final compliance/source review.

The first SecretoTools / Mohamed LALAH release of this customized Tellyscope fork brings Secreto TV Manager branding, Ocean Blue presentation, eight languages including Arabic RTL and French, and official SecretofNet YouTube/Instagram links.

## Included

- Existing TV tools: ADB discovery, pairing/connection, remote, debloat/undo, app management, files, tweaks and monitoring.
- Compact About & developer dialog, first-run Welcome branding and preserved original credits.
- Fork-only update checks, project reporting/sharing and release history. Updates open the official release page; no automatic APK download or installation.
- Bundled GPL-3.0 license, original NOTICE and additional third-party legal materials.

## Download and verify

- Filename: **Secreto-TV-Manager-1.0.0.apk**
- Application ID: `com.secreto.tvmanager`
- Version name/code: `1.0.0` / `1`
- Size: **3,742,065 bytes**
- Controller minimum: **Android 8.0 / API 26**
- Signature schemes verified: **v2 and v3**, one RSA-4096 signer.

APK SHA-256:
```text
f942f5fbe9731656ca4e944d0ce18761db8cf864a7ae7df063c787e164954e23
```

Signing certificate SHA-256:
```text
83d1c238040cc3dca9dbb3fe71ad5468a95eb4aaa09654f6bb7107d36c06ef61
```

The developer reports physical testing of this exact signed APK on a Samsung Galaxy A15. YOU-BOX discovery, connection and remote control were reported tested, but those cases’ build/hash was not explicitly identified. File transfers, app/launcher management and TV tweaks were tested during development, not confirmed individually on the final signed APK. See docs/DEVICE_TEST_EVIDENCE.md. Automated layout tests cover phone/tablet, large fonts, landscape and Arabic RTL. TV/OEM compatibility varies; no comprehensive hardware matrix is claimed.

## Known limitations and safety

Kadb class files require Java25: the full app701/ADB296 host tests now pass with Temurin25.0.5 and a temporary Test launcher (997/997, zero failures/errors/skips). Java21 test execution without that override still encounters the class-version mismatch; daemon criteria were preserved. The APK contains converted DEX, but this does not prove every ADB/TLS path works on every supported device. Java25 host verification is complete; retain the documented limits on real-TV pairing/connection coverage rather than inferring a universal compatibility claim.

Use a trusted local network and authorize only your own phone. Debloat, launcher, DNS and other system changes can disrupt TV functions; review selections and preserve a recovery path. Debug-signed installations have a different signer; do not remove them without considering app data.

## License and corresponding source

Based on Tellyscope, Copyright (C) 2026 Tellyscope contributors, under GPL-3.0-only. Fork customizations: SecretoTools / Mohamed LALAH. Original NOTICE, dependency notices and licenses must accompany this release.

**Publication prerequisite:** attach or clearly link the complete corresponding source for this exact binary, including the now-prepared immutable native graphics-path source/build closure and applicable notices (docs/NATIVE_GRAPHICS_PATH.md). Record the final immutable source commit and source-archive SHA-256 in these notes after that audit passes. Do not publish these prepared notes with this prerequisite unresolved. The fresh local signed-source package passes sanitization/integrity checks, and Java25 verification passes, but live remote workflow/public-source remediation remains blocked. See docs/RELEASE_BLOCKERS_RESOLUTION.md.

Official source/release location: https://github.com/mohamedlalah/SecretoTVManager
Original upstream: https://github.com/man15h/tellyscope
Website: https://secretotools.com
YouTube: https://www.youtube.com/@SecretofNet
Instagram: https://www.instagram.com/secretofnet/
