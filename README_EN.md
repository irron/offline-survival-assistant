# Doomsday Toolbox

A local-first survival assistant app for offline, outdoor, disaster, and emergency scenarios.

## Features

- `AI Survival Assistant`: runs locally after model download, with text and image input support.
- `Offline Knowledge Base`: fire-starting, water sourcing, shelter building, first aid, and more.
- `SOS Module`: flashlight SOS, screen flashing, and Morse code signal sending.
- `Survival Tools`: compass and GPS display.
- `Model Management`: download and auto-activate the local model.

## Tech Stack

- Kotlin
- MVVM
- AndroidX + Material
- Offline local inference
- MNN / Qwen3.5-2B

## Requirements

- Android 8.0+
- 64-bit ARM device
- Network access is recommended for the initial model download

## Permissions

- `INTERNET`: downloads the local model
- `ACCESS_NETWORK_STATE`: checks network availability
- `ACCESS_FINE_LOCATION`: displays GPS data
- `CAMERA` / `FLASHLIGHT`: SOS flashlight blinking

## Privacy & Safety

- The app is designed to work primarily on-device
- Images, chat content, and model files are stored locally on the device
- This app is for reference only and does not replace professional rescue, medical, or official guidance

## Build

```powershell
git clone https://github.com/irron/offline-survival-assistant.git
cd offline-survival-assistant
./gradlew assembleRelease
```

On Windows:

```powershell
.\gradlew.bat assembleRelease
```

## Release Artifacts

- `AAB`: `app/build/outputs/bundle/release/app-release.aab`
- `APK`: `app/build/outputs/apk/release/app-release.apk`

## Notes

- Downloaded models are automatically set as the active model.
- The project is structured for future expansion into a fuller emergency toolkit.
