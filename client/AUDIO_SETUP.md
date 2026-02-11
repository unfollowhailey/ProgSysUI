# 🔊 Configuration Audio - SecurePhone Client

## Sons MP3

Les fichiers sons sont stockés dans `resources/sounds/` et utilisent le format MP3.

### Fichiers sons disponibles

✅ Tous les fichiers sont présents :
- `message_received.mp3` - Notification message reçu
- `message_sent.mp3` - Confirmation envoi message
- `call_incoming.mp3` - Appel entrant
- `call_connected.mp3` - Appel connecté
- `call_ended.mp3` - Appel terminé
- `error.mp3` - Son d'erreur
- `button_click.mp3` - Clic de bouton
- `notification.mp3` - Notification générique

## Bibliothèque JLayer

Le support MP3 est assuré par **JLayer 1.0.1** (javazoom).

### Installation

La bibliothèque est déjà téléchargée dans `lib/jlayer-1.0.1.jar`.

Si besoin de la retélécharger :
```bash
./download_jlayer.sh
```

## Compilation

### Avec Maven
```bash
mvn clean compile
```

### Avec javac (mode manuel)
```bash
javac -cp "lib/jlayer-1.0.1.jar:target/classes" \
  -d target/classes \
  src/main/java/com/securephone/client/utils/SoundPlayer.java
```

## Test des sons

### Test simple
```bash
java -cp "lib/jlayer-1.0.1.jar:target/classes" \
  com.securephone.client.utils.SimpleSoundTest
```

### Utilisation dans le code

```java
// Récupérer l'instance
SoundPlayer player = SoundPlayer.getInstance();

// Jouer un son
player.playMessageReceived();
player.playCallIncoming();
player.playError();

// Contrôler le volume (0.0 à 1.0)
player.setMasterVolume(0.5f);

// Activer/désactiver
player.setSoundEnabled(false);
```

## Architecture

### Classe SoundPlayer

- **Pattern** : Singleton
- **Thread-safe** : Oui (lecture dans threads séparés)
- **Cache** : Vérifie l'existence au démarrage
- **Format supporté** : MP3 (via JLayer)

### Méthodes principales

| Méthode | Description |
|---------|-------------|
| `getInstance()` | Obtenir l'instance singleton |
| `playSound(SoundType)` | Jouer un son spécifique |
| `setSoundEnabled(boolean)` | Activer/désactiver les sons |
| `setMasterVolume(float)` | Définir le volume (0.0-1.0) |
| `stopAllSounds()` | Arrêter tous les sons |

### Méthodes de commodité

```java
playMessageReceived()
playMessageSent()
playCallIncoming()
playCallConnected()
playCallEnded()
playError()
playButtonClick()
playNotification()
```

## Résolution des problèmes

### Les sons ne jouent pas

1. **Vérifier que JLayer est présent**
   ```bash
   ls -lh lib/jlayer-1.0.1.jar
   ```

2. **Vérifier que les fichiers MP3 existent**
   ```bash
   ls resources/sounds/*.mp3
   ```

3. **Vérifier les logs au démarrage**
   ```
   Vérification des fichiers sons...
     ✓ MESSAGE_RECEIVED trouvé
     ✓ MESSAGE_SENT trouvé
     ...
   ```

4. **Vérifier que les sons sont activés**
   ```java
   if (!player.isSoundEnabled()) {
       player.setSoundEnabled(true);
   }
   ```

### Erreur "Format non supporté"

- SoundPlayer utilise maintenant **MP3 uniquement** (via JLayer)
- Les fichiers WAV ne sont plus supportés
- Convertir les WAV en MP3 si nécessaire

### Pas de sortie audio

- Vérifier les paramètres système audio
- Vérifier que le volume système n'est pas à 0
- Tester avec un autre lecteur MP3

## Notes techniques

- **JLayer** : Bibliothèque pure Java pour décoder MP3
- **Threading** : Chaque son joue dans un thread séparé
- **Gestion mémoire** : Les sons ne sont pas préchargés en RAM (streaming)
- **Performance** : Décodage MP3 léger, impact minimal

## Intégration avec NotificationManager

Les notifications utilisent automatiquement SoundPlayer :

```java
NotificationManager notifications = NotificationManager.getInstance();
notifications.showMessageNotification("Alice", "Salut!");
// → Joue automatiquement le son MESSAGE_RECEIVED
```

---

**✅ Statut** : Fonctionnel et testé  
**📅 Dernière mise à jour** : 27 janvier 2026
