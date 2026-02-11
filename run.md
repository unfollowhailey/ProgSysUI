# ✅ Run SecurePhone (serveur + client)

Ce guide donne les commandes **fiables** pour lancer le serveur et l’application cliente.

---

## ✅ Pré-requis

- **Java 11+**
- **Maven 3.8+**
- (Optionnel) **MySQL** si tu veux activer la persistance

Vérifier :
- `java --version`
- `mvn -v`

---

## 🖥️ Lancer le serveur (TCP + UDP)

Depuis la racine du projet :

1) Compiler le serveur :
- `cd serveur`
- `mvn clean compile`

2) Démarrer le serveur :
- `mvn exec:java -Dexec.mainClass="com.securephone.server.MainServer"`

Le serveur ouvre :
- Chat TCP : **8081**
- Audio UDP : **50000**
- Vidéo UDP : **50020**

> Le serveur peut tourner sans base de données (mode dégradé). La BDD est optionnelle.

---

## 🧩 Lancer le client (UI)

Dans un second terminal :

1) Compiler le client :
- `cd client`
- `mvn clean compile`

2) Démarrer l’UI :
- `mvn exec:java -Dexec.mainClass="com.securephone.client.SecurePhoneApp"`

---

## 🚀 Lancer les deux en une seule commande

Depuis la racine :

- `./run_app.sh`

Ce script compile puis lance le serveur et l’application.

---

## 🧪 Tests côté client

- `./test.sh compile`
- `./test.sh test`
- `./test.sh audio`
- `./test.sh full`

---

## ⚙️ Configuration

Les paramètres sont dans :
- client : `client/resources/config.properties`
- serveur : `serveur/src/main/java/com/securephone/resources/config.properties`

---

## ℹ️ API REST (optionnel)

Les servlets REST existent, mais **l’application cliente actuelle utilise le TCP/UDP**.
Pour exposer les endpoints REST, déployer le module serveur dans un conteneur (Tomcat/Jetty).
