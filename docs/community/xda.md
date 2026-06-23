# XDA post draft

## Thread title

[APP][Android 11+] Essential Key Remapper for Nothing/CMF — single, double and long press

## Body

Essential Key Remapper is an open-source utility for assigning independent single, double, and long-press actions to the Nothing/CMF Essential Key.

### Features

- Flashlight, camera, assistant, apps and URLs
- Screenshot, lock, power menu, notifications and Quick Settings
- Home, Back and Recents
- Media play/pause, next and previous
- Sound modes and haptic feedback
- HTTP GET/POST actions for local automation

### Setup

The app includes a local Wireless ADB client, so setup does not require a PC or terminal. The user explicitly enables Wireless Debugging and enters Android's six-digit pairing code. Shell execution is restricted to disabling or restoring:

- `com.nothing.ntessentialspace`
- `com.nothing.ntessentialrecorder`

Important: restore Essential Space from the app before uninstalling, because uninstalling alone does not reverse the package-state change.

Website:
https://wreck2053.github.io/essential-key/

Setup guide:
https://wreck2053.github.io/essential-key/setup/

Source:
https://github.com/wreck2053/essential-key

The project is unofficial and not affiliated with Nothing Technology. Please include device model, Android version, and Nothing OS version in compatibility reports.
