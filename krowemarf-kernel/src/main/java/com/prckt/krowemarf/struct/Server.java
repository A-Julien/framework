package com.prckt.krowemarf.struct;

import com.prckt.krowemarf.components._Component;
import com.prckt.krowemarf.services.ClientListenerManagerServices.ClientListenerManager;
import com.prckt.krowemarf.services.ComponentManagerSevices.ComponentManager;
import com.prckt.krowemarf.services.ConfigManagerServices.ConfigManager;
import com.prckt.krowemarf.services.DbConnectionServices.DbConnectionManager;
import com.prckt.krowemarf.services.UserManagerServices.UserManager;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class Server extends £Server implements _Runnable {
    private int port;
    private String adresse;
    private ComponentManager componentManager;
    private UserManager userManager;
    private ClientListenerManager clientListenerManager;
    private Registry registry;
    private Connection dbConnection;


    public Server(int port, String adresse) throws RemoteException, IOException {
        this.port = port;
        this.adresse = adresse;
        LocateRegistry.createRegistry(this.port);
        this.registry = LocateRegistry.getRegistry(this.port);
        this.componentManager = new ComponentManager();
        this.userManager = new UserManager();
        this.clientListenerManager = new ClientListenerManager();
        this.dbConnection = new DbConnectionManager().connect("Server");
        Logger log = Logger.getGlobal();
        FileHandler fh = new FileHandler("myLog.txt", true);
        log.addHandler(fh);
    }

    /**
     * Add component to the component manager
     * This methode must be call after the running method
     * @param component to add
     * @throws RemoteException
     */
    public void bindComponent(_Component component) throws RemoteException {
        Logger.getGlobal().log(Level.INFO,"Ajout du component au registre RMI : " + component.getName());
        this.componentManager.addComponent(component);
    }

    private void unBindComponent(_Component component) throws RemoteException {
        this.componentManager.removeComponent(componentManagerName);
        Logger.getGlobal().log(Level.INFO,"Retrait du component du registre RMI : " + component.getName());
    }

    /**
     * This method allows to run the server. Need to call them first.
     * Open the connection with database and check if table exist or not.
     * Initialize the rmi server by binding all the components
     * @return 0 if the server start correctly
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Override
    public int run() throws IOException, ClassNotFoundException {
        try {
            System.setProperty("java.security.policy", ConfigManager.getConfig("bdProp"));
            if (System.getSecurityManager() == null)
            {
                System.setSecurityManager ( new RMISecurityManager() );
                Logger.getGlobal().log(Level.INFO,"Instanciation du security manager");
            }

            if(!DbConnectionManager.tableExist(this.dbConnection,_Component.messengerTableName)) this.dbConnection.createStatement().executeUpdate(sqlTable(_Component.messengerTableName));
            if(!DbConnectionManager.tableExist(this.dbConnection,_Component.postTableName)) this.dbConnection.createStatement().executeUpdate(sqlTable(_Component.postTableName));
            if(!DbConnectionManager.tableExist(this.dbConnection,_Component.documentLibraryTableName))this.dbConnection.createStatement().executeUpdate(sqlTable(_Component.documentLibraryTableName));
            this.dbConnection.close();
        }catch (SQLException e) {
            Logger.getGlobal().log(Level.INFO,"Connexion à la BD échouée");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            this.registry.rebind(this.buildRmiAddr(componentManagerName, this.adresse), this.componentManager);
            this.registry.rebind(this.buildRmiAddr(userManagerName,this.adresse), this.userManager);
            this.registry.rebind(this.buildRmiAddr(clientListenerManagerName,this.adresse), this.clientListenerManager);
            Logger.getGlobal().log(Level.INFO,"server run on port : " + this.port + " at " + this.buildRmiAddr(componentManagerName, this.adresse));
        } catch (RemoteException e) {
            Logger.getGlobal().log(Level.INFO,"Echec du démarrage du serveur");
            System.exit(1);
        }
        return 0;

    }

    /**
     * Method call when the server shutdown.
     * call all stop() methods of each component in the component manager to close them.
     * Unbind all managers from the rmi registry to stop the server.
     * @throws RemoteException
     * @throws NotBoundException
     * @throws SQLException
     */
    @Override
    public void stop() throws RemoteException, NotBoundException, SQLException {
        for (_Component component:
            this.componentManager.getComponents()) {
            component.stop();
        }
        this.registry.unbind(this.buildRmiAddr(componentManagerName, this.adresse));
        this.registry.unbind(this.buildRmiAddr(userManagerName, this.adresse));
        this.registry.unbind(this.buildRmiAddr(clientListenerManagerName, this.adresse));

        Logger.getGlobal().log(Level.INFO,"Arrêt du serveur");

    }
}
