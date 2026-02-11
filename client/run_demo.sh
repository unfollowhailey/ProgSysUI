#!/usr/bin/env bash
# Run the SecurePhone UI demo with MP3 sounds (MainUIDemo)
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

JAR_PATH="lib/jlayer-1.0.1.jar"

# Ensure JLayer is available
if [[ ! -f "$JAR_PATH" ]]; then
  echo "[info] JLayer missing, downloading..."
  ./download_jlayer.sh
fi

# Compile if target/classes is missing
if [[ ! -d "target/classes" ]]; then
  echo "[info] Compiling client classes (Maven)..."
  mvn -q -DskipTests compile
fi

echo "[info] Launching MainUIDemo..."
java -cp "${JAR_PATH}:target/classes" com.securephone.client.MainUIDemo
