# CRE Studio 1.0

Creative · Resilient · Evolving — a mobile music studio with 3D buttons.

## Build the APK (GitHub Actions)
1. Replace the contents of your CRE-Studio repo with this folder (keep the `.git` folder), commit and push to `main`.
2. Open the repo > Actions > "Build CRE Studio APK" > latest run > download the `CRE-Studio-debug` artifact.
3. Unzip it and install `app-debug.apk` (uninstall the old prototype first — the signing key differs).

## What's inside
- BEATS: 8-channel x 16-step drum sequencer, swing, playhead
- KEYS: piano roll (24 notes), tap the keys on the left to play, synth with 4 waveforms, filter, delay
- PADS: finger drums; arm REC + PLAY to record pads into the beat
- MIXER: faders, LED meters, mute/solo per channel
- Menu: starter beats, save/load projects, export WAV (4 bars) and share, autosave
