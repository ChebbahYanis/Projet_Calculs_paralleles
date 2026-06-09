package client;

import raytracer.Image;
import raytracer.Scene;

public class TestLocal {
    public static void main(String[] arguments) {
        // parametres de base
        String fichierScene = "simple.txt";
        int largeur = 800;
        int hauteur = 600;

        // chargement de la scene
        System.out.println("Préparation de la scène : " + fichierScene + " (" + largeur + "x" + hauteur + ")");
        Scene scene = new Scene(fichierScene, largeur, hauteur);
        
        // calcul pur (1 seul processeur)
        System.out.println("Calcul sur 1 machine en cours... Veuillez patienter.");
        long debut = System.currentTimeMillis();
        // le moteur de rendu calcule toute l'image d'un seul coup
        Image image = scene.compute(0, 0, largeur, hauteur);
        long fin = System.currentTimeMillis();

        System.out.println("Calcul LOCAL (1 seule machine) terminé en " + (fin - debut) + " ms");

        // sauvegarde de la reference visuelle
        String nomImage = "rendu_local";
        image.save(nomImage, "png");
        System.out.println("Fichier de référence sauvegardé : " + nomImage + ".png");
    }
}
