#!/usr/bin/env bash

set -euo pipefail

adb install -r app/build/outputs/apk/release/app-release.apk
adb logcat -c
adb shell rm -f /sdcard/bareuang-window.xml

start_result="$(adb shell am start -W -n com.ssajudn.bareuang/.MainActivity)"
printf '%s\n' "$start_result"
printf '%s\n' "$start_result" | grep -q 'Status: ok'

ui_dump=""
attempt=0
while (( attempt < 12 )); do
    attempt=$((attempt + 1))
    adb shell uiautomator dump /sdcard/bareuang-window.xml >/dev/null 2>&1 || true
    ui_dump="$(adb shell cat /sdcard/bareuang-window.xml 2>/dev/null | tr -d '\r' || true)"
    if [[ "$ui_dump" == *"Bareuang"* ]]; then
        break
    fi
    if adb logcat -d -s AndroidRuntime:E | grep -q 'FATAL EXCEPTION'; then
        echo "Release APK crashed during launch"
        adb logcat -d -s AndroidRuntime:E
        exit 1
    fi
    sleep 1
done

if [[ "$ui_dump" != *"Bareuang"* ]]; then
    echo "Release APK did not render the expected Bareuang UI content"
    adb shell dumpsys activity activities | tail -n 80
    adb logcat -d -s AndroidRuntime:E
    exit 1
fi
