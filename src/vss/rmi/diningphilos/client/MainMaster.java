/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.client;

import vss.rmi.diningphilos.server.n.remote.objects.Master;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Scanner;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteTable;
import vss.rmi.diningphilos.server.n.remote.objects.Philosopher;
import vss.rmi.diningphilos.server.n.remote.objects.Table;

/**
 *
 * @author T500
 */
public class MainMaster {

    /**
     * Die Main Methode liest eine Anzahl an Philosophen und Sitzen und
     * erstellt entsprechend  einen Tisch und die Philosophen. Sie lässt
     * die Threads für eine bestimmte Zeit laufen und unterbricht danach
     * alle. Sie kann mit einem Raum verglichen werden, in der die Objekte
     * existieren.
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(final String[]args) throws InterruptedException {

        try {

            String serverIp = "localhost";

            Registry registry = LocateRegistry.getRegistry(serverIp, 1099);
            RemoteTable stubTable = (RemoteTable) registry.lookup("table0");



            // --------------------------------------

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

            // master feature
            final Master master = new Master(nPhilosophers);
            master.start();
            System.out.println("master enters room.");

            // create table
            System.out.println("table opens.");
            int cores = stubTable.getCoreCount();
            stubTable.setMaster(master); // TODO: serialize or not?
            stubTable.init(nSeats);

            for (int i = 0; i < nPhilosophers; i++) {
                if (Arrays.asList(hungry).contains(i+"")) {
                    master.getPhilosophers()[i] = new Philosopher("id " + i, (Table) stubTable, true);
                    System.out.println("id " + i + " stomach seems to growl faster.");
                } else {
                    master.getPhilosophers()[i] = new Philosopher("id " + i, (Table) stubTable, false);
                }
            }

            // start philosophers
            for (final Philosopher cur : master.getPhilosophers()) {
                cur.setDaemon(true);
                cur.start();
            }

            // run time
            Thread.sleep(60000);

            System.out.println("table closes.");
            // stop all
            for (final Philosopher cur : master.getPhilosophers()) {
                cur.interrupt();
            }

            master.interrupt();

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
