/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * (C) Mathias Long Yan, myan@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.objects;

import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteMaster;

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
    private final Philosopher[] philosophers;

    /**
     * Ctor.
     * @param nPhilosophers Number of philosophers.
     */
    public Master(final int nPhilosophers) {
        philosophers = new Philosopher[nPhilosophers];
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