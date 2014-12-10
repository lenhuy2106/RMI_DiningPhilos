/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.client;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteMaster;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemotePhilosopher;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteTable;
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
/*        System.out.println("Number Philosophers:");
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

        // dynamic number of clients/tableparts?
        System.out.println("Number TableParts:");
        final int nTableParts = in.nextInt();
*/

        int nPhilosophers = 2;
        String[] hungry = {"0"};
        int nSeats = 2;
        int nTableParts = 1;


        try {

            // initiate registry
            LocateRegistry.createRegistry(1099);
            Registry registry = LocateRegistry.getRegistry();

            // initiate and bind master
            Master master = new Master(nPhilosophers);
            RemoteMaster stubMaster = (RemoteMaster) UnicastRemoteObject.exportObject(master, 0);
            registry.bind("master", stubMaster);

            // loop: wait for clients
            System.out.println("Waiting for enough clients...");
            // TODO: length volatile ?

            // !! length von tables
            while (registry.list().length-1 < nTableParts) {
            }

            System.out.println("Connected.");

            // communication with clients --> tables

            RemoteTable[] remoteTables = new RemoteTable[nTableParts];

            // TODO: gleichmäßige verteilung
            int seatsPerStub = nSeats / nTableParts;
            // TODO: lastenabhängige verteilung
            // int cores = stubTable.getCoreCount();
            int philosPerStub = nPhilosophers / nTableParts;

            // get remote tables
            int i = 1;
            int j = 0;
            for (RemoteTable stubTable : remoteTables) {
                stubTable = (RemoteTable) registry.lookup("table" + i++);
                System.out.println("table " + i + " found.");

                stubTable.init(seatsPerStub);

                // create and introduce philos
                for (; j < philosPerStub; j++) {
                    if (Arrays.asList(hungry).contains(j+"")) {
                        stubTable.addPhilosopher(j, "id " + j, true);
                        System.out.println("id " + j + " stomach seems to growl faster.");
                    } else {
                        stubTable.addPhilosopher(j, "id " + j, false);
                    }
                }
            }

            // --------------------------------------

            System.out.println("master enters room.");

            // create table
            System.out.println("table opens.");

            // start philosophers
            for (final RemotePhilosopher cur : master.getPhilosophers()) {
                System.out.println("found");
                cur.setThreadDaemon(true);
                cur.threadStart();
            }

            // start master
            master.start();

            // run time
            Thread.sleep(60000);

            System.out.println("table closes.");
            // stop all
            for (final RemotePhilosopher cur : master.getPhilosophers()) {
                cur.threadInterrupt();
            }
            master.interrupt();

        } catch (InterruptedException | NotBoundException | RemoteException | AlreadyBoundException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
