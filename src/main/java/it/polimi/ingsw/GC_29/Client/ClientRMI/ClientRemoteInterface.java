package it.polimi.ingsw.GC_29.Client.ClientRMI;


import it.polimi.ingsw.GC_29.Client.Distribution;
import it.polimi.ingsw.GC_29.Model.PlayerColor;
import it.polimi.ingsw.GC_29.Server.RMI.RMIViewRemote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Christian on 11/06/2017.
 */
public interface ClientRemoteInterface extends Remote {

    void runNewGame(RMIViewRemote serverViewStub) throws RemoteException;

    void initialize() throws RemoteException;

    void setPlayerColor(PlayerColor playerColor) throws RemoteException;

    PlayerColor getPlayerColor() throws RemoteException;

    String getUserName() throws RemoteException;

    RMIViewRemote getServerViewStub() throws RemoteException;

    Distribution getDistribution() throws RemoteException;

    void joinGame() throws RemoteException;
}
