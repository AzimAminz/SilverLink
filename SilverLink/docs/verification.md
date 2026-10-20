# Native Android verification — 19 October 2026

`./gradlew assembleDebug testDebugUnitTest lintDebug --console=plain` completed
successfully with JDK 21. The unit suite contains three passing tests, including
two request-state tests covering prerequisite checks, duplicate verification,
fixed category awards, saved state and the published/draft boundary.

Android lint reports zero errors. Remaining warnings concern optional layout
optimisations, quantity-string suggestions, a newer Gradle release, and the
Android 13+ locale configuration attribute. They are not suppressed by a baseline.

The Pixel 5 emulator was used to exercise:

- Welcome, signup, EN/BM switching, home and the request form.
- Broadcast navigation, role switching and helper acceptance.
- The original route illustration and tracking card.
- Completion submission, requester verification and the 280 → 320 point award.
- Reopening verification without awarding points a second time.
- Rotation and background process recreation, preserving the current screen and points.
- Category typography and filtering to exactly one request with the singular label.

Native screenshots are in `screenshots/`. The supplied Figma frames are 412 dp wide;
the implementation scrolls and wraps content at the emulator’s available width.
System bars are the device’s native bars.

Backend services and real account, notification, messaging, map and reward operations
are outside this local preview. See the README for integration boundaries.
