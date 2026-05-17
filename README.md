# FairTriage

FairTriage is an Android MVP prototype for AI-assisted clinical triage. It lets a triage doctor log in, add patients, view a risk-scored queue, inspect AI decision rationales, override triage levels, complete patients, and review decision logs.

Prototype only. Not for real medical diagnosis.

Android package id: `com.aliumitalgan.fairtriage`.

## Project Shape

The runnable app code is in the `composeApp` folder and is exposed to Gradle as the `androidApp` module so Android Studio's default Android run configuration works.

This repository was simplified to a standard Android Compose app because AGP 9 does not support a single module that is both `com.android.application` and `org.jetbrains.kotlin.multiplatform`. The app still uses Compose UI, Voyager navigation/screen models, Ktor, Kotlinx Serialization, and Coroutines, and it is Android-only as requested.

The active Android app source folder is:

```text
composeApp/
  src/main/kotlin/com/fairtriage/
    core/
    model/
    repository/
    screenmodel/
    ui/
  src/main/AndroidManifest.xml
```

The old generated `androidApp` folder is not used; `settings.gradle.kts` maps the Gradle module `:androidApp` to the `composeApp` folder.

## Backend URL

The emulator URL is configured in:

```text
composeApp/src/main/kotlin/com/fairtriage/core/Constants.kt
```

Default for the current physical-device setup:

```kotlin
const val BASE_URL = "http://192.168.0.18:500"
```

For an Android emulator, use:

```kotlin
const val BASE_URL = "http://10.0.2.2:500"
```

## Open in Android Studio

1. Open the repository root folder in Android Studio.
2. Let Gradle sync.
3. Select the `androidApp` run configuration, or create an Android App configuration for module `FairTriage.androidApp`.
4. Start the FastAPI backend on port `500`.
5. Run the app on an Android emulator.

## Run from Terminal

Build the debug APK:

```powershell
.\gradlew.bat :androidApp:assembleDebug -x test
```

On macOS/Linux:

```bash
./gradlew :androidApp:assembleDebug -x test
```

## Device Notes

Use `http://192.168.0.18:500` from the physical Android phone while the backend is running on the same local network. Use `http://10.0.2.2:500` only for the Android emulator. The manifest enables internet access and cleartext HTTP for this local prototype.
