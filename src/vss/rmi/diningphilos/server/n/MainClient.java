/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import vss.rmi.diningphilos.server.n.remote.objects.Tablepart;

/**
 *
 * @author T500
 */
public class MainClient implements Remote {

    public static Registry registry;

    public static void main(String[] args) throws RemoteException {

        String serverIp = "localhost";

            // RemoteTablepart stubTable = (RemoteTablepart) UnicastRemoteObject.exportObject(table, 0);
            registry = LocateRegistry.getRegistry(serverIp, 1099);
            Tablepart tablepart = new Tablepart();

    }
}
