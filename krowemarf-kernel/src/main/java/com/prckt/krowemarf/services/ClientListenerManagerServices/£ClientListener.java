package com.prckt.krowemarf.services.ClientListenerManagerServices;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;

/**
 * Allows client to have callback by overriding callback methods
 */
public abstract class £ClientListener extends UnicastRemoteObject implements _ClientListener{


    /**
     * Initialize the clientListener
      * @throws RemoteException
     */
    protected £ClientListener() throws RemoteException {
        super();
    }


    /**
     * This method is the callback of the private messager
     * Client must redefine is behavior
     *
     * @param componentName the composant name that will be auto-created by the server.
     *                       This component name allows the client to find the component in the component Managers
     * @throws RemoteException
     * @throws SQLException
     */
    @Override
    public abstract void onNewPrivateMessenger(String componentName) throws RemoteException, SQLException;

}
