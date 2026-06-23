# Essential Key Remapper

An Android app for remapping the Nothing/CMF Essential Key. It detects the key through an Accessibility Service and assigns separate actions to single, double, and long presses.

**Website:** https://wreck2053.github.io/essential-key/

**Setup guide:** https://wreck2053.github.io/essential-key/setup/

**Downloads:** https://github.com/wreck2053/essential-key/releases

> Unofficial community project. Not affiliated with or endorsed by Nothing Technology Limited.

## Actions

- HTTP GET or POST with a separate base URL for each press gesture
- Toggle flashlight
- Set normal, vibrate, silent, or toggle silent/normal
- Launch an installed app
- Open a URL
- Screenshot
- Lock screen
- Power menu
- Play/pause, next, or previous media
- Open camera or assistant
- Notifications
- Quick Settings
- Home, Back, or Recents
- Haptic-only/no action

## Setup

### 1. Enable Developer options

Open **About phone → Software info → Build number**, tap Build number seven times, then keep the main Developer options switch enabled.

### 2. Release the Essential Key

Nothing OS normally consumes the key through Essential Space and Essential Recorder. The app includes a local Wireless ADB setup flow that disables these handlers for the current Android user.

1. Tap **Open Wireless debugging setup**.
2. Confirm the package change.
3. Enable **Wireless debugging** if needed.
4. Tap **Pair device with pairing code**.
5. Enter the six-digit code using the Essential Key setup notification.

The app executes only these allowlisted operations:

```text
pm disable-user --user 0 com.nothing.ntessentialspace
pm disable-user --user 0 com.nothing.ntessentialrecorder
```

The packages and their data are not deleted. **Restore Essential Space** reverses the change with `pm enable`.

Uninstalling the remapper does not restore the Essential Key. Restore Essential Space from the Setup page before uninstalling.

Android requires Developer Options, Wireless Debugging, and pairing approval. The app cannot bypass those system confirmations. Wireless Debugging can be turned off after setup.

### 3. Enable the button listener

The Accessibility Service receives and consumes only hardware `KeyEvent` objects. It does not inspect screen content, type text, or collect accessibility data.

Sideloaded APKs on Android 13+ may require **App info → ⋮ → Allow restricted settings** before the service can be enabled.

### 4. Detect and configure

Detect the Essential Key once, choose one action for each gesture, select haptic strength, and save.

Normal and vibrate modes use standard audio controls. On Nothing OS, changes involving silent mode may require Notification Policy access.

## Gesture timing

- Single press: fires after a 300 ms double-press window.
- Double press: two short presses within that window.
- Long press: fires after holding for 600 ms.

## Compatibility and limitations

- Embedded Wireless ADB setup requires Android 11 or newer.
- The app targets Nothing/CMF devices containing the Essential Space packages.
- Android or Nothing OS updates may change package names, key delivery, background activity behavior, or Wireless Debugging behavior.
- Flashlight access can fail while the camera is in use.
- Launch-app and open-URL actions remain subject to Android background activity restrictions.
- Google Play distribution requires an Accessibility Service declaration and prominent disclosure. Automatic package changes may also receive policy review.

## Build

```text
./gradlew testDebugUnitTest lintDebug assembleDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## License

MIT
