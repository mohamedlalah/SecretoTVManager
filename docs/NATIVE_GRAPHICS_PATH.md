# graphics-path 1.0.1 native source and license closure

Status: **PASS for the identified native graphics-path source gap**. This is source/provenance closure, not a bit-identical native rebuild or a blanket certification of every dependency.

## Exact origin

The four libandroidx.graphics.path.so entries in the final signed APK are byte-for-byte identical to the corresponding jni entries in the official Google Maven artifact:

https://dl.google.com/dl/android/maven2/androidx/graphics/graphics-path/1.0.1/graphics-path-1.0.1.aar

Its POM identifies androidx.graphics:graphics-path:1.0.1, The Android Open Source Project, Apache License 2.0 and the platform/frameworks/support source repository. AAR, POM, hashes and original downloads are retained in the new source bundle.

The [official AndroidX release notes](https://developer.android.com/jetpack/androidx/releases/graphics) date 1.0.1 to May 1, 2024 and link the release changes ending at revision **8a05a22af450d589ef911d772a001a49dcb05b71**:

https://android.googlesource.com/platform/frameworks/support/+log/4fcd99eacd92d7c73fb1d3580fd423ed7704a98a..8a05a22af450d589ef911d772a001a49dcb05b71/graphics/graphics-path

That revision's libraryversions.toml sets GRAPHICS_PATH to 1.0.1. Its four main Kotlin source files exactly match the published 1.0.1 source JAR. This is an official release-linked immutable revision, not a substitute from today's main branch.

[Machine-readable provenance and download hashes](NATIVE_GRAPHICS_PATH_PROVENANCE.json) records the exact archives and checks. The linkage is from official release metadata and matching source/artifact content; no upstream cryptographic source-to-binary build attestation or independent native rebuild was obtained.

## Actual native inputs

The immutable module archive includes all 12 files under src/main/cpp: Conic.cpp/Conic.h, Path.h, PathIterator.cpp/PathIterator.h, pathway.cpp, scalar.h, math/compiler.h, math/TVecHelpers.h, math/vec2.h, CMakeLists.txt and libandroidx.graphics.path.map. Local includes resolve within this set. Remaining headers are standard C/C++ and Android NDK headers, including JNI and android/api-level.h.

CMake builds the three supplied C++ translation units into the shared library, with the original version script and max-page-size=16384 link option. The original module Gradle file supplies C++17, compiler/visibility/size/FP flags and minSdk21. Exact-version build context specifies:

- CMake 3.22.1 (minimum in the module CMake file: 3.18.1).
- Android NDK 25.2.9519653 from buildSrc/public/src/main/kotlin/androidx/build/AndroidXConfig.kt.
- Four APK ABIs: arm64-v8a, armeabi-v7a, x86 and x86_64.
- Original AndroidX buildSrc/plugin sources, root Gradle/settings/properties/version files, Gradle wrapper/archive and referenced build-log resource files are packaged verbatim.

ELF DT_NEEDED inspection on each APK library finds only libm.so, libdl.so and libc.so. No additional non-system native shared-library dependency was found. This module does not require a guessed separate Skia checkout: the required implementation/header files are supplied in the exact module tree. All supplied native code headers state AOSP Apache-2.0 copyright/terms; those exact headers and the upstream LICENSE.txt are retained. No missing source file was invented.

The packaged buildSrc archive contains four original relative symlinks. Their root Gradle/configuration and development/build_log_simplifier resource targets are included as build context. Do not extract upstream archives over arbitrary existing directories; reconstruct an isolated AndroidX tree with the module at graphics/graphics-path, buildSrc at buildSrc, Gradle archive at gradle and build-context over its root. These upstream relative links are not machine-local signing paths.

For a native rebuild/modification, use the supplied CMakeLists.txt with the NDK toolchain file, ANDROID_PLATFORM=android-21, each ABI and the original module's cppFlags. The original upstream Gradle build is available at the immutable source revision; its settings enumerate the wider AndroidX checkout, so the context subset is not represented as a standalone whole-AndroidX checkout. NDK/CMake/compilers and platform system libraries are external general-purpose tools/System Libraries, not secret release inputs. No NDK/compiler binaries are bundled, and no native rebuild was performed. Do not promise identical .so bytes from a different toolchain or omit original compiler options.

## Corresponding-source and notices

For this GPL-covered app distribution, retain the native source, necessary build instructions and relevant scripts alongside the app/dependency source package, with exact source directions and equivalent download access under GPL section 6(d). Preserve original Apache-2.0 license/copyright notices; do not relicense them or replace them with a generic GPL claim. Keep original Tellyscope GPL and NOTICE unchanged.

The fresh final-signed-release source bundle includes the immutable native/module/build archives, original copyright/license texts, official POM, binary-origin comparison receipts and build context. This closes the concrete native-source omission identified in FINAL_RELEASE_AUDIT.md. Final public hosting of this package and broader distribution review remain separate requirements.
