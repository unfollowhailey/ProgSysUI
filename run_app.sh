#!/bin/bash

# 🚀 Script de lancement - SecurePhone Application
# Usage: ./run_app.sh

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$SCRIPT_DIR"

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                                                                ║"
echo "║              🚀 Lancement de SecurePhone                       ║"
echo "║                                                                ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Vérifier si Maven est installé
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven non trouvé. Veuillez installer Maven:"
    echo "   Ubuntu/Debian: sudo apt-get install maven"
    echo "   macOS: brew install maven"
    echo "   Windows: https://maven.apache.org/download.cgi"
    exit 1
fi

echo "📦 Compilation du serveur..."
cd "$PROJECT_DIR/serveur"
mvn clean compile -q

echo "📦 Compilation du client..."
cd "$PROJECT_DIR/client"
mvn clean compile -q

echo "✅ Compilation réussie!"
echo ""

echo "🖥️  Lancement du serveur (logs: serveur/target/server.log)..."
cd "$PROJECT_DIR/serveur"
mvn exec:java \
    -Dexec.mainClass="com.securephone.server.MainServer" \
    -q > target/server.log 2>&1 &
SERVER_PID=$!

cleanup() {
    echo ""
    echo "🛑 Arrêt du serveur..."
    kill $SERVER_PID >/dev/null 2>&1 || true
}

trap cleanup EXIT

sleep 1

echo "🎮 Lancement de l'application client..."
echo ""
cd "$PROJECT_DIR/client"
mvn exec:java \
    -Dexec.mainClass="com.securephone.client.SecurePhoneApp" \
    -q
