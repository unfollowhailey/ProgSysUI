## SecurePhone Server

Serveur Java pour chat TCP, audio/vidéo UDP et API REST.

### Lancer

Depuis la racine :

- `cd serveur`
- `mvn clean compile`
- `mvn exec:java -Dexec.mainClass="com.securephone.server.MainServer"`

### Configuration

Fichier principal : `serveur/src/main/java/com/securephone/resources/config.properties`

### Web UI

La page d’accueil est dans `serveur/src/main/webapp/index.html`.
