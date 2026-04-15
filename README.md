# 🏥 Hospitalisation Microservice

## 📌 Description

Ce microservice fait partie d'une architecture microservices pour la gestion hospitalière (PediNephro).

Il permet de gérer :
- les patients
- les hospitalisations
- les épisodes de soin

Il inclut également des fonctionnalités avancées d’analyse et de prédiction.

---

## ⚙️ Technologies utilisées

- Java 17
- Spring Boot
- Spring Data JPA
- MySQL
- Maven
- JUnit 5
- Mockito
- Eureka Discovery Server
- API Gateway

---

## 🏗️ Architecture

Ce microservice fonctionne dans une architecture distribuée :

- Eureka → Service Discovery  
- API Gateway → Routage des requêtes  
- Microservices indépendants  

---

## 🚀 Fonctionnalités principales

### 🔹 Gestion des patients
- CRUD complet

### 🔹 Gestion des hospitalisations
- Création / mise à jour / suppression
- Gestion des lits
- Vérification capacité service
- Statut (EN_COURS / TERMINEE)

### 🔹 Gestion des épisodes de soin
- CRUD complet
- Validation des dates
- Signature médicale

---

## 🧠 Fonctionnalités avancées

### 📊 File d’attente intelligente
- Priorisation automatique
- Temps d’attente estimé

### 📈 Timeline patient
- Historique chronologique des soins

### 📉 Analyse historique
- Détection de récidives
- Calcul de durée moyenne

### 🔮 Prédictions
- Occupation des services
- Durée de séjour
- Tableau de bord prédictif

### 🧬 Profil médical 360°
- Score de complexité
- Analyse complète patient

---

## 🧪 Tests

Des tests unitaires ont été réalisés avec :

- JUnit 5
- Mockito

Tests réalisés sur :
- EpisodeDeSoinService
- HospitalisationService
- PatientService

---

## ▶️ Lancer le projet

```bash
mvn clean install
mvn spring-boot:run
