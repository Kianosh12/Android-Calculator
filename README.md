# Android Calculator

A minimal, fast calculator for Android — built with Kotlin and Jetpack Compose.

<p align="center">
  <em>No ads, no tracking, no network access. Fully offline.</em>
</p>

## Features

- Standard arithmetic: `+`, `−`, `×`, `÷`, parentheses and `%`
- Live result preview while you type
- `±` to flip the sign of the current number
- `⌫` to delete, `AC` to clear
- Percentage follows the usual calculator convention: `100 + 5%` is `105`,
  while a standalone `50%` is `0.5`
- Haptic feedback on every key press
- Dark theme, edge-to-edge, rotates without losing your calculation
- Tiny and dependency-light — no ads SDK, no permissions

## Screenshots

| Main screen | Example |
|---|---|
| `0` | `12.5 × 4 + 8` → preview `58` |

## Requirements

- Android 8.0 (API 26) or newer
- The app requests **no permissions**

## Install the prebuilt APK

A signed release APK is attached to the
[latest successful run](https://github.com/Kianosh12/Android-Calculator/actions/workflows/android.yml).

1. Download the `android-calculator-release` artifact from the Actions tab
   (or `gh run download --name android-calculator-release`).
2. Copy `app-release.apk` to your phone.
3. Open it and allow your file manager to "install unknown apps" — Android asks
   for this permission the first time you sideload an app.
4. The installed app is called **Calculator**.

## Build from source

### Using Android Studio

1. Open the project root (`Android-Calculator/`) in Android Studio Hedgehog or newer.
2. Let it sync (Gradle 8.7 / AGP 8.5.2 / JDK 17).
3. Run the `app` configuration on a device or emulator.

### Using the command line

```bash
git clone https://github.com/Kianosh12/Android-Calculator.git
cd Android-Calculator

# Debug build (auto-signed with the debug key, installable on any device)
./gradlew assembleDebug
# -> app/build/outputs/apk/debug/app-debug.apk

# Release build (signed with your own key, see below)
./gradlew assembleRelease
# -> app/build/outputs/apk/release/app-release.apk
```

Install it with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Release signing

Create a keystore once:

```bash
keytool -genkey -v -keystore release.keystore -alias release \
        -keyalg RSA -keysize 2048 -validity 10000
```

Then add a `keystore.properties` file in the project root (it is git-ignored):

```properties
storeFile=release.keystore
storePassword=...
keyAlias=release
keyPassword=...
```

`./gradlew assembleRelease` now produces a signed, installable APK.

## GitHub Actions

Pushing to `main` (or opening a pull request) runs the
[Build APK](.github/workflows/android.yml) workflow. It produces:

| Artifact | Notes |
|---|---|
| `android-calculator-debug` | Always built. Debug-signed, ready to sideload. |
| `android-calculator-release` | Built only when the `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD` repository secrets are set. |

Download them from the **Actions** tab, or with the GitHub CLI:

```bash
gh run download --name android-calculator-debug
```

## Project layout

```
app/src/main/java/com/kianosh12/androidcalculator/
├── Calculator.kt          # expression parser / evaluator (pure Kotlin, unit-testable)
├── MainActivity.kt        # Compose UI and calculator state handling
└── ui/theme/Theme.kt      # colours and Material theme
```

The expression engine in `Calculator.kt` is a self-contained recursive-descent
parser — it has no Android dependencies, so it can be unit-tested on the JVM.

## License

MIT
