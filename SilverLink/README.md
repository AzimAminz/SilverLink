# SilverLink — Native Android

Native Kotlin + XML implementation of the [SilverLink Figma design](https://www.figma.com/design/AEAK5cCMDkUVtAx9C2v8WT/Hackathon---SilverLink).

## Open and run

Open this directory (`SilverLink`, containing `settings.gradle.kts`) in Android Studio.
Use a full **JDK 21**, the existing Android SDK 37 configuration, and an Android 7.0+
device or emulator. Gradle daemon criteria are configured for JDK 21.

```sh
./gradlew assembleDebug
./gradlew testDebugUnitTest lintDebug
```

The debug APK is `app/build/outputs/apk/debug/app-debug.apk`.

## Try the preview

1. Tap **Login → Requester → Enter demo** to open Priya’s home.
2. Tap **Request help for Mum**, review the form, and **Broadcast request**.
3. Tap the **Requester** badge in the app bar to switch to **Helper**.
4. Open and accept the grocery request, add completion notes, optionally replace
   the sample photo using Android’s document picker, and confirm help was received.
5. Send for verification, switch back to **Requester**, and open **Requests**.
6. Confirm with Mum before verifying. The helper earns the category’s fixed award
   exactly once. The default grocery scenario moves from 280 to 320 points.

The EN/BM control uses native Android app locales. User-entered task descriptions
and the Figma sample content in the ViewModel remain content rather than translations.

## Code structure

- `MainActivity.kt`: single activity, fragment navigation, system insets and role switching.
- `DemoViewModel.kt`: explicit request state and SavedStateHandle; drafts, chosen images
  and awarded points survive activity recreation and normal saved-state restoration.
- `ScreenFragment.kt`: lifecycle-scoped View Binding and bounded evidence image decoding.
- `AuthFragments.kt`, `RequesterFragments.kt`, `HelperFragments.kt`, `RewardsFragment.kt`:
  focused UI event handlers.
- `app/src/main/res/layout/`: nine XML screens and two native dialog layouts.
- `app/src/main/res/values/`: shared palette, typography, component styles and English copy.
- `app/src/main/res/values-ms/`: Malay resources.
- `design/figma-assets/`: original Figma assets; runtime photos and rasterised icons are
  bundled in Android drawable resources. Identical icons share a runtime resource.

The supplied signup frame contains only a header. Its form is an implementation
addition to make the preview usable. Native status/navigation bars replace static
Figma system indicators. Tall screens scroll on standard phones and large fonts.

## Integration boundaries

This is a local UI preview. Authentication, Google OAuth, management verification,
messaging, notifications, real location tracking and authoritative reward transactions
require backend integration. The route illustration is the original Figma artwork.
The app does not request network or broad storage permissions.

The two secondary nearby tasks and profile use informational dialogs because the
source file supplies no dedicated detail/profile screens. Demo state is not a database
or an account and is not intended to persist after an explicit fresh app launch.

Screenshots from the Android emulator are saved in `docs/screenshots/`.
