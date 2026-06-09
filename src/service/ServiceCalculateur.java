package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import raytracer.Image;
import raytracer.Scene;

public interface ServiceCalculateur extends Remote {
    Image calculerTranche(Scene scene, int x, int y, int largeur, int hauteur) throws RemoteException;
    String getNom() throws RemoteException;
}
