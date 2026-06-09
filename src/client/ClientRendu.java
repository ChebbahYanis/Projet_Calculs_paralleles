package client;

import service.ServiceCoordinateur;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import raytracer.Disp;
import raytracer.Image;
import raytracer.Scene;

public class ClientRendu {
    public static void main(String[] arguments) {
        // parametres de base
        String fichierScene = "simple.txt";
        int largeur = 800;
        int hauteur = 600;
        String nomImage = "rendu_final";

        try {
            // chargement de la scene
            System.out.println("Préparation de la scène : " + fichierScene + " (" + largeur + "x" + hauteur + ")");
            Scene scene = new Scene(fichierScene, largeur, hauteur);

            // connexion rmi
            System.out.println("Connexion au coordinateur...");
            Registry registre = LocateRegistry.getRegistry("localhost", 1099);
            ServiceCoordinateur coordinateur = (ServiceCoordinateur) registre.lookup(ServiceCoordinateur.NOM_REGISTRE);

            // verif des effectifs
            int nbCalculateurs = coordinateur.getNombreCalculateurs();
            System.out.println(nbCalculateurs + " calculateur(s) disponible(s)");

            // delegation du calcul
            long debut = System.currentTimeMillis();
            System.out.println("Lancement du calcul...");
            // l'appel rmi bloque le client jusqu'a ce que le coordinateur renvoie l'image finale
            Image imageFinale = coordinateur.lancerLeRendu(scene, largeur, hauteur);
            long fin = System.currentTimeMillis();

            imageFinale.save(nomImage, "png");
            System.out.println("Image reçue en " + (fin - debut) + " ms");
            System.out.println("Image sauvegardée sous le nom : " + nomImage + ".png");

            // on affiche dans une fenetre 
            Disp fenetre = new Disp("Raytracer RMI", largeur, hauteur);
            fenetre.setImage(imageFinale, 0, 0);

        } catch (Exception erreur) {
            System.err.println("Erreur côté client : " + erreur.getMessage());
            erreur.printStackTrace();
        }
    }
}
