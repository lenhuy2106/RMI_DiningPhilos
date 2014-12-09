/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * (C) Mathias Long Yan, myan@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.objects;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteTable;

/**
 * Ein Master ist eine Thread-Unterklasse, die alle Philosophen zu
 * einem Tisch kennt. Er berechnet fortlaufend die größte Differenz
 * der jeweiligen Speisensummen. Diese kann er ausschließlich lesen.
 * Übersteigt diese eine bestimmte Zahl, wird der entsprechende Philosoph
 * für eine bestimmte Dauer vom Esstisch gesperrt. Der Master wird von
 * der Main Klasse initiiert und terminiert.
 * @author Nhu-Huy Le, Mathias Long Yan
 */
public class Master extends Thread {

    /** Array of all philosophers. */
    private final Philosopher[] philosophers;

    /**
     * Ctor.
     * @param nPhilosophers Number of philosophers.
     */
    public Master(final int nPhilosophers) {
        philosophers = new Philosopher[nPhilosophers];
    }

    public void initiate(Registry registry) {

        try {

            RemoteTable stubTable = (RemoteTable) registry.lookup("table0");

            // --------------------------------------




            System.out.println("master enters room.");

            // create table
            System.out.println("table opens.");
            int cores = stubTable.getCoreCount();
            // stubTable.setMaster(master); // TODO: serialize or not?
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

        } catch (InterruptedException | RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start thread.
     */
    public void run() {

        while (!isInterrupted()) {
            Philosopher min = philosophers[0];
            Philosopher max = philosophers[0];

            if (philosophers[philosophers.length-1] != null) {
                for (final Philosopher cur : philosophers) {
                    if (cur.getMeals() < min.getMeals()) {
                        min = cur;
                    } else if (cur.getMeals() > max.getMeals()) {
                        max = cur;
                    }
                }

                // max differ of 5
                if (max.getMeals() - min.getMeals() > 5) {
                    max.ban();
                }
            }
        }
        System.out.println("master counts: ");
        int total = 0;

        for (final Philosopher cur : philosophers) {
            total += cur.getMeals();
            System.out.printf("%15s %s %3d meals.%n", cur.getPhilName(), "ate", cur.getMeals());
        }
        System.out.printf("- %d meals in total.%n", total);
        System.out.println("master leaves room.");
    }

    /**
     * Getter.
     * @return Philosophers array.
     */
    public Philosopher[] getPhilosophers() {
        return philosophers;
    }
}