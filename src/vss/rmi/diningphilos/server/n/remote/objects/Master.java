/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * (C) Mathias Long Yan, myan@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.objects;

import java.rmi.RemoteException;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteMaster;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemotePhilosopher;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteTablepart;

/**
 * Ein Master ist eine Thread-Unterklasse, die alle Philosophen zu
 * einem Tisch kennt. Er berechnet fortlaufend die größte Differenz
 * der jeweiligen Speisensummen. Diese kann er ausschließlich lesen.
 * Übersteigt diese eine bestimmte Zahl, wird der entsprechende Philosoph
 * für eine bestimmte Dauer vom Esstisch gesperrt. Der Master wird von
 * der Main Klasse initiiert und terminiert.
 * @author Nhu-Huy Le, Mathias Long Yan
 */
public class Master extends Thread implements RemoteMaster {

    /** Array of all philosophers. */
    private final RemotePhilosopher[] philosophers;
    private final RemoteTablepart[] tableparts;

    /**
     * Ctor.
     * @param nPhilosophers Number of philosophers.
     * @param nTableparts
     */
    public Master(final int nPhilosophers, final int nTableparts) {
        philosophers = new RemotePhilosopher[nPhilosophers];
        tableparts = new RemoteTablepart[nTableparts];
    }

    /**
     * Start thread.
     */
    public void run() {
        try {

            while (!isInterrupted()) {
                RemotePhilosopher min = philosophers[0];
                RemotePhilosopher max = philosophers[0];

                if (philosophers[philosophers.length-1] != null) {
                    for (final RemotePhilosopher cur : philosophers) {
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

            for (final RemotePhilosopher cur : philosophers) {
                total += cur.getMeals();
                System.out.printf("%15s %s %3d meals.%n", cur.getPhilName(), "ate", cur.getMeals());
            }
            System.out.printf("- %d meals in total.%n", total);
            System.out.println("master leaves room.");

        } catch (RemoteException rm) {
            // TODO: ausfallsicherung

        }
    }

    /**
     * Getter.
     * @return Philosophers array.
     */
    public RemotePhilosopher[] getPhilosophers() {
        return philosophers;
    }

    /**
     * Getter.
     * @return Tableparts array.
     */
    public RemoteTablepart[] getTableparts() {
        return tableparts;
    }


    public boolean addTablepart(final int id, RemoteTablepart rtp) {
        if (id <= tableparts.length) {
            tableparts[id] = rtp;
            return true;
        } else {
            return false;
        }
    }

    public boolean addPhilosopher(final int id, RemotePhilosopher rph) {
        if (id <= philosophers.length) {
            philosophers[id] = rph;
            return true;
        } else {
            return false;
        }
    }

    // private void handleCrash
}