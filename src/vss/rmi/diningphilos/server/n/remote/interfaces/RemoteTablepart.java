/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author T500
 */
public interface RemoteTablepart extends Remote {
    void initLocal(final int nSeats) throws RemoteException;
    void initGlobal() throws RemoteException;
    RemotePhilosopher createPhilosopher(final int id, final String name, final boolean hungry, final int nSeats) throws RemoteException;;
    int getCoreCount() throws RemoteException;
    void callOne() throws RemoteException;
    List<RemoteSeat> getOwnSeats() throws RemoteException;
    List<RemoteFork> getOwnForks() throws RemoteException;
    public List<RemoteSeat> getAllSeats() throws RemoteException;
    public List<RemoteFork> getAllForks() throws RemoteException;
    RemoteMaster getMaster() throws RemoteException;
    void setMaster(final RemoteMaster master) throws RemoteException;
}