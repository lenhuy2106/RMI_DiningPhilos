/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import vss.rmi.diningphilos.server.n.remote.objects.Master;

/**
 *
 * @author T500
 */
public class MainServer {

    /**
     * Die Main Methode liest eine Anzahl an Philosophen und Sitzen und
     * erstellt entsprechend  einen Tisch und die Philosophen. Sie lässt
     * die Threads für eine bestimmte Zeit laufen und unterbricht danach
     * alle. Sie kann mit einem Raum verglichen werden, in der die Objekte
     * existieren.
     *
     * @param args
     */
    public static void main(final String[]args) {

        final Scanner in = new Scanner(System.in);

        // TODO: create dialogue add/remove objects
        // read console input
        System.out.println("Number Philosophers:");
        final int nPhilosophers = in.nextInt();
        System.out.println("Index of very hungry Philosophers (seperated by space):");
        String hungryInput = "";

        while (in.hasNext()) {
            hungryInput = in.nextLine();
            if (!hungryInput.isEmpty()) break;
        }
        final String[] hungry = hungryInput.split(" ");
        System.out.println("Number Seats:");
        final int nSeats = in.nextInt();

        try {

            // initiate registry
            LocateRegistry.createRegistry(1099);
            Registry registry = LocateRegistry.getRegistry();

            // start master
            Master master = new Master(nPhilosophers);
            master.initiate(registry);
            master.start();
            
        } catch (RemoteException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
