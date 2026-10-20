# SilverLink native Android implementation

Source: Figma AEAK5cCMDkUVtAx9C2v8WT, Page 1. User requested Kotlin and XML views.

## Architecture

Single AppCompatActivity, XML fragments, Material components and View Binding. Shared
SavedStateHandle ViewModel holds the local demonstration state across recreation.
Native system bars replace the static Figma status bars and gesture handles.
Original Figma photos and icons are bundled locally. No network permission is needed.

## Work

- [x] Add design tokens, original assets, shared app bar and bottom navigation.
- [x] Implement welcome, signup, requester home and request form.
- [x] Implement tracking, nearby requests, accepted task, review and rewards.
- [x] Connect local request state, role switching, validation and photo picker.
- [x] Build, lint and exercise navigation and recreation in the Android emulator.

## Boundaries

Authentication, Google identity, resident verification, messaging, live tracking and
reward transactions require backend integration. The welcome screen clearly offers
a demo entry after explaining this. Signup content is a practical native form because
the supplied Figma signup frame contains only a header. Secondary task details and
profile use native dialogs because no corresponding Figma screens were supplied.
