# Projet_Calculs_paralleles

## Équipe de Projet
* **[BELAIB Nael]**
* **[CHEBBAH Yanis]**
* **[TAMOURGH Jassem]**
* **[VALEUR--MASELLI Marius]**

---

## Présentation du Projet
Ce projet illustre la puissance de la **parallélisation des données** dans le cadre d'un traitement lourd en cycles CPU : la synthèse d'images par tracé de rayons (*Raytracing*).

L'algorithme de base étant particulièrement chronophage sur une machine unique, nous avons mis en place une architecture répartie utilisant **Java RMI** et le **multi-threading**. L'objectif est de découper l'image en dalles indépendantes et de distribuer leur calcul sur un réseau de machines travaillant simultanément, afin d'obtenir un gain de temps significatif.

---

## Architecture (Modèle Master-Worker)
Notre application repose sur trois composants distincts :

1. **Le Coordinateur (Serveur) :** Processus fixe qui écoute sur le registre RMI. Il agit comme un chef d'orchestre. Il réceptionne la demande globale, gère un *Pool de Threads*, distribue les dalles aux différents calculateurs de manière asynchrone, et assemble l'image finale.
2. **Les Calculateurs (Ouvriers) :** Processus mobiles qui s'inscrivent auprès du coordinateur. Ils reçoivent les coordonnées d'une dalle, exécutent le calcul géométrique sur leur CPU local, et renvoient les pixels calculés.
3. **Le Client (Programme Externe) :** Processus mobile qui charge la scène, demande le rendu au coordinateur et affiche le résultat final à l'écran.

---

## Arborescence du Projet

```text
├── src/
│   ├── calculateur/      # Contient l'implémentation des nœuds de calcul
│   ├── client/           # Contient les points d'entrée utilisateur (Local et RMI)
│   ├── raytracer/        # Le moteur de rendu d'origine (modifié pour la sérialisation)
│   ├── serveur/          # Contient l'implémentation du coordinateur central
│   └── service/          # Interfaces RMI partagées entre les composants
├── simple.txt            # Fichier de description de la scène par défaut
├── rapport_projet.pdf    # Rapport contenant l'analyse, les schémas et diagrammes
└── demo_raytracer.mp4    # Capture vidéo de l'exécution répartie

---

## Instructions d'exécution

### 1. Compilation
Placez-vous à la racine du projet et compilez l'ensemble des fichiers Java :
javac -sourcepath src -d out (Get-ChildItem -Recurse -Filter "*.java" src).FullName (sous powershell)
javac -sourcepath src -d out $(find src -name "*.java") (sous bash)

## 2. Démarrage du Coordinateur (Serveur)
Ouvrez un terminal, placez-vous dans le dossier compilé et lancez le registre RMI ainsi que le coordinateur :
java -cp out serveur.LanceurCoordinateur

## 3. Démarrage des Calculateurs (Ouvriers)
Ouvrez plusieurs nouveaux terminaux (sur la même machine ou sur des machines différentes du réseau) et lancez autant de calculateurs que désiré : 
java -cp out calculateur.LanceurCalculateur

## 4. Lancement du Client
Enfin, pour lancer le calcul réparti, ouvrez un dernier terminal et exécutez le client :
java -cp out client.ClientRendu

## Test de référence 
Pour comparer les performances et prouver l'accélération, vous pouvez lancer la version locale sur un seul processeur :
java client.TestLocal