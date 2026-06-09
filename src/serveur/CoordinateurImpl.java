package serveur;

import java.awt.Color;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import raytracer.Image;
import raytracer.Scene;
import service.ServiceCalculateur;
import service.ServiceCoordinateur;

// Serveur central : reçoit les demandes du client, distribue le travail aux calculateurs
// et renvoie l'image assemblée
public class CoordinateurImpl extends UnicastRemoteObject implements ServiceCoordinateur {

    // ConcurrentHashMap pour éviter les conflits si plusieurs calculateurs s'enregistrent en même temps
    private final Map<String, ServiceCalculateur> listeCalculateurs = new ConcurrentHashMap<>();

    public CoordinateurImpl() throws RemoteException {
        super();
    }

    // Appelé par chaque calculateur au démarrage pour rejoindre la liste
    @Override
    public int enregistrerCalculateur(String nom, ServiceCalculateur calculateur) throws RemoteException {
        listeCalculateurs.put(nom, calculateur);
        System.out.println("Nouvel ouvrier connecté : " + nom + " (Total : " + listeCalculateurs.size() + ")");
        return listeCalculateurs.size();
    }

    // Appelé par un calculateur quand il s'arrête pour quitter la liste
    @Override
    public void retirerCalculateur(String nom) {
        if (listeCalculateurs.remove(nom) != null) {
            System.out.println("Ouvrier déconnecté : " + nom + " (Total : " + listeCalculateurs.size() + ")");
        }
    }

    @Override
    public int getNombreCalculateurs() {
        return listeCalculateurs.size();
    }

    // Point d'entrée principal : le client appelle cette méthode avec la scène et les dimensions
    // Le coordinateur découpe, distribue, attend, assemble, puis retourne l'image finale
    @Override
    public Image lancerLeRendu(Scene scene, int largeur, int hauteur) throws RemoteException {
        List<ServiceCalculateur> ouvriersDispo = new ArrayList<>(listeCalculateurs.values());
        List<String> noms = new ArrayList<>(listeCalculateurs.keySet());


        if (ouvriersDispo.isEmpty()) {
            throw new RemoteException("Erreur : Aucun ouvrier connecté pour faire le calcul !");
        }

        // On ne mobilise pas plus d'ouvriers qu'il n'y a de lignes dans l'image
        int nombreOuvriers = Math.min(ouvriersDispo.size(), hauteur);
        System.out.println("Début du calcul avec " + nombreOuvriers + " ouvrier(s)...");

        Image imageFinale = new Image(largeur, hauteur);
        List<ThreadCalcul> threads = new ArrayList<>();
        long tempsDebut = System.currentTimeMillis();

        // Découpage de l'image en tranches horizontales et envoi simultané à chaque ouvrier
        // Tous les thread.start() se font à la suite, les calculs partent en parallèle
        for (int i = 0; i < nombreOuvriers; i++) {
            String nom = noms.get(i);
            ServiceCalculateur ouvrier = ouvriersDispo.get(i);
            int ligneDebut = i * hauteur / nombreOuvriers;
            int ligneFin = (i + 1) * hauteur / nombreOuvriers;
            int hauteurTranche = ligneFin - ligneDebut;

            ThreadCalcul thread = new ThreadCalcul(ouvrier, scene, nom,  ligneDebut, largeur, hauteurTranche);
            threads.add(thread);
            thread.start();
        }

        // join() bloque jusqu'à ce que chaque thread ait reçu sa réponse
        // puis on colle chaque tranche à sa position verticale dans l'image finale
        for (ThreadCalcul thread : threads) {
            try {
                thread.join();
                if (thread.erreur != null) {
                    System.out.println("Ouvrier " + thread.nom + " a planté, redistribution de sa tranche...");
                    
                    // On cherche un ouvrier encore présent (différent de celui qui a planté)
                    ServiceCalculateur remplacant = null;
                    for (Map.Entry<String, ServiceCalculateur> entree : listeCalculateurs.entrySet()) {
                        if (!entree.getKey().equals(thread.nom)) {
                            remplacant = entree.getValue();
                            break;
                        }
                    }
                    
                    if (remplacant == null) {
                        throw new RemoteException("Plus aucun ouvrier disponible !");
                    }
                    
                    // On relance le calcul de la tranche sur le remplaçant
                    Image trancheRecalculee = remplacant.calculerTranche(
                        thread.scene, 0, thread.ligneDebut, thread.largeur, thread.hauteurTranche
                    );
                    copierPixels(imageFinale, trancheRecalculee, 0, thread.ligneDebut);
                    
                } else {
                    copierPixels(imageFinale, thread.imageResultat, 0, thread.ligneDebut);
                }
            } catch (InterruptedException e) {
                throw new RemoteException("Calcul interrompu.");
            }
        }

        System.out.println("Image assemblée en " + (System.currentTimeMillis() - tempsDebut) + " ms !");
        return imageFinale;
    }

    // Recopie pixel par pixel une tranche dans l'image finale à la bonne position
    private void copierPixels(Image imageFinale, Image tranche, int xDestination, int yDestination) {
        for (int y = 0; y < tranche.getHeight(); y++) {
            for (int x = 0; x < tranche.getWidth(); x++) {
                Color couleur = tranche.getPixel(x, y);
                if (couleur != null) imageFinale.setPixel(xDestination + x, yDestination + y, couleur);
            }
        }
    }

    // Classe interne représentant un thread de calcul
    // Chaque instance gère un appel RMI vers un calculateur distant
    private class ThreadCalcul extends Thread {
        ServiceCalculateur ouvrier;
        Scene scene;
        String nom;
        int ligneDebut, largeur, hauteurTranche;
        Image imageResultat; // Résultat renvoyé par le calculateur
        Exception erreur;    // Null si le calcul s'est bien passé

        public ThreadCalcul(ServiceCalculateur ouvrier, Scene scene, String nom, int ligneDebut, int largeur, int hauteurTranche) {
            this.ouvrier = ouvrier;
            this.scene = scene;
            this.nom = nom;
            this.ligneDebut = ligneDebut;
            this.largeur = largeur;
            this.hauteurTranche = hauteurTranche;
        }

        // Appel RMI bloquant vers le calculateur distant
        // Le résultat est stocké dans imageResultat pour être récupéré après le join()
        @Override
        public void run() {
            try {
                imageResultat = ouvrier.calculerTranche(scene, 0, ligneDebut, largeur, hauteurTranche);
            } catch (Exception e) {
                erreur = e;
                // On retire l'ouvrier mort de la liste
                retirerCalculateur(nom);
            }
        }
    }
}