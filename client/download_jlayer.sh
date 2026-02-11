#!/bin/bash
# Script pour télécharger JLayer manuellement

echo "Téléchargement de JLayer 1.0.1..."

# Créer le dossier lib s'il n'existe pas
mkdir -p lib

# Télécharger JLayer
curl -L -o lib/jlayer-1.0.1.jar \
  "https://repo1.maven.org/maven2/javazoom/jlayer/1.0.1/jlayer-1.0.1.jar"

if [ $? -eq 0 ]; then
    echo "✓ JLayer téléchargé avec succès dans lib/jlayer-1.0.1.jar"
    ls -lh lib/jlayer-1.0.1.jar
else
    echo "✗ Erreur lors du téléchargement"
    exit 1
fi
