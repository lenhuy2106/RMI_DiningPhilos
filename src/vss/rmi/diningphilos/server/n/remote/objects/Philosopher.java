/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * (C) Mathias Long Yan, myan@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.objects;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteFork;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemotePhilosopher;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteSeat;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteTablepart;

/**
 * Philosoph Thread-Klasse.
 * Ein Philosoph ist eine Thread-Unterklasse mit Name, zugehörigem Tisch und
 * den booleschen Zuständen großer Hunger und ist verbannt. Er zählt bis drei
 * Speisen. In wiederholender Reihenfolge meditiert, isst und ggf. schläft er.
 * Gestartet und beendet wird ein Philosoph von der Hauptklasse. Er kann
 * versuchen, sich auf einen Platz zu setzen und eine der beiden dazugehörigen
 * Gabeln sowie danach die andere aufzunehmen. Die Philosophen kennen sich
 * untereinander nicht und können nach dem Instanziieren auch nur eigene
 * Attribute schreiben. Sonst keine anderer Philosophen und von keinem anderen
 * Objekt der Umgebung.
 * @author Nhu-Huy Le, Mathias Long Yan
 */
public class Philosopher extends Thread implements RemotePhilosopher {

    /** Name of the philosopher. */
    private final String name;
    /** Table of the philosopher. */
    private final RemoteTablepart tablepart;
    /** Hungry or not. */
    private final boolean hungry;
    /** Meal counter. */
    private int meals;
    /** Currently banned or not. */
    private boolean banned;

    private RemotePhilosopher remoteThis = null;

    /**
     * Ctor.
     * @param name Name of the philosopher.
     * @param tablePart Table of the Philosopher.
     * @param hungry Hungry or not.
     */
    public Philosopher(final String name, final RemoteTablepart tablePart, final boolean hungry) throws RemoteException {
        this.name = name;
        this.tablepart = tablePart;
        this.hungry = hungry;
        meals = 0;
        banned = false;
        setDaemon(true);
        // remote itself
        remoteThis = (RemotePhilosopher) UnicastRemoteObject.exportObject(this, 0);
    }

    public Philosopher(final String name, final RemoteTablepart tablePart, final boolean hungry, final int meals) throws RemoteException {
        this(name, tablePart, hungry);
        this.meals = meals;
    }

    public int lookForSeat() {
        int curTablePart = 0;
        try {
            int free = -1;
            int cur = -1;
            final boolean clockwise = Math.random() < 0.5;
            int tablePartLength = tablepart.getOwnSeats().size();
            // random direction?
            for (int i = 0; i < tablePartLength; i++) {
                cur = clockwise ? i : (tablePartLength-1)-i;
                if (tablepart.getOwnSeats().get(cur).sit(this)) {
                    free = cur;
                    break;
                }
            }

            // remote look TODO: optimize
            if (free == -1) {
                for (RemoteSeat seat : tablepart.getAllSeats()) {
                    if (seat.sit(remoteThis)) {
                        free = 0;
                        break;
                    }
                }
            }

            return free;

        } catch (RemoteException ex) {
            System.err.println(getPhilName()
                    + " failed at lookForSeat().");
            threadWait();
            return lookForSeat();
        }
    }

    /**
     * Going to eat.
     * @throws InterruptedException
     * @throws IndexOutOfBoundsException
     * @throws ArithmeticException
     */
    public void search() throws InterruptedException {
        try {
            int i = 0;
            RemoteFork first;
            RemoteFork second;
            System.out.printf("%-30s %s %n", name, "seaches seat.");
            // waiting for a seat
            while (true) {
                i = lookForSeat();
                if (i != -1) {
                    break;
                }

                // TODO: remoteMe ot this?
                synchronized (this) {
                    System.out.println(name + " is waiting.");
                    this.wait();
                }
            }

            eat(i);

        } catch (RemoteException | IndexOutOfBoundsException | ArithmeticException ex) {
            System.err.println(getPhilName()
                                + " failed at eat().");
            threadWait();
            search();
        }
    }

    private void eat(int i) throws RemoteException, InterruptedException {
        RemoteFork first;
        RemoteFork second;
        int nAllSeats = tablepart.getAllSeats().size();
        final int left = (i) % nAllSeats;
        final int right = (i + 1) % nAllSeats;
        System.out.printf("%-45s %s %n", name, "needs forks " + left + " " + right + ".");
        //waiting for fork
        while (true) {
            // left or right first
            final boolean decision = Math.random() < 0.5;
            first = decision ? tablepart.getAllForks().get(left) : tablepart.getAllForks().get(right);
            second = decision ? tablepart.getAllForks().get(right) : tablepart.getAllForks().get(left);
            // TODO: first, second -> null
            if (first.pick(remoteThis)) {
                if (second.pick(remoteThis)) {
                    break;

                } else {
                    first.drop();
                }
            }
        }
        System.out.printf("%-60s %s %d.%n", name, "eats at", i);
        Thread.sleep(1);
        second.drop();
        first.drop();
        tablepart.getOwnSeats().get(i).leave();
        meals++;
    }

    public synchronized State getThreadState() {
        return getState();
    }

    public synchronized void setThreadDaemon(final boolean flag) {
        this.setDaemon(flag);
    }

    public synchronized void threadStart() {
        this.start();
    }

    public synchronized void threadInterrupt() {
        this.interrupt();
    }

    public synchronized void threadNotifyOrStart() {
        if (this.getState().equals(State.WAITING)) {
            this.notify();
            System.out.println(getPhilName()
                    + " has been woken up.");

        } else if (this.getState().equals(State.NEW)) {
            this.start();
            System.out.println(getPhilName()
                                + " has been added.");

        } else {
            // // TODO: delete print - performance leak
            System.err.println(this.getPhilName()
                    + " has unexpected state: "
                    + this.getState());

            // // recursive loop validation for thread to wait
            // threadWait();
        }
    }

    // CONTINUE: synchronized block

    /**
     * Only called by this.
     */
    private synchronized void threadWait() {
        try {
            System.out.println(name + " is waiting for crash handling.");
            wait();
        } catch (InterruptedException ex) {
            Logger.getLogger(Philosopher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Starts thread.
     */
    public void run() {
        int mealsLeft = 3;
        if (hungry) {
            setPriority(Thread.MAX_PRIORITY);
        }

        try {
            while (!isInterrupted()) {
                System.out.println(name + " meditates.");
                // int meditate = hungry ? 0 : 5;
                Thread.sleep(5);
                System.out.printf("%-15s %s %n", name, "gets hungry.");
                search();
                System.out.printf("%-75s %s %n", name, "leaves.");
                if (--mealsLeft == 0) {
                    System.out.printf("%-90s %s %n", name, "sleeps.");
                    Thread.sleep(10);
                    mealsLeft = 3;

                } else if (banned) {
                    System.out.printf("%-90s %s %n", name, "banned.");
                    Thread.sleep(5);
                    banned = false;
                }
            }

            System.err.println("Unreachable.");

        } catch (InterruptedException ex) {
            System.out.println(name + " starves to death.");
        }
    }

    /**
     * Getter.
     * @return Name.
     */
    public String getPhilName() {
        return name;
    }

    /**
     * Getter
     * @return meals sum.
     */
    public int getMeals() {
        return meals;
    }

    /**
     * Ban this philosopher.
     */
    public void ban() {
        banned = true;
    }

    public void reportCrash() {
        // decapitated
    }
}