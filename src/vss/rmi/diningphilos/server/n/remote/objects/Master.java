/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * (C) Mathias Long Yan, myan@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.objects;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteFork;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteMaster;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemotePhilosopher;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteSeat;
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
    private final List<RemotePhilosopher> philosophers;
    private final RemoteTablepart[] tableparts;
    private int nPhilosophers;
    private int nSeats;

    /**
     * Ctor.
     * @param nPhilosophers Number of philosophers.
     * @param nTableparts
     */
    public Master(final int nPhilosophers, final int nTableparts, final int nSeats) {
        philosophers = new ArrayList<>(nPhilosophers);
        tableparts = new RemoteTablepart[nTableparts];
        this.nSeats = nSeats;
        this.nPhilosophers = nPhilosophers;
    }

    /**
     * Start thread.
     */
    public void run() {
        try {

            while (!isInterrupted()) {
                RemotePhilosopher min = philosophers.get(0);
                RemotePhilosopher max = philosophers.get(0);

                if (philosophers.get(philosophers.size()-1) != null) {
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
    public List<RemotePhilosopher> getPhilosophers() {
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

    public int addPhilosopher(RemotePhilosopher rph) {

            philosophers.add(rph);
            return philosophers.size();
    }

    public void openUI() {

        // hardcoded TODO: UI
        for (int i = 0; i < tableparts.length; i++) {
            try {

                // add philosopher
                nPhilosophers++;
                RemotePhilosopher philo = tableparts[i].createPhilosopher(nPhilosophers,
                                                                "id " + nPhilosophers,
                                                                true);
                addPhilosopher(philo);

                // add seat, fork
                nSeats++;
                RemoteSeat seat = tableparts[i].createSeat();
                RemoteFork fork = tableparts[i].createFork();

                for (RemoteTablepart tablepart : tableparts) {
                    tablepart.getAllSeats().add(seat);
                    tablepart.getAllForks().add(fork);
                }

            } catch (RemoteException ex) {
                Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // private void handleCrash
}