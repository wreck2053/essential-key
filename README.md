# Essential Key HTTP Mapper

A small Android app that maps one observable hardware key to local HTTP requests for single, double, and long presses. It is designed for the Nothing Phone (3a) Essential Key, which may appear to Android as `KEYCODE_UNKNOWN` (`keyCode=0`).

## Install without Android build tools

1. Open the latest successful **Android build** run on this repository's Actions page.
2. Download the `essential-key-debug-apk` artifact.
3. Unzip it and transfer `app-debug.apk` to the phone.
4. Allow installation from the browser or file manager when Android prompts, then install the APK.

All compilation happens in GitHub Actions. Editing and pushing source files locally does not require Android Studio, Java, Gradle, or the Android SDK.

## Configure

1. Open the app and tap **Open accessibility settings**.
2. Enable **Essential Key button listener**, then return to the app.
3. Tap **Detect hardware button** and press the Essential Key once.
4. Enter a local `http://` or `https://` URL and select GET or POST for each gesture.
5. Tap **Save actions**.

POST sends an empty request body. The app shows the latest HTTP status or error below each action. Requests use a 5-second connection timeout and 10-second read timeout.

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
./gradlew testDebugUnitTest assembleDebug
```

The workflow then uploads `app/build/outputs/apk/debug/app-debug.apk` as `essential-key-debug-apk`.

## License

MIT

