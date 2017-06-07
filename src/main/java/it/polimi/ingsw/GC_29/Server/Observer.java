package it.polimi.ingsw.GC_29.Server;

import java.rmi.RemoteException;

/**
 * Created by Christian on 07/06/2017.
 */
public interface Observer<C> {

    public default void update(C o) throws RemoteException {
        System.out.println("I am the" + this.getClass().getSimpleName() +
                " I have been notified with the "+o.getClass().getSimpleName());
    }

    public void update();
}
