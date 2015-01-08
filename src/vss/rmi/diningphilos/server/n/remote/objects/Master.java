/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * (C) Mathias Long Yan, myan@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.objects;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
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

    Registry registry;

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
    public Master(Registry reg, final int nPhilosophers, final int nTableparts, final int nSeats) {
        this.registry = reg;
        philosophers = Collections.synchronizedList(new ArrayList<>(nPhilosophers));
        tableparts = new ArrayList<>(nTableparts);
        allSeats = Collections.synchronizedList(new ArrayList<>());
        allForks = Collections.synchronizedList(new ArrayList<>());
        this.nSeats = nSeats;
        this.nPhilosophers = nPhilosophers;
    }

    public void proxyBind (String name, Remote obj) {
        try {
            registry.bind(name, obj);
        } catch (RemoteException | AlreadyBoundException ex) {
            Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Start thread.
     */
    public void run() {
        while (!isInterrupted()) {
            try {
                RemotePhilosopher min = philosophers.get(0);
                RemotePhilosopher max = philosophers.get(0);
                // meals regulation by max difference
                if (philosophers.get(philosophers.size()-1) != null) {
                    for (int i = 0; i < philosophers.size(); i++) {
                        RemotePhilosopher cur = philosophers.get(i);
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



    public void handleCrash(int i) {
        // TODO: faire verteilung

        System.out.println("------ Start crash handling:");

        // hibernateAll();

        int idTablepart = 0;

        // find crashed tablepart
        try {
            for (RemoteTablepart rtp : tableparts) {
                rtp.getId();
                idTablepart++;
            }
        }

        catch (RemoteException ex1) {
            // TODO: counter resets
            System.out.println("Tablepart " + idTablepart + " crashed.");

            tableparts.remove(idTablepart);
            recoverSeatsAndForks();
            recoverPhilosophers();

            System.out.println("\nRecovery successful: Restarting Now!");
            initAll();
            restartPhilosophers();
            // restart master control
            run();
        }
    }

    private void restartPhilosophers() {
        for (int j = 0; j < philosophers.size(); j++) {
            RemotePhilosopher rp = philosophers.get(j);

            try {
                philosophers.get(j).getPhilName();
                System.out.println(philosophers.get(j));
                System.out.println("wake up " + philosophers.get(j).getPhilName());
                philosophers.get(j).threadNotifyOrStart();

            } catch (RemoteException ex) {
                System.err.println("SEVERE: Starting philosophers failed.");
                handleCrash(0);
                break;
            }
        }
    }

    private void recoverPhilosophers() {

        System.out.print("\nRecover Philosophers...");

        int idPhilo = 0;
        int avgMeals = getAvgMeals();

        for (RemotePhilosopher rph : philosophers) {
        // find crashed philosopher
            try {
                System.out.print("[" + rph.getPhilName() + "]");
                // philo threads are self-responsible for crash wait state

            } catch (RemoteException ex) {

                // find subclient TODO: not all on first tablepart ...
                // TODO: not every tablepart
                for (RemoteTablepart rtp : tableparts) {
                    try {
                        // TODO: was he hungry?
                        RemotePhilosopher rp = rtp.createPhilosopher(idPhilo, "id " + idPhilo, true, avgMeals);
                        philosophers.set(idPhilo, rp);
                        System.out.print("[id " + idPhilo + " -> " + rtp.getId() + "]");

                    } catch (RemoteException ex1) {
                        // recovery client crashed
                        handleCrash(0);
                        break;
                    }
                }
            }

            idPhilo++;
        }
    }

    private void recoverSeatsAndForks() {
        System.out.print("\nRecover Seats/Forks...");
        int idSeat = 0;
        for (RemoteSeat rst : allSeats) {
            // find crashed philosopher
            try {
                rst.leave();
                allForks.get(idSeat).drop();
                System.out.print("[" + idSeat + "]");

            } catch (RemoteException ex) {
                // find subclient TODO: not all on first tablepart ...
                for (RemoteTablepart rtp : tableparts) {
                    try {
                        allSeats.set(idSeat, rtp.createSeat());
                        allForks.set(idSeat, rtp.createFork());
                        System.out.print("[" + idSeat + " -> " + rtp.getId() + "]");

                    // recovery client crashed
                    } catch (RemoteException ex1) {
                        ex1.printStackTrace();
                        // handleCrash(0);
                    }
                }
            }

            idSeat++;
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

    public int getAvgMeals() {
        int avg = 0;
        for (RemotePhilosopher rp : philosophers) {
            try {
                rp.getPhilName();
                avg += rp.getMeals();

            } catch (RemoteException ex) {
                // skip crashed philos
            }
        }

        avg /= philosophers.size();
        System.out.println("Current average meals count: " + avg);
        return avg;
    }

    public void setPhilosopher(final int id, final RemotePhilosopher rp) throws RemoteException {
        philosophers.set(id, rp);
    }
}