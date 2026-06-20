# Essential Key HTTP Mapper

A Material 3 Android app that maps one observable hardware key to local HTTP requests and haptic feedback for single, double, and long presses. It is designed for the Nothing Phone (3a) Essential Key, which may appear to Android as `KEYCODE_UNKNOWN` (`keyCode=0`).

## Install without Android build tools

1. Open the latest successful **Android build** run on this repository's Actions page.
2. Download the `essential-key-debug-apk` artifact.
3. Unzip it and transfer `app-debug.apk` to the phone.
4. Allow installation from the browser or file manager when Android prompts, then install the APK.

All compilation happens in GitHub Actions. Editing and pushing source files locally does not require Android Studio, Java, Gradle, or the Android SDK.

## Configure

1. In the setup panel, tap **Open Accessibility** and try to enable **Essential Key button listener**.
2. If Android blocks it, return to the app and tap **Open App info**. Open the top-right three-dot menu and tap **Allow restricted settings**. This Android 13+ step is required for APKs installed outside Google Play; signing the APK does not bypass it.
3. Return to **Open Accessibility** and enable the listener. The app never advances setup from a tap; the panel turns green only after Android reports that the service is enabled.
4. Tap **Detect hardware button** and press the Essential Key once.
5. Set the controller base URL and edit the GET/POST path for each gesture. A complete URL can be used instead of a path.
6. Choose the shared haptic strength, preview it if needed, then tap **Save actions**. Leaving a path blank creates a haptic-only action.

Defaults target [`wreck2053/home-automation`](https://github.com/wreck2053/home-automation):

- Controller: `http://192.168.0.108`
- Single press: `GET /toggle-light`
- Double press: `GET /preset-ac`
- Long press: `GET /toggle-fan`

Button detection is intentionally disabled until Android reports that the Accessibility Service is enabled. If the **Allow restricted settings** menu item is not visible, first try enabling the service once, accept the denial, and return to App info.

POST sends an empty request body. The app shows the latest HTTP status or error below each action. Requests use a 5-second connection timeout and 10-second read timeout. Light, Medium, and Strong haptics use progressively longer and higher-amplitude pulses, with duration-based fallback on devices without amplitude control. Haptics respect the system touch-feedback setting.

The interface uses dynamic Material You color on Android 12+, supports light/dark mode, and adapts from compact phones to landscape and tablet windows.

## Gesture timing

- Single press: fires after a 300 ms double-press window.
- Double press: two short presses within that window.
- Long press: fires after holding for 600 ms.

## Android and Nothing OS limitations

The Accessibility Service asks Android to filter global hardware key events and consumes both down and up events for the mapped key. Android does not expose every physical button: Power, Home, and some OEM-reserved events may be unavailable. Nothing OS may also run Essential Space through an OEM path even when the accessibility event is consumed. Behavior can change with Nothing OS updates.

The service's accessibility-event callback intentionally does nothing; it observes only hardware `KeyEvent` objects. Cleartext HTTP is enabled because local-network devices commonly do not provide TLS.

## Development

GitHub Actions runs:

```text
./gradlew testDebugUnitTest lintDebug assembleDebug
```

The workflow then uploads `app/build/outputs/apk/debug/app-debug.apk` as `essential-key-debug-apk`.

## License

MIT
