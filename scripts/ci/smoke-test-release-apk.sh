#!/usr/bin/env bash

set -euo pipefail

adb install -r app/build/outputs/apk/release/app-release.apk
adb logcat -c

start_result="$(adb shell am start -W -n com.ssajudn.bareuang/.MainActivity)"
printf '%s\n' "$start_result"
printf '%s\n' "$start_result" | grep -q 'Status: ok'

sleep 8
if adb logcat -d -s AndroidRuntime:E | grep -q 'FATAL EXCEPTION'; then
    echo "Release APK crashed during launch"
    exit 1
fi
