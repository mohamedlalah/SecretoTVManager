# Live GitHub release-readiness audit

Status: **BLOCKED**, with authenticated read access available through the connected GitHub tool. No remote mutation was performed. The local gh CLI token is invalid; it was not used to retrieve credentials or change authentication.

## Verified live state

Repository: https://github.com/mohamedlalah/SecretoTVManager

- Visibility: public; not archived; default branch: main.
- Connected identity has repository read/admin permission metadata; no secrets were read.
- Remote main: 442330d85a84d8bad0930883693e14a36fe6cd09.
- Remote secreto-branding-v1: 2bf8aa3ef24a9935ba7a5a545ea4039326057fff.
- Branches main/dev/secreto-branding-v1 are reported unprotected; rulesets API returns an empty list.
- Git refs API returns 72 tags, including inherited upstream history; no v1.0.0 tag exists.
- Releases API returns zero releases (including drafts visible to this connection). Workflow-runs API returns zero runs; this alone does not prove Actions is disabled.
- The branding branch tree lacks SECRETO_CHANGELOG.md, release/SOURCE_AVAILABILITY.md, scripts/release/check_release.py and docs/FINAL_RELEASE_AUDIT.md. Public visibility therefore does not mean this local release's complete source is already public.

Evidence and tag/branch metadata: [LIVE_GITHUB_AUDIT.json](LIVE_GITHUB_AUDIT.json).

## Critical live workflow discrepancy

The live main and branding branches still contain the inherited push-triggered release.yml with main/dev/tag triggers, contents:write jobs, signing-secret references, git tag/push and gh release create commands. It also retains Tellyscope release/artifact naming.

Evidence:
- https://github.com/mohamedlalah/SecretoTVManager/blob/442330d85a84d8bad0930883693e14a36fe6cd09/.github/workflows/release.yml
- https://github.com/mohamedlalah/SecretoTVManager/blob/2bf8aa3ef24a9935ba7a5a545ea4039326057fff/.github/workflows/release.yml

The local replacement is manual-only and non-publishing, but it is not deployed. Missing secrets or zero runs are not sufficient assurance against future publication. Do not push/tag/merge assuming local edits already protect the live repository.

## Precise manual verification/remediation checklist

The connector does not expose actions/workflows or environments through its allowed fetch endpoints. Repository Actions permissions, workflow enablement, release immutability and Environment policies therefore require manual inspection; no secret values are needed.

Before any later authorized publishing-related push:

1. Repository Actions → inherited Release workflow → verify its current state. With separate authorization, disable that workflow before preparing the workflow replacement. Do not click Run workflow or rerun old jobs. Inspect queued/in-progress runs; none were returned during this audit.
2. Settings → Actions → General: inspect enabled Actions policy, default GITHUB_TOKEN permissions and workflow approval policy. Prefer read-only default permissions; confirm no inherited publisher/integration is exempted.
3. Settings → Environments: inspect release/alpha environments, required reviewers, allowed deployment branches/tags and secret NAMES only. Do not reveal, copy or export values. Ensure a push cannot access a production key unexpectedly.
4. Settings → General → Releases (where available): inspect immutable-release and tag/release protection policies. Settings → Rules/Branches: review protections and consider requiring review for workflow changes and release tags.
5. With separate commit/push approval, publish the reviewed manual-only workflow and exact sanitized source to the intended branch/default branch. Re-fetch live files afterward; verify removal of push-triggered publishing and signing-secret references. Never assume the working tree equals the remote.
6. Verify all app/resource/Gradle/legal inputs, native/dependency source assets and final source archive are publicly downloadable at the reviewed immutable commit/asset URLs. GitHub's automatic source archive covers only committed repository content, not ignored dependency/native bundles.
7. Confirm Releases and tags again before creating v1.0.0, never overwrite an existing tag/release, and verify artifact/source checksums after any separately approved publication.
8. Settings → Pages and repository webhooks/installed integrations: confirm no unrelated website deployment or release integration will be triggered. No Pages settings or external integrations were changed or fully enumerated here.

This audit performed GET operations only. It did not disable a workflow, change permissions, create a tag, commit source, upload an artifact or publish a release.
