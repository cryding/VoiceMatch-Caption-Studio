# VoiceMatch Caption Studio

Offline-first Android starter for local video selection, caption editing, style preview and SRT export.

## Current scope
- Pick a local video with Android's document picker.
- Edit transcript/caption text.
- Choose a basic caption style and preview sample text.
- Export an SRT subtitle file.
- GitHub Actions debug APK build artifact.

## Not implemented yet
Automatic speech recognition, reference-voice matching/cloning, narration generation, word-level alignment, and burned-in MP4 rendering are **not** implemented in this starter. SRT timing is estimated from text chunks, not derived from audio. Do not describe these features as complete.

## Planned offline pipeline
1. Decode local video/audio.
2. Integrate and benchmark whisper.cpp for local transcription.
3. Integrate a TTS/voice-conversion runtime proven compatible with Android.
4. Add word-level alignment and editable caption timeline.
5. Add local MP4 rendering using a compatible, properly licensed renderer.
6. Export MP4 + SRT/VTT + WAV.

OpenVoice is an MIT-licensed candidate upstream, but its documented language coverage and Android deployment constraints must be tested before claiming voice cloning works on-device. Bengali needs its own verified model/runtime.

## Build
JDK 17, Android SDK Platform 35, Android Gradle Plugin 8.9.1 and Gradle 8.11.1. Run `gradle assembleDebug` in a configured Android environment. GitHub Actions uploads a debug APK artifact; no production-signed release APK is available yet.

## Licensing
Starter code: MIT. No third-party model weights are bundled. Check model/runtime licences and preserve notices before distribution.