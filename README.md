# SmartConnect — Bluetooth Scanner & NFC Tap-to-Share

A modern Android app built with **Kotlin**, **Jetpack Compose**, **MVVM + Clean Architecture**, and **Firebase** for Bluetooth device discovery and NFC-based data sharing.

## Features

- **Bluetooth Scanner** — Discover nearby BT devices with real-time signal strength, device type icons, and one-tap pairing
- **Paired Devices** — View all paired devices, send test data via BT socket
- **NFC Tap & Share** — Read NFC tags, write payloads (text, URL, contact, WiFi creds, custom data) to tags
- **Transfer History** — All BT/NFC transfers logged in Room DB with Firebase Firestore sync
- **Runtime Permissions** — Accompanist permissions for BT/NFC on Android 12+

## Architecture

```
app/                          ← UI layer (Compose, ViewModels, Navigation, Hilt DI)
core/
  ├── common/                 ← Shared utilities (Resource, Constants)
  ├── domain/                 ← Models, Repository interfaces, Use Cases
  └── data/                   ← Bluetooth controller, NFC controller, Room, Firebase, Mappers
```

## Tech Stack

| Layer       | Technology                                                      |
|------------|----------------------------------------------------------------|
| UI         | Jetpack Compose, Material 3, Accompanist Permissions           |
| State      | StateFlow, collectAsStateWithLifecycle                         |
| Async      | Kotlin Coroutines + Flow                                       |
| DI         | Dagger Hilt                                                    |
| Bluetooth  | Android BluetoothAdapter, BroadcastReceiver, RFCOMM Socket     |
| NFC        | NfcAdapter, NDEF read/write, Foreground Dispatch               |
| Database   | Room                                                           |
| Cloud      | Firebase Firestore (transfer history sync)                     |
| Build      | Gradle Kotlin DSL, Version Catalog                             |

## Setup

1. Clone and open in Android Studio
2. Add `google-services.json` from Firebase Console to `app/` folder
3. Run on a **physical device** (Bluetooth/NFC don't work on emulators)

## Screens

1. **Scanner** — Tap FAB to scan, see devices with signal bars, tap to connect
2. **Paired** — List of bonded devices with send button
3. **NFC** — Status card, payload type selector, read/write buttons
4. **History** — Transfer log with direction arrows, cloud sync button
