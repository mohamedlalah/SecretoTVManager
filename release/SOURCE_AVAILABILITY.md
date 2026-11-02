# Corresponding source and distribution checklist

Official project: https://github.com/mohamedlalah/SecretoTVManager
Original project: https://github.com/man15h/tellyscope
License: GPL-3.0-only; original copyright and third-party terms remain applicable.

Before distributing any binary, provide its complete corresponding source under the GPL,
including all fork changes, build scripts and required non-system dependency source.
A repository homepage or upstream source snapshot alone is not the exact modified source.
Do not distribute a binary while its corresponding source is only an unpublished local checkout.

scripts/release/prepare_materials.py creates a review-only source snapshot from the exact
current tracked and non-ignored project files, including uncommitted changes. It records
commit/dirty state and SHA-256 for every included file; it excludes build outputs, Git
internals and signing secrets. It copies the GPL dependency spake2-java 1.1.1 source JAR
from the local Gradle cache into the accompanying materials. Manual CI explicitly allows
fetching that exact artifact from JitPack; both paths verify its pinned SHA-256 and fail
on mismatch. Dependency source is not silently substituted with a different version.
It also copies exact-version runtime source JARs from the cache. With the explicit fetch
flag, missing source archives are requested from Google Maven, Maven Central or JitPack
using the resolved coordinates. SHA256SUMS records their downloaded bytes; unlike the
pinned spake2 archive these hashes are recorded, not independently authenticated pins.
Two inspected empty compatibility artifacts map to their matching implementation sources
in COMPATIBILITY_SOURCE_MAPPING.json. Any unresolved archives remain null entries
in DEPENDENCY_SOURCE_INVENTORY.json. Missing sources remain a
distribution-review blocker; source JAR presence alone does not prove completeness
(e.g. native code/build tools may need additional corresponding source).
The manual CI preparation uses the exact checked-out commit. No tag or release is created.

Before publication, review and host the source archive beside the APK, offering equivalent
source download access at no additional charge. Record the exact official immutable commit,
source archive checksum and APK checksum in reviewed release notes. Preserve this entire
source tree, LICENSE, NOTICE, release materials and the build instructions in README.md.
Private signing keys/passwords are neither corresponding source nor distributable material.

Resolve runtime-dependencies.txt from the produced APK; preserve sources and notices for
its exact versions (not just current upstream main). Principal upstream sources:
- Kadb/Dadb: https://github.com/flyfishxu/Kadb and https://github.com/mobile-dev-inc/dadb
- spake2-java: https://github.com/Flyfish233/spake2-java (1.1.1 source archive accompanies materials)
- AndroidX/Compose/Material: https://android.googlesource.com/platform/frameworks/support/
- Kotlin: https://github.com/JetBrains/kotlin
- Coroutines: https://github.com/Kotlin/kotlinx.coroutines
- Bouncy Castle: https://github.com/bcgit/bc-java
- cleanyourtv device data (downloaded separately): https://github.com/Gathin23/cleanyourtv

Dependency source acquisition/completeness and notices must be reviewed before actual
distribution. The prepared app snapshot plus spake2 source is not a claim to have assembled
and legally audited every transitive dependency's complete corresponding source.
No release can be declared ready solely because the unsigned APK builds.
