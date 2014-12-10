/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteTable;
import vss.rmi.diningphilos.server.n.remote.objects.Table;

/**
 *
 * @author T500
 */
public class MainClient implements Remote {

    public static Registry registry;

    public static void main(String[] args) {

        String serverIp = "localhost";

        try {
            Table table = new Table();

            RemoteTable stubTable = (RemoteTable) UnicastRemoteObject.exportObject(table, 0);
            registry = LocateRegistry.getRegistry(serverIp, 1099);

            int id = registry.list().length;
            registry.bind("table" + id, stubTable);

            System.out.println("table " + id + " ready.");

        } catch (RemoteException | AlreadyBoundException ex) {
            Logger.getLogger(MainClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
