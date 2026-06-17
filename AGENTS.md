# Agora Codex Instructions

Source: imported from Claude Code project memory at `C:\Users\newoether\.claude\projects\F--workspace-repo-Agora\memory\`.
Imported on 2026-06-16.

These instructions are specific to `F:\workspace\Repo\Agora`. They complement the global Codex/Claude memory in `C:\Users\newoether\.codex\AGENTS.md`.

## Hard Rules

- Discuss any code/file change first. Wait for explicit user approval such as "确认", "好", "执行", "do it", or equivalent before editing.
- Do not commit or push unless the user explicitly asks.
- After every code edit in Agora, run `.\build.ps1` from the repo root. Use the script, not raw `gradlew assembleRelease`; the script sets Android SDK paths.
- After `.\build.ps1` succeeds, run `.\deploy.ps1` to install and launch on the test phone, unless the user explicitly says not to deploy.
- Prefer the true/root-cause fix over workarounds or compromises. If requirements are unclear, stop and clarify.

## Build And Tooling

- Android SDK: `D:\Program Files\Android-SDK`.
- Release build script: `build.ps1` on Windows.
- F-Droid build script: `build-fdroid.ps1` under Arch WSL for reproducible-build work.
- F-Droid metadata repo: `F:\workspace\Repo\fdroiddata`.
- GitLab CLI: `C:\Users\newoether\AppData\Local\Programs\glab\glab.exe`; use with `-R newo-ether/fdroiddata`.

## Device And Server

- Test phone deployment target: `192.168.31.24:8022` via SSH/SCP.
- `deploy.ps1` uses `TargetHost`; do not reintroduce PowerShell reserved `$Host`.
- `pm install` needs root, but `am start` must not run under `su -c`.
- Backend SSH/admin host: `newoether@35.212.195.4`.
- Public domain: `newoether.space` through Cloudflare.
- Never put the raw origin IP `35.212.195.4` in app code, strings, config, or committed repo files. Client code must use `https://newoether.space/...`.

## Server Stack Reference

- `agora-rating.service`: `/usr/local/bin/agora-rating-api.py`, `127.0.0.1:8091`, SQLite at `/var/lib/agora-rating/ratings.db`.
- `agora-crash.service`: `/opt/agora-crash/agora-crash.py`, `127.0.0.1:8092`, appends to `/var/lib/agora-crash/crashes.jsonl`.
- nginx public vhost `newoether.space`: `/api/` proxies 8091, `= /crash` proxies 8092. Do not expose sensitive data from 8091.
- nginx admin vhost: localhost-only `127.0.0.1:8080`, root `/var/www/admin`, crash view via `/api/crashes`.
- Login shell on server is fish; wrap remote commands in `bash -lc '...'`.

## Project Status Notes

- Agora is considered mature as of 2026-06-14 after architecture/code review. New work should preserve that quality bar.
- Comprehensive review log: `.claude\REVIEW_EXECUTION_LOG.md`. For review follow-ups, read the top `DECISION`, `Remediation log`, and `Statistics` sections first.
- Review remediation status as of 2026-06-14: 14 fixes/features built and deployed; god-class behavioral refactor closed `wontfix`; work was in the working tree and uncommitted at the time of memory capture.
- UTF-8 JNI residual is resolved in `app/src/main/cpp/llama_chat_jni.cpp` via `utf8_to_jstring()` using UTF-8 to UTF-16 and `env->NewString()`.
- "Update wiped settings" app-store review is likely install-source/signature mismatch, not a DataStore/upgrade bug. Consider FAQ/release note if it recurs.

## F-Droid Metadata Notes

- In `metadata/com.newoether.agora.yml`, F-Droid scanner runs after `prebuild:` and before `build:`.
- Generated/downloaded binary artifacts such as proot/talloc `.so` files and Alpine rootfs tarballs belong in `build:`, so they do not exist at scan time and need no `scanignore`.
- Committed generated blobs such as `site/sitemap.xml.gz` still need `scandelete: - site`.
- Key order matters for `fdroid rewritemeta`: `scandelete:` must appear before `build:`.
- Pipeline fix exception: for F-Droid CI pipeline fixes, the user pre-approved commit, push, upload, fdroiddata updates, and CI triggers without per-action confirmation.

## Roadmap Context

- Product positioning: open-source ChatGPT alternative / agentic chat app, not an autonomous "openclaw" product.
- Design principle: chat is the spine; every capability feeds results back into conversation.
- Modes should hang off the sidebar, each with its own surface, not as special chat messages.
- Roadmap order from Claude memory:
  1. Image generation via OpenAI-compatible `/v1/images/generations` BYOK, using existing image storage and `FullScreenMediaViewer`; closes GitHub issue #15.
  2. Mistral intermittent 401 bug; likely client-side auth/retry bug; GitHub issue #5.
  3. Quick wins: localize "thinking/思考"; add SOCKS5 proxy support for model endpoints; issue #17.
  4. Scheduled tasks via sidebar Task surface, local notifications only, no FCM.
  5. Remote code control via sidebar Code surface; start with Claude Code, keep client/relay agent-agnostic, later consider opencode, codex, gemini CLI.
- Rejected: background keep-alive, anti-kill, stealth-running, or hide-from-recents behavior.

## Local Vendor Note

- `thirdparty\llama.cpp\CLAUDE.md` belongs to the vendored llama.cpp tree. Apply it only when working inside `thirdparty\llama.cpp`.
