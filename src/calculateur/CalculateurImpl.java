package calculateur;

import service.ServiceCalculateur;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import raytracer.Image;
import raytracer.Scene;

public class CalculateurImpl extends UnicastRemoteObject implements ServiceCalculateur {
    private final String nom;

    public CalculateurImpl(String nom) throws RemoteException {
        super();
        this.nom = nom;
    }

    @Override
    public String getNom() {
        return nom;
    }

    @Override
    public Image calculerTranche(Scene scene, int x, int y, int largeur, int hauteur) throws RemoteException {
        System.out.println("[" + nom + "] Je calcule de la ligne " + y + " à " + (y + hauteur - 1));
        long debut = System.currentTimeMillis();

        Image tranche = scene.compute(x, y, largeur, hauteur);

        long fin = System.currentTimeMillis();
        System.out.println("[" + nom + "] Tranche terminée en " + (fin - debut) + " ms");

        return tranche;
    }
}