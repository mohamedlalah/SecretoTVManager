# Secreto TV Manager 1.0.0 publication checklist

Status: HOLD for live remote workflow/public-source remediation and reviewed CI Test JVM integration. Java25 validation now PASS (997/997); see JAVA25_VERIFICATION.md. Native source closure and fresh sanitized local packaging now PASS; see RELEASE_BLOCKERS_RESOLUTION.md. These are future steps, not permission to execute them. No commit, push, tag or publication was performed in this audit.

## 1. Close the blockers before staging

- [x] Obtain the exact native source and build inputs for androidx.graphics:graphics-path:1.0.1. The original source JAR contains Kotlin files but no C/C++ sources; the immutable native/module/build archives are now supplied (see NATIVE_GRAPHICS_PATH.md); four native shared libraries are in the signed APK. Identify the immutable AndroidX revision used for that artifact and include the native sources, associated build scripts, required non-system subcomponents and applicable notices. Do not substitute current upstream main or assume a similarly named archive is exact source.
- [ ] Review the remaining dependency source archives for the GPL corresponding-source definition (LICENSE section 1), not just matching Maven filenames. Account for generated code and shared/native code; confirm which build tools/System Libraries may be excluded.
- [x] Resolve or isolate the Java 21/25 test-runtime mismatch in a reviewed verification setup. Run the failing LastKnownTest and relevant Connector/pairing/certificate tests on Java 25, recording the actual test JVM. The daemon criteria select 21 and take precedence over JAVA_HOME. The isolated Java25 Test launcher now passes all app/ADB tests; daemon21/project toolchain settings remain unchanged. CI must adopt an explicit compatible Test JVM before its result is considered equivalent.
- [ ] Record what the Galaxy A15 physical test actually covered: app launch/navigation versus real TV discovery, classic connection, wireless pairing, authorization failure/retry and reconnection. Use non-destructive checks; do not infer all paths from a launch test. Record TV/OS models without private IPs, pairing codes or ADB keys.
- [ ] Confirm rights/provenance for added artwork and preserve applicable service-mark/third-party notices. Do not imply YouTube, Instagram or OEM endorsement.
- [ ] Review the sanitized documentation, README screenshots and prepared release notes. Screenshots are simulated test renders, not Galaxy A15 captures.

## 2. Review the exact commit set (future explicit approval required)

The current explicit candidate list is docs/RELEASE_COMMIT_FILES.txt. It includes prior uncommitted branding/update work, all newly added icon assets, release scripts/tests/legal materials and this documentation. It excludes ignored outputs and signing files. Review every file; the list is a candidate set, not an assertion every line has independent coverage.

Read-only review commands:

```sh
git branch --show-current
git diff --check
git diff --stat
git ls-files --others --exclude-standard
cat docs/RELEASE_COMMIT_FILES.txt
```

Only after blockers are closed and the user explicitly approves committing, regenerate the list if any files changed, then stage/review the explicit set:

```sh
git add --pathspec-from-file=docs/RELEASE_COMMIT_FILES.txt
git diff --cached --check
git diff --cached --stat
git diff --cached
```

Do not use git add -f. Do not stage build/, .gradle/, .idea/, local.properties, keystores, .env files, passwords, certificates' private keys, device data, raw Gradle/daemon logs or old local-only source bundles. The daemon criteria file is portable configuration, but its Java 21 choice needs the explicit test-runtime decision above.

After explicit approval, commit the reviewed snapshot with an appropriate v1.0.0 source-preparation message. Keep the existing signed APK unchanged. Record the immutable commit ID. Do not silently change app source/configuration and keep claiming the old APK was built from it; if functional/build inputs must change, reassess binary/source provenance before distribution.

## 3. Assemble the final distribution beside the preserved APK

Use a new distribution directory outside the checkout. Use the newly verified app/build/final-signed-release-materials only after remaining approval gates. Do not overwrite or upload app/build/release-materials as-is: it is an unsigned-review bundle, contains an older source snapshot with local paths, lacks native-source closure and does not authenticate the official signed APK.

The existing prepare_materials.py intentionally expects an unsigned APK. It is not an official signed-release packaging command. Do not rebuild or rename the signed APK to satisfy it.

After approval and an immutable reviewed source commit:

```sh
# Set these to the reviewed commit and a NEW, private staging directory.
RELEASE_COMMIT='<reviewed-full-commit-id>'
DIST='/path/to/new/release-staging'
mkdir -p "$DIST"

git archive --format=tar.gz --prefix=SecretoTVManager-1.0.0/ \
  -o "$DIST/Secreto-TV-Manager-1.0.0-project-source.tar.gz" "$RELEASE_COMMIT"
cp app/build/outputs/apk/release/Secreto-TV-Manager-1.0.0.apk "$DIST/"
cp LICENSE NOTICE release/RELEASE_NOTICE.md release/SOURCE_AVAILABILITY.md "$DIST/"
cp -R release/licenses "$DIST/licenses"
```

Also attach the reviewed dependency/native corresponding-source archive with build instructions, full relevant notices and a mapping from the APK's runtime inventory to each immutable source version/revision. Source JAR presence alone is not sufficient. Write final provenance containing the source commit, APK checksum, signing-certificate fingerprint, dependency revisions, source archive checksums and documented local validation results. Do not include absolute local paths or signing secrets.

Verify the staged APK still has exactly this SHA-256:

```text
f942f5fbe9731656ca4e944d0ce18761db8cf864a7ae7df063c787e164954e23
```

Run apksigner verify --verbose --print-certs and aapt2 dump badging on the copied APK; require v2/v3 true, one signer with SHA-256 83d1c238040cc3dca9dbb3fe71ad5468a95eb4aaa09654f6bb7107d36c06ef61, com.secreto.tvmanager / 1.0.0 / code1, and size under 4,000,000 bytes. Generate SHA256SUMS for the final staged assets. Public certificate/fingerprint is safe to distribute; private key is not.

## 4. Publish only after separate approval

- [ ] Confirm origin is the official mohamedlalah/SecretoTVManager repository and that the reviewed source will be publicly downloadable at the stated immutable commit.
- [ ] Review active Actions, repository rules, external integrations and GitHub Environments in the live repository. Only local workflow files were audited here; remote settings were not verified.
- [ ] With explicit authorization, push the reviewed branch/commit, verify the remote source and manually tag v1.0.0 at that commit (never move an existing public release tag).
- [ ] With explicit authorization, prepare a GitHub Release titled “Secreto TV Manager v1.0.0”, using release/RELEASE_NOTES_1.0.0.md after removing its unresolved-prerequisite text and adding final source links/checksums. Do not remove caveats while blockers remain.
- [ ] Attach the original signed APK, SHA256SUMS, project/dependency/native source archives, build/provenance instructions and legal materials. GPL section 6(d) requires equivalent source access with clear directions next to the binary, at no additional charge; a repository homepage alone is not the finished source delivery.
- [ ] Review the draft, then obtain approval before making it public. No automatic workflow publishes releases.
- [ ] Verify the downloaded public APK hash/certificate and public source links after publication. Keep the release signing key and backups offline/private; do not upload them.

Do not claim bit-for-bit reproducibility: no independent clean build was done in this audit, and reproducing the official signature requires private signing material which must remain private. Functional source rebuildability does not require giving users the official private key.
