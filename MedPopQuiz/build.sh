#!/bin/bash

# MedPop Quiz Build Script

echo "================================"
echo "Building MedPop Quiz Android App"
echo "================================"

# Check if Android SDK is available
if [ -z "$ANDROID_SDK_ROOT" ] && [ -z "$ANDROID_HOME" ]; then
    echo "Warning: ANDROID_SDK_ROOT or ANDROID_HOME not set"
    echo "Please set your Android SDK path"
fi

# Clean build
echo "Cleaning previous build..."
./gradlew clean

# Build debug APK
echo "Building debug APK..."
./gradlew assembleDebug

# Check if build succeeded
if [ $? -eq 0 ]; then
    echo ""
    echo "================================"
    echo "Build Successful!"
    echo "================================"
    echo ""
    echo "APK location:"
    echo "  app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "To install on device:"
    echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "================================"
    echo "Build Failed!"
    echo "================================"
    exit 1
fi
