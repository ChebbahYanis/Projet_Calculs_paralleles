import java.time.Instant;
import java.time.Duration;

import raytracer.Disp;
import raytracer.Scene;
import raytracer.Image;

public class LancerRaytracer {

    public static String aide = "Raytracer : synthèse d'image par lancé de rayons (https://en.wikipedia.org/wiki/Ray_tracing_(graphics))\n\nUsage : java LancerRaytracer [fichier-scène] [largeur] [hauteur]\n\tfichier-scène : la description de la scène (par défaut simple.txt)\n\tlargeur : largeur de l'image calculée (par défaut 512)\n\thauteur : hauteur de l'image calculée (par défaut 512)\n";
     
    public static void main(String args[]){

        // Le fichier de description de la scène si pas fournie
        String fichier_description="simple.txt";

        // largeur et hauteur par défaut de l'image à reconstruire
        int largeur = 512, hauteur = 512;
        
        if(args.length > 0){
            fichier_description = args[0];
            if(args.length > 1){
                largeur = Integer.parseInt(args[1]);
                if(args.length > 2)
                    hauteur = Integer.parseInt(args[2]);
            }
        }else{
            System.out.println(aide);
        }
        
   
        // création d'une fenêtre 
        Disp disp = new Disp("Raytracer", largeur, hauteur);
        
        // Initialisation d'une scène depuis le modèle 
        Scene scene = new Scene(fichier_description, largeur, hauteur);
        

        
        // calcul de la taille d'un carré (la moitié de l'image)
        int tileL = largeur / 2;
        int tileH = hauteur / 2;
                
        // Chronométrage du temps de calcul total
        Instant debut = Instant.now();
        System.out.println("Calcul de l'image par dalles...");

        // rendu et affichage du carré haut-gauche (coordonnées 0, 0)
        Image tileTL = scene.compute(0, 0, tileL, tileH);
        disp.setImage(tileTL, 0, 0);

        // rendu et affichage du carré bas-droite (coordonnées tileL, tileH)
        Image tileBR = scene.compute(tileL, tileH, tileL, tileH);
        disp.setImage(tileBR, tileL, tileH);

        Instant fin = Instant.now();

        long duree = Duration.between(debut, fin).toMillis();
        
        System.out.println("Image calculée en :"+duree+" ms");
    
    }	
}
