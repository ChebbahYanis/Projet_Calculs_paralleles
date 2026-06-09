package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import raytracer.Image;
import raytracer.Scene;

public interface ServiceCoordinateur extends Remote {
    String NOM_REGISTRE = "CoordinateurRaytracer";

    int enregistrerCalculateur(String nom, ServiceCalculateur calculateur) throws RemoteException;
    void retirerCalculateur(String nom) throws RemoteException;
    int getNombreCalculateurs() throws RemoteException;
    
    Image lancerLeRendu(Scene scene, int largeur, int hauteur) throws RemoteException;
}
