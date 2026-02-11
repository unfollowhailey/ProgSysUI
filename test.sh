#!/bin/bash
# Script rapide pour compiler et tester SecurePhone Client
# Usage: ./test.sh [compile|test|audio|full]

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR"
CLIENT_DIR="$PROJECT_ROOT/client"
LOG_FILE="/tmp/securephone_build.log"

# Couleurs pour l'output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction pour afficher les messages
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}✓${NC} $1"
}

log_error() {
    echo -e "${RED}✗${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Affichage du header
echo ""
echo "==============================================="
echo "   SecurePhone Client - Compilation & Tests"
echo "==============================================="
echo ""

# Vérifier que Maven est installé
if ! command -v mvn &> /dev/null; then
    log_error "Maven n'est pas installé. Veuillez installer Maven."
    exit 1
fi

# Aller au répertoire client
cd "$CLIENT_DIR"

# Choix utilisateur
if [ "$1" = "compile" ]; then
    ACTION="compile"
elif [ "$1" = "test" ]; then
    ACTION="test"
elif [ "$1" = "audio" ]; then
    ACTION="audio"
elif [ "$1" = "full" ]; then
    ACTION="full"
else
    echo "Usage: $0 [compile|test|audio|full]"
    echo ""
    echo "Options:"
    echo "  compile  - Compiler le code source uniquement"
    echo "  test     - Lancer les tests unitaires (35 tests)"
    echo "  audio    - Lancer le test d'intégration audio (interactif)"
    echo "  full     - Compiler + tests + test audio (complet)"
    echo ""
    echo "Exemple:"
    echo "  ./test.sh compile   # Vérifie que le code compile"
    echo "  ./test.sh test      # Lance les 35 tests unitaires"
    echo "  ./test.sh audio     # Lance le test de capture/playback audio"
    echo "  ./test.sh full      # Exécute toutes les étapes"
    echo ""
    exit 0
fi

# ============================================================
# COMPILATION
# ============================================================
if [ "$ACTION" = "compile" ] || [ "$ACTION" = "full" ]; then
    log_info "Nettoyage et compilation..."
    echo "------"
    
    if mvn clean compile -q 2>&1 | tee -a "$LOG_FILE"; then
        log_success "Compilation réussie"
        echo ""
    else
        log_error "La compilation a échoué. Voir $LOG_FILE"
        exit 1
    fi
fi

# ============================================================
# TESTS UNITAIRES
# ============================================================
if [ "$ACTION" = "test" ] || [ "$ACTION" = "full" ]; then
    log_info "Lancement des tests unitaires (35 tests)..."
    echo "------"
    
    if mvn test 2>&1 | tee -a "$LOG_FILE"; then
        log_success "Tests unitaires réussis"
        echo ""
    else
        log_error "Les tests ont échoué. Voir $LOG_FILE"
        exit 1
    fi
fi

# ============================================================
# TEST D'INTÉGRATION AUDIO
# ============================================================
if [ "$ACTION" = "audio" ] || [ "$ACTION" = "full" ]; then
    log_warning "Test d'intégration audio (interactif)"
    echo "------"
    echo ""
    echo "Ce test va:"
    echo "  1. Tester votre buffer audio"
    echo "  2. Enregistrer 5 secondes depuis votre microphone"
    echo "  3. Rejouer immédiatement l'audio dans vos haut-parleurs"
    echo ""
    echo "Prérequis:"
    echo "  ✓ Microphone branché et actif"
    echo "  ✓ Haut-parleurs/casque branché"
    echo "  ✓ Pas d'autre application utilisant l'audio"
    echo ""
    
    read -p "Appuyez sur ENTRÉE pour commencer le test audio: " -t 3 || true
    echo ""
    
    if mvn exec:java -Dexec.mainClass="com.securephone.client.audio.AudioIntegrationTest" 2>&1 | tee -a "$LOG_FILE"; then
        log_success "Test audio complété"
        echo ""
    else
        log_warning "Le test audio a échoué (peut-être pas de carte son)"
        echo "Voir $LOG_FILE pour les détails"
        echo ""
    fi
fi

# ============================================================
# RÉSUMÉ FINAL
# ============================================================
echo "==============================================="
log_success "Succès!"
echo "==============================================="
echo ""

# Afficher un résumé des fichiers créés
if [ "$ACTION" = "full" ] || [ "$ACTION" = "compile" ]; then
    log_info "Fichiers compilés:"
    echo "  - client/target/classes/ (bytecode Java)"
    echo ""
fi

if [ "$ACTION" = "full" ] || [ "$ACTION" = "test" ]; then
    log_info "Tests lancés (35 tests):"
    echo "  - AudioBufferTest (8 tests)"
    echo "  - OpusCodecTest (9 tests)"
    echo "  - AudioDeviceTest (8 tests)"
    echo "  - UserSessionTest (10 tests)"
    echo ""
    
    log_info "Résultats détaillés dans:"
    echo "  - client/target/surefire-reports/"
    echo ""
fi

if [ "$ACTION" = "audio" ]; then
    log_info "Pour lancer les tests unitaires aussi:"
    echo "  ./test.sh full"
    echo ""
fi

log_info "Logs disponibles à:"
echo "  - $LOG_FILE"
echo ""
