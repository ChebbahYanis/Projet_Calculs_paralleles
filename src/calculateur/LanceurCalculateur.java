package calculateur;

import service.ServiceCalculateur;
import service.ServiceCoordinateur;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class LanceurCalculateur {
    public static void main(String[] arguments) {
        // adresse IP du coordinateur et numéro de port ( par défaut localhost et 1099)
        String adresseCoordinateur = arguments.length >= 1 ? arguments[0] : "localhost";
        int port = arguments.length >= 2 ? Integer.parseInt(arguments[1]) : 1099;
        // Nom random pour chaque calculateur pour pouvoir les identifier
        String nomOuvrier = "Ouvrier-" + UUID.randomUUID().toString().substring(0, 4);

        try {
            // On créer le registre et on se connecte au coordinateur
            Registry registre = LocateRegistry.getRegistry(adresseCoordinateur, port);
            // On récupère l'objet distant
            ServiceCoordinateur coordinateur = (ServiceCoordinateur) registre.lookup(ServiceCoordinateur.NOM_REGISTRE);
            // On crée l'objet calculateur
            CalculateurImpl calculateur = new CalculateurImpl(nomOuvrier);
            // On enregistre l'objet dans le registre du coordinateur
            int total = coordinateur.enregistrerCalculateur(nomOuvrier, calculateur);

            System.out.println("Ouvrier prêt : " + nomOuvrier);
            System.out.println("Ouvriers connectés : " + total);
            System.out.println("Pour arrêter : Ctrl+C");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    coordinateur.retirerCalculateur(nomOuvrier);
                    UnicastRemoteObject.unexportObject(calculateur, true);
                } catch (Exception ignore) {}
            }));

            new CountDownLatch(1).await(); // Attendre indéfiniment
        } catch (Exception erreur) {
            System.err.println("Erreur de l'ouvrier : " + erreur.getMessage());
        }
    }
}
