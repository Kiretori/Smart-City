# Système de Monitoring IoT - Ville Intelligente

Ce projet implémente un système de surveillance en temps réel pour une ville intelligente, utilisant des capteurs IoT simulés pour collecter des données sur l'énergie, l'eau, les déchets et le trafic.

## 🏗️ Architecture

Le système utilise une architecture de données en temps réel avec :
- **Apache Kafka** pour le streaming de données
- **InfluxDB2** pour le stockage de données temporelles
- **Grafana** pour la visualisation et les tableaux de bord
- **Docker** pour la containerisation des services

## 📋 Prérequis

Assurez-vous d'avoir installé les éléments suivants :

- **Kafka** >= 4.0.0
- **InfluxDB2**
- **Grafana**
- **Docker** et **Docker Compose**
- **JDK** 21 ou supérieur
- **Maven** (pour la compilation du projet)

## 🚀 Installation et Configuration

### 1. Configuration des Secrets

Créez un répertoire `secrets` à la racine du projet et ajoutez les fichiers suivants :

```
secrets/
├── influxdb2-admin-username    # Nom d'utilisateur administrateur InfluxDB
├── influxdb2-admin-password    # Mot de passe administrateur InfluxDB
└── influxdb2-admin-token       # Token d'administration InfluxDB
```

**Important :** Modifiez le code dans `InfluxDBService.java` selon vos identifiants.

### 2. Démarrage des Services Docker

Lancez les services avec Docker Compose :

```bash
docker compose up -d
```

### 3. Configuration de Kafka

Démarrez Kafka sur votre terminal et créez les topics suivants :

```bash
# Créer les topics Kafka
kafka-topics --create --topic energy --bootstrap-server localhost:9092
kafka-topics --create --topic water --bootstrap-server localhost:9092
kafka-topics --create --topic waste --bootstrap-server localhost:9092
kafka-topics --create --topic traffic --bootstrap-server localhost:9092
```

### 4. Compilation du Projet

Compilez le projet Java avec Maven :

```bash
mvn clean compile
```

## 🎯 Utilisation

### Génération de Données de Capteurs

Pour envoyer des données simulées de capteurs vers InfluxDB :

1. **Mode Serveur** - Dans un premier terminal :
   ```bash
   mvn exec:java -Dexec.args="server"
   ```

2. **Mode Client** - Dans un second terminal :
   ```bash
   mvn exec:java -Dexec.args="client"
   ```

Les données commenceront à peupler InfluxDB automatiquement.

> **Note :** Chaque exécution du programme en mode serveur génère des données pour un nouvel ID de capteur unique. Les horodatages des données commencent au 1er janvier 2025.

## 📊 Accès aux Interfaces

### InfluxDB
- **URL :** http://localhost:8086
- Interface d'administration pour consulter les données stockées

### Grafana
- **URL :** http://localhost:3000
- Interface de visualisation et création de tableaux de bord

## 🔧 Configuration de Grafana

Pour connecter Grafana à InfluxDB :

1. Accédez à Grafana (http://localhost:3000)
2. Allez dans **Configuration** → **Data Sources**
3. Cliquez sur **Add data source**
4. Sélectionnez **InfluxDB**
5. Configurez la source de données selon vos paramètres

### Documentation Officielle
Pour plus d'aide sur la configuration : [Documentation Grafana InfluxDB](https://grafana.com/docs/grafana/latest/datasources/influxdb/)

## 📈 Types de Données Collectées

Le système surveille quatre catégories principales :

- 🔋 **Énergie** : Consommation électrique
- 💧 **Eau** : Utilisation des ressources hydriques  
- 🗑️ **Déchets** : Gestion des déchets urbains
- 🚦 **Trafic** : Flux de circulation

## 🛠️ Dépannage

Si vous rencontrez des problèmes :

1. Vérifiez que tous les services Docker sont en cours d'exécution
2. Assurez-vous que les topics Kafka sont créés
3. Vérifiez les identifiants dans les fichiers secrets
4. Consultez les logs des conteneurs Docker

---

**Développé pour le cours de [Nom du Cours] - [Année Scolaire]**