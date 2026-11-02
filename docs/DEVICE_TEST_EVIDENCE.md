# v1.0.0 real-device test evidence

Evidence source: developer's report in this release-preparation conversation. These tests were not repeated by the audit agent. No device was installed, queried or modified during this audit.

| Device / case | Reported result | Build classification | Limits |
|---|---|---|---|
| Samsung Galaxy A15: signed APK installed and working | PASS, developer reported | Final signed v1.0.0, APK SHA-256 f942f5fbe9731656ca4e944d0ce18761db8cf864a7ae7df063c787e164954e23 | Exact launch/navigation test script and Android firmware version not supplied |
| YOU-BOX: discovery, connection, remote control | PASS, developer reported | Build/hash not explicitly identified for these individual TV tests | Do not silently label these signed-release tests; record which APK was used if known |
| File transfers | Tested during development | Development/debug-build evidence; exact APK/hash not supplied | Signed-release repetition not confirmed |
| Application management | Tested during development | Development/debug-build evidence; exact APK/hash not supplied | Signed-release repetition not confirmed |
| Launcher management | Tested during development | Development/debug-build evidence; exact APK/hash not supplied | Signed-release repetition not confirmed; no destructive retest performed |
| TV tweaks | Tested during development | Development/debug-build evidence; exact APK/hash not supplied | Signed-release repetition not confirmed; exact operations not supplied |

The development rows distinguish development testing from the explicitly identified final signed APK. They do not assert that every development APK was literally a Gradle debug variant; that variant/hash was not recorded. The YOU-BOX result is real-device functional evidence, but its signed-versus-debug attribution remains unspecified.

Wireless pairing, TLS authentication failures/retry, certificate-generation behavior, reconnection and a full Android/OEM compatibility matrix are not individually evidenced by the supplied statement. No pairing codes, network addresses, device IDs or ADB keys are recorded here.
