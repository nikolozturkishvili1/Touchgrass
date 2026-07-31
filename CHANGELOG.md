# Changelog

All notable changes to Touchgrass are documented here. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project follows
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.2] - 2026-07-22

### Changed
- Now targets Android 16 (API 36), meeting Google Play's target-API requirement
  (deadline Aug 31, 2026). Toolchain bumped alongside: AGP 8.7.3 → 8.9.2,
  Gradle 8.10.2 → 8.11.1. No user-facing behavior changes.

## [0.1.1] - 2026-07-11

### Fixed
- No more false "Touchgrass stopped working" notifications. Service liveness is now
  read from the accessibility binding rather than heartbeat age, so simply not
  opening a blocked app for a few hours no longer looks like a crash.
- The app no longer re-asks for the Accessibility permission when it is already
  granted. Enabled-service detection now compares normalized component names
  instead of brittle string matching that missed short-form component names.

## [0.1.0] - 2026-05-30

- Initial release: blocks Reels, Shorts, TikTok, and Spotlight; Trust Dashboard;
  commitment lock; reliability watchdog.
