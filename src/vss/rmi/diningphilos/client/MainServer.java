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
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteFork;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteMaster;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemotePhilosopher;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteSeat;
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
     * @param args /-
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
        // dynamic number of clients/tableparts?
        System.out.println("Number TableParts:");
        final int nTableparts = in.nextInt();

        try {
            // initiate registry
            LocateRegistry.createRegistry(1099);
            Registry registry = LocateRegistry.getRegistry();
            // initiate and bind master
            // TODO: server: try to use localmaster
            Master localMaster = new Master(registry, nPhilosophers, nTableparts, nSeats);
            RemoteMaster rm = (RemoteMaster) UnicastRemoteObject.exportObject(localMaster, 0);
            registry.bind("master", rm);
            Master master = localMaster;
            // loop: wait for clients
            System.out.println("Waiting for " + nTableparts +" clients...");
            // TODO: length volatile ?
            // !! length von tables
            while (registry.list().length-1 < nTableparts) {
                // wait for all ready
            }

             System.out.println("Connected.");
            // communication with clients --> tables
            // TODO: gleichmäßige verteilung
            int seatsPerStub[] = new int[nTableparts];
            for (int i = 0; i < nSeats; i++) {
                seatsPerStub[i%nTableparts] += 1;
            }
            // TODO: lastenabhängige verteilung
            // int cores = stubTable.getCoreCount();
            int philosPerStub[] = new int[nTableparts];
            for (int i = 0; i < nPhilosophers; i++) {
                philosPerStub[i%nTableparts] += 1;
            }
            // get remote tables
            int j = 0;
            for (int i = 0; i < nTableparts; i++) {
                // master knows all table parts
                RemoteTablepart tablepart = (RemoteTablepart) registry.lookup("table" + i);
                System.out.print("----- Tablepart " + i + " found -");
                System.out.println(seatsPerStub[i]
                                    + " seats and "
                                    + philosPerStub[i]
                                    + " philosophers.");

                // init local
                tablepart.initLocal(seatsPerStub[i]);
                master.getTableparts().add(i, tablepart);
                List<RemotePhilosopher> philos = master.getPhilosophers();
                // create and introduce philos
                // TODO: obergrenze: verteilung
                for (; j < (i+1)*philosPerStub[i]; j++) {
                    if (Arrays.asList(hungry).contains(j+"")) {
                        philos.add(tablepart.createPhilosopher(j, "id " + j, true));
                        System.out.println("Philosopher [id " + j + "] enters room. He's hungry.");

                    } else {
                        philos.add(tablepart.createPhilosopher(j, "id " + j, false));
                        System.out.println("Philosopher [id " + j + "] enters room.");
                    }
                }
            }

            // init Master: Seats/Forks
            List<RemoteSeat> allSeats = master.getAllSeats();
            List<RemoteFork> allForks = master.getAllForks();
            for (RemoteTablepart tablepart : master.getTableparts()) {
                // add all table seats
                for (RemoteSeat seat : tablepart.getOwnSeats()) {
                    allSeats.add(seat);
                }

                // add all table forks
                for (RemoteFork fork : tablepart.getOwnForks()) {
                    allForks.add(fork);
                }
            }

            // init global
            for (RemoteTablepart tablepart : master.getTableparts()) {
                tablepart.initGlobal();
            }

            // --------------------------------------

            System.out.println("master enters room.");
            System.out.println("- There are "
                                + master.getAllSeats().size()
                                + " seats/forks overall.");
            // create table
            System.out.println("table opens.");
            // start philosophers
            for (final RemotePhilosopher cur : master.getPhilosophers()) {
                // cur.setThreadDaemon(true); -- set in ctor
                cur.threadStart();
            }

            // start master
            localMaster.start();
            // run time thread
            new Thread(() -> {
                try {
                    Thread.sleep(60000);

                    System.out.println("table closes.");
                    // stop all
                    for (final RemotePhilosopher cur : master.getPhilosophers()) {
                        cur.threadInterrupt();
                    }

                    localMaster.interrupt();

                } catch (InterruptedException | RemoteException ex) {
                    Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
                }

            }).start();
            // start UI
            openUI(localMaster);

        } catch (NotBoundException | RemoteException | AlreadyBoundException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // TODO: transfer to master class
    public static void openUI(Master master) {
        try {
            // hardcoded TODO: UI
            Thread.sleep(5000);
            int nPhilos = master.getPhilosophers().size();
            for (RemoteTablepart rtp : master.getTableparts()) {
                // add philosopher
                nPhilos++;
                System.out.print("Add philosopher...");
                RemotePhilosopher philo = rtp.createPhilosopher(nPhilos,
                                                                "id " + nPhilos,
                                                                true,
                                                                master.getAvgMeals());
                master.getPhilosophers().add(philo);
                philo.threadStart();
                System.out.println("[id " + nPhilos + " -> " + rtp.getId() + "]");
    //                // add seat, fork
    //                nSeats++;
    //                RemoteSeat seat = tablepart.createSeat();
    //                RemoteFork fork = tablepart.createFork();
            }

            for (RemoteTablepart tablepart : master.getTableparts()) {
                // reload all refs (secure)
                tablepart.initGlobal();
            }

        } catch (RemoteException | InterruptedException ex) {
            Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}