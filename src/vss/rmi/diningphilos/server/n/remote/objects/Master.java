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
import java.util.Collections;
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
    private final List<RemoteTablepart> tableparts;

    private final List<RemoteSeat> allSeats;
    private final List<RemoteFork> allForks;
    private int nPhilosophers;
    private int nSeats;

    /**
     * Ctor.
     * @param nPhilosophers Number of philosophers.
     * @param nTableparts
     */
    public Master(final int nPhilosophers, final int nTableparts, final int nSeats) {
        // remote: read only lists
        // TODO: philo iteration concurrent exc?
        philosophers = new ArrayList<>(nPhilosophers);
        tableparts = new ArrayList<>(nTableparts);
        allSeats = Collections.synchronizedList(new ArrayList<>());
        allForks = Collections.synchronizedList(new ArrayList<>());
        this.nSeats = nSeats;
        this.nPhilosophers = nPhilosophers;
    }

    /**
     * Start thread.
     */
    public void run() {

        while (!isInterrupted()) {

            try {
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

            } catch (RemoteException rm) {
                handleCrash(0);
            }
        }

        try {
            System.out.println("master counts: ");
            int total = 0;

            for (final RemotePhilosopher cur : philosophers) {
                total += cur.getMeals();
                System.out.printf("%15s %s %3d meals.%n", cur.getPhilName(), "ate", cur.getMeals());
            }
            System.out.printf("- %d meals in total.%n", total);
            System.out.println("master leaves room.");

        } catch (RemoteException ex) {
            Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.exit(0);
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
    public List<RemoteTablepart> getTableparts() {
        return tableparts;
    }

    public List<RemoteSeat> getAllSeats() {
        return allSeats;
    }

    public List<RemoteFork> getAllForks() {
        return allForks;
    }

    public void openUI() {

        try {
            // hardcoded TODO: UI
            Thread.sleep(5000);

            for (RemoteTablepart tablepart : tableparts) {

                // add philosopher
                nPhilosophers++;
                RemotePhilosopher philo = tablepart.createPhilosopher(nPhilosophers,
                                                                "id " + nPhilosophers,
                                                                true);
                philosophers.add(philo);

                // add seat, fork
                nSeats++;
                RemoteSeat seat = tablepart.createSeat();
                RemoteFork fork = tablepart.createFork();
            }

            for (RemoteTablepart tablepart : tableparts) {
                // reload all refs (secure)
                tablepart.initGlobal();
            }
        } catch (RemoteException | InterruptedException ex) {
            Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // TODO: faire verteilung
    public void handleCrash(int i) {

        int idTablepart = 0;

        // find crashed tablepart
        try {
            for (RemoteTablepart rtp : tableparts) {
                rtp.getId();
                idTablepart++;
            }
        }

        catch (RemoteException ex1) {
            System.out.println("Tablepart " + idTablepart + " crashed.");

            tableparts.remove(i);
            recoverPhilosophers();
            recoverSeatsAndForks();
            run();
        }
    }

    private void recoverPhilosophers() {

        int idPhilo = 0;

        // find crashed philosopher
        try {
            for (RemotePhilosopher rph : philosophers) {
                rph.getPhilName();
                idPhilo++;
            }

        } catch (RemoteException ex) {

            for (RemoteTablepart rtp : tableparts) {
                try {
                    // TODO: was he hungry?
                    rtp.createPhilosopher(idPhilo, "id " + idPhilo, true);
                    break; // all on first tablepart ...

                // recovery client crashed
                } catch (RemoteException ex1) {
                    handleCrash(0);
                    break;
                }
            }
        }
    }

    private void recoverSeatsAndForks() {

        int idSeat = 0;

        // find crashed philosopher
        try {
            for (RemoteSeat rst : allSeats) {
                rst.leave();
                allForks.get(idSeat).drop();
                idSeat++;
            }

        } catch (RemoteException ex) {

            for (RemoteTablepart rtp : tableparts) {
                try {
                    allSeats.add(rtp.createSeat());
                    allForks.add(rtp.createFork());
                    break; // all on first tablepart ...

                // recovery client crashed
                } catch (RemoteException ex1) {
                    handleCrash(0);
                    break;
                }
            }
        }
    }

    private void initAll() {
        for (RemoteTablepart tablepart : tableparts) {
            try {
                // reload all refs (secure)
                tablepart.initGlobal();
            } catch (RemoteException ex) {
                handleCrash(0);
                break;
            }
        }
    }
}