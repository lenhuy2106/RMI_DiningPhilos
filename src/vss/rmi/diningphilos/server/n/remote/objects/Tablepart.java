/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * (C) Mathias Long Yan, myan@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.objects;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteTablepart;
import vss.rmi.diningphilos.server.n.MainClient;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteFork;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteMaster;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemotePhilosopher;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteSeat;

/**
 * Tisch Main Klasse.
 * Ein Tisch ist eine Klasse und erstellt für sich die gleiche Anzahl an
 * Sitzen und Gabeln. Er kennt auch ihre Reihenfolge, greift aber sonst
 * nicht mehr auf sie zu.
 * @author Nhu-Huy Le, Mathias Long Yan
 */
public class Tablepart implements RemoteTablepart {

    private List<RemoteSeat> ownSeats;
    private List<RemoteSeat> allSeats;
    private List<RemoteFork> ownForks;
    private List<RemoteFork> allForks;
    private RemoteMaster master;
    private RemoteTablepart remoteThis;

    // TODO: final?
    private int id;

    public Tablepart() {

        try {
            id = MainClient.registry.list().length-1; // minus master
            remoteThis = (RemoteTablepart) UnicastRemoteObject.exportObject(this, 0);

            MainClient.registry.bind("table" + id, remoteThis);
            System.out.println("table " + id + " ready.");

        } catch (RemoteException | AlreadyBoundException ex) {
            Logger.getLogger(Tablepart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Ctor
     * @param nOwnSeats Seats number.
     */
    public void initLocal(final int nOwnSeats) {

        try {
            // TODO: synchronized
            ownSeats = Collections.synchronizedList(new ArrayList<>());
            ownForks = Collections.synchronizedList(new ArrayList<>());
            master = (RemoteMaster) MainClient.registry.lookup("master");

            for (int i = 0; i < nOwnSeats; i++) {

                RemoteSeat rSeat = (RemoteSeat) UnicastRemoteObject.exportObject(new Seat(this), 0);
                RemoteFork rFork = (RemoteFork) UnicastRemoteObject.exportObject(new Fork(), 0);

                ownSeats.add(i, rSeat);
                ownForks.add(i, rFork);
            }
            // last fork is remote
            // forks[nSeats] =

        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(Tablepart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initGlobal() {

        try {
            allSeats = Collections.synchronizedList(new ArrayList<>());
            allForks = Collections.synchronizedList(new ArrayList<>());

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
        } catch (RemoteException ex) {
            Logger.getLogger(Tablepart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public RemotePhilosopher createPhilosopher(final int id, final String name, final boolean hungry) {

        // philosopher can walk
        return new Philosopher(name, remoteThis, hungry);
    }

    public RemoteSeat createSeat() {

        Seat seat = new Seat(this);
        ownSeats.add(seat);
        return seat;
    }

    public RemoteFork createFork() {

        Fork fork = new Fork();
        ownForks.add(fork);
        return fork;
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

    public int getId() {
        return id;
    }

    /**
     * Getter
     * @return Array of seats.
     */
    public List<RemoteSeat> getOwnSeats() {
        return ownSeats;
    }

    /**
     * Getter
     * @return Array of forks.
     */
    public List<RemoteFork> getOwnForks() {
        return ownForks;
    }

    public RemoteMaster getMaster() {
        return master;
    }

    public void setMaster(RemoteMaster master) {
        this.master = master;
    }

    public List<RemoteSeat> getAllSeats() {
        return allSeats;
    }

    public List<RemoteFork> getAllForks() {
        return allForks;
    }
}