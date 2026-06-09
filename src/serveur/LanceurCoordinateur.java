package serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import service.ServiceCoordinateur;

public class LanceurCoordinateur {
    public static void main(String[] arguments) {
        int port = 1099;
        
        try {
            // Crée l'annuaire RMI sur le port 1099 (port par défaut de RMI)
            Registry registre = LocateRegistry.createRegistry(port);
            
            // Instancie le coordinateur
            CoordinateurImpl coordinateur = new CoordinateurImpl();
            
            // Enregistre le coordinateur dans l'annuaire sous un nom connu
            // Les calculateurs et le client utiliseront ce nom pour le retrouver
            registre.rebind(ServiceCoordinateur.NOM_REGISTRE, coordinateur);

            System.out.println("Le Coordinateur est prêt !");
            System.out.println("En attente d'ouvriers...");
            
        } catch (Exception erreur) {
            System.err.println("Impossible de lancer le coordinateur : " + erreur.getMessage());
        }
    }
}