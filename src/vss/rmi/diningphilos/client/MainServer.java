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
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteTablepart;
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

        int nPhilosophers = 4;
        String[] hungry = {"0"};
        int nSeats = 4;
        int nTableparts = 2;


        try {

            // initiate registry
            LocateRegistry.createRegistry(1099);
            Registry registry = LocateRegistry.getRegistry();

            // initiate and bind master
            // TODO: server: try to use localmaster
            Master localMaster = new Master(nPhilosophers, nTableparts);
            RemoteMaster master = (RemoteMaster) UnicastRemoteObject.exportObject(localMaster, 0);
            registry.bind("master", master);

            // loop: wait for clients
            System.out.println("Waiting for enough clients...");
            // TODO: length volatile ?

            // !! length von tables
            while (registry.list().length-1 < nTableparts) {
            }

            System.out.println("Connected.");

            // communication with clients --> tables

            // TODO: gleichmäßige verteilung
            int seatsPerStub = nSeats / nTableparts;
            // TODO: lastenabhängige verteilung
            // int cores = stubTable.getCoreCount();
            int philosPerStub = nPhilosophers / nTableparts;

            // get remote tables
            int j = 0;
            for (int i = 0; i < nTableparts; i++) {

                // master knows all table parts
                RemoteTablepart tablepart = (RemoteTablepart) registry.lookup("table" + i);
                System.out.println("----- Tablepart " + i + " found:");
                System.out.println("It has " + seatsPerStub
                                    + " seats and "
                                    + philosPerStub
                                    + " philosophers.");

                // !! init local
                tablepart.initLocal(seatsPerStub);
                master.addTablepart(i, tablepart);

                // create and introduce philos
                // TODO: obergrenze: verteilung
                for (; j < (i+1)*philosPerStub; j++) {
                    if (Arrays.asList(hungry).contains(j+"")) {

                        // !!
                        master.addPhilosopher(j, tablepart.createPhilosopher(j, "id " + j, true, nSeats));
                        System.out.println("Philosoph [id " + j + "] enters room. He's hungry.");
                    } else {
                        master.addPhilosopher(j, tablepart.createPhilosopher(j, "id " + j, false, nSeats));
                        System.out.println("Philosoph [id " + j + "] enters room.");
                    }
                }
            }

            // init global
            for (RemoteTablepart tablepart : master.getTableparts()) {
                tablepart.initGlobal();
            }

            // --------------------------------------

            System.out.println("master enters room.");

            // create table
            System.out.println("table opens.");

            // start philosophers
            for (final RemotePhilosopher cur : master.getPhilosophers()) {
                cur.setThreadDaemon(true);
                cur.threadStart();
            }

            // start master
            localMaster.start();

            // run time
            Thread.sleep(60000);

            System.out.println("table closes.");
            // stop all
            for (final RemotePhilosopher cur : master.getPhilosophers()) {
                cur.threadInterrupt();
            }
            localMaster.interrupt();

        } catch (InterruptedException | NotBoundException | RemoteException | AlreadyBoundException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
