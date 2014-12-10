/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import vss.rmi.diningphilos.server.n.remote.objects.Seat;
import vss.rmi.diningphilos.server.n.remote.objects.Fork;
import vss.rmi.diningphilos.server.n.remote.objects.Master;

/**
 *
 * @author T500
 */
public interface RemoteTable extends Remote {
    void init(final int nSeats) throws RemoteException;
    void addPhilosopher(final int id, final String name, final boolean hungry) throws RemoteException;;
    int getCoreCount() throws RemoteException;
    void callOne() throws RemoteException;
    Seat[] getSeats() throws RemoteException;
    Fork[] getForks() throws RemoteException;
    Master getMaster() throws RemoteException;
    void setMaster(final Master master) throws RemoteException;
}