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
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteTable;
import vss.rmi.diningphilos.server.n.MainClient;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteMaster;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemotePhilosopher;

/**
 * Tisch Main Klasse.
 * Ein Tisch ist eine Klasse und erstellt für sich die gleiche Anzahl an
 * Sitzen und Gabeln. Er kennt auch ihre Reihenfolge, greift aber sonst
 * nicht mehr auf sie zu.
 * @author Nhu-Huy Le, Mathias Long Yan
 */
public class Table implements RemoteTable {

    private Seat[] seats;
    private Fork[] forks;
    private RemoteMaster master;

    public Table() {}

    /**
     * Ctor
     * @param nSeats Seats number.
     */
    public void init(final int nSeats) {

        try {
            seats = new Seat[nSeats];
            forks = new Fork[nSeats];
            master = (RemoteMaster) MainClient.registry.lookup("master");

            for (int i = 0; i < nSeats; i++) {
                seats[i] = new Seat(this);
                forks[i] = new Fork();
            }
            // last fork is remote
            // forks[nSeats] =

        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public RemotePhilosopher addPhilosopher(final int id, final String name, final boolean hungry) {

        RemotePhilosopher stubPhilo = null;

        try {
            Philosopher ph = new Philosopher(name, this, hungry);
            stubPhilo = (RemotePhilosopher) UnicastRemoteObject.exportObject(ph, 0);

        } catch (RemoteException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stubPhilo;
    }

    public int getCoreCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    public void callOne() throws RemoteException {
        for (RemotePhilosopher phil : master.getPhilosophers()) {
            if (phil.getThreadState().equals(Thread.State.WAITING)) {
                synchronized (phil) {
                    phil.notify();
                }
                break;
            }
        }
    }

    /**
     * Getter
     * @return Array of seats.
     */
    public Seat[] getSeats() {
        return seats;
    }

    /**
     * Getter
     * @return Array of forks.
     */
    public Fork[] getForks() {
        return forks;
    }

    public RemoteMaster getMaster() {
        return master;
    }

    public void setMaster(Master master) {
        this.master = master;
    }
}