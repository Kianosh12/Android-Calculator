#!/usr/bin/env bash
#
# Makes sure the Android SDK contains everything the build needs:
# platform-tools, platforms;android-34 and build-tools;34.0.0.
#
# The GitHub runner image ships the SDK but does not always put `sdkmanager`
# on the PATH, so we locate it (and install the command line tools as a last
# resort) before asking it to install the packages we need.
set -euo pipefail

SDK_ROOT="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/usr/local/lib/android/sdk}}"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"

echo "::notice::Android SDK root: $SDK_ROOT"

find_sdkmanager() {
  local candidate
  for candidate in \
    "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" \
    "$SDK_ROOT"/cmdline-tools/*/bin/sdkmanager \
    "$SDK_ROOT/tools/bin/sdkmanager"; do
    if [ -x "$candidate" ]; then
      echo "$candidate"
      return 0
    fi
  done
  return 1
}

install_cmdline_tools() {
  echo "::notice::sdkmanager not found - installing the Android command line tools"
  mkdir -p "$SDK_ROOT/cmdline-tools"
  local tmp
  tmp="$(mktemp -d)"
  curl -fsSL -o "$tmp/cmdline-tools.zip" "$CMDLINE_TOOLS_URL"
  unzip -q "$tmp/cmdline-tools.zip" -d "$tmp"
  rm -rf "$SDK_ROOT/cmdline-tools/latest"
  mv "$tmp/cmdline-tools" "$SDK_ROOT/cmdline-tools/latest"
  rm -rf "$tmp"
}

SDKMANAGER="$(find_sdkmanager || true)"
if [ -z "$SDKMANAGER" ]; then
  install_cmdline_tools
  SDKMANAGER="$(find_sdkmanager)"
fi

echo "::notice::Using sdkmanager: $SDKMANAGER"

# Accept all SDK licenses so that the packages can be installed unattended.
yes | "$SDKMANAGER" --licenses > /dev/null 2>&1 || true

"$SDKMANAGER" "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo "::notice::Installed platforms:"
ls "$SDK_ROOT/platforms" || true
echo "::notice::Installed build-tools:"
ls "$SDK_ROOT/build-tools" || true
