package com.prckt.krowemarf.services.ComponentManagerSevices;

import com.prckt.krowemarf.components.Messenger.Messenger;
import com.prckt.krowemarf.components.Messenger._Messenger;
import com.prckt.krowemarf.components._Component;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ComponentManager extends UnicastRemoteObject implements _ComponentManager {
    private HashMap<String, _Component> components;
    private int mpNumber = 1;

    public ComponentManager() throws RemoteException {
        super();
        this.components = new HashMap<>();
    }

    public void addComponent(_Component c) throws RemoteException {
        Logger.getGlobal().log(Level.INFO,"Ajout du composant " + c.getName() + " au component Manager");
        this.components.put(c.getName(), c);
    }

    @Override
    public _Component getComponantByName(String name) throws RemoteException {
        return this.components.get(name);
    }

    @Override
    public String addPrivateMessenger() throws RemoteException {
        Logger.getGlobal().log(Level.INFO,"Création d'une messagerie privée de nom : chat" + this.mpNumber);

        _Messenger messenger = new Messenger("chat" + this.mpNumber);
        this.mpNumber++;
        this.components.put(messenger.getName(), messenger);
        return messenger.getName();
    }

    public void removeComponent(String name) {
        Logger.getGlobal().log(Level.INFO,"Retrait du composant : " + name + " du component Manager" );
        this.components.remove(name);
    }

    public Collection<_Component> getComponents() {
        return components.values();
    }
}


