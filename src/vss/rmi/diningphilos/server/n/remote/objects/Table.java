/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * (C) Mathias Long Yan, myan@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.objects;

import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteTable;

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
    private Master master;

    public Table() {}

    /**
     * Ctor
     * @param nSeats Seats number.
     */
    public void init(final int nSeats) {

        seats = new Seat[nSeats];
        forks = new Fork[nSeats];

        for (int i = 0; i < nSeats; i++) {
            seats[i] = new Seat(this);
            forks[i] = new Fork();
        }
    }

    public int getCoreCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    public void callAll() {
        for (Philosopher phil : master.getPhilosophers()) {
            phil.come();
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

    public Master getMaster() {
        return master;
    }

    public void setMaster(Master master) {
        this.master = master;
    }
}