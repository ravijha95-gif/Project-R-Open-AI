# PROJECT R — Phase 1

A Kotlin + Jetpack Compose Android launcher foundation for a personal Windows-PC-game compatibility project.

## Phase 1 features
- Dark PROJECT R launcher UI (Home, Library, Settings)
- Import a game folder with Android's Storage Access Framework (`ACTION_OPEN_DOCUMENT_TREE`)
- Persist folder URI permissions and a lightweight local game library
- Game detail screen and Phase 2 placeholders
- GitHub Actions workflow to build a debug APK

## Important scope note
This phase is a launcher and library only. It does **not** run `.exe` files or include Wine, Box86/Box64, Mesa, Winlator components, game files, or proprietary Windows files. The compatibility engine is planned for Phase 2.

## Build locally
Requirements: Android Studio with Android SDK 35 and JDK 17.

1. Open this folder in Android Studio.
2. Allow Gradle sync to complete.
3. Connect an Android device or start an emulator.
4. Run the `app` configuration, or use `gradle assembleDebug`.

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Build with GitHub Actions
1. Create a GitHub repository and upload the contents of this folder.
2. Push to `main`/`master`, or run **Build PROJECT R APK** from Actions → Run workflow.
3. Download the `PROJECT-R-debug-apk` artifact from the completed workflow run.

## Next phases
- Phase 2: evaluate and integrate a compatible Winlator/Wine engine for ARM64 Mali devices, subject to upstream licences and build compatibility.
- Phase 3: game installer and executable discovery.
- Phase 4: graphics profiles and per-game configuration.
- Phase 5: touch controls and controller mapping.

## Licensing
PROJECT R's original Phase 1 source is provided for personal project use. Any upstream engine components integrated later retain their own licences and notices. Do not redistribute third-party game files unless you have the rights to do so.
