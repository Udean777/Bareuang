#!/usr/bin/env bash

set -euo pipefail

: "${EXPECTED_API_LEVEL:?EXPECTED_API_LEVEL must be set by the workflow matrix}"

adb wait-for-device

api_level=""
for attempt in $(seq 1 30); do
    api_level="$(adb shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r' || true)"
    if [[ "$api_level" == "$EXPECTED_API_LEVEL" ]]; then
        break
    fi
    sleep 2
done

if [[ "$api_level" != "$EXPECTED_API_LEVEL" ]]; then
    echo "Expected emulator API $EXPECTED_API_LEVEL, got '${api_level:-unavailable}'"
    adb devices -l
    exit 1
fi

adb shell svc power stayon true
adb shell settings put system screen_off_timeout 2147483647
adb shell input keyevent 82

./gradlew :data:connectedDebugAndroidTest :app:connectedDebugAndroidTest --no-daemon --max-workers=2
