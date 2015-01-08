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
public interface RemoteMaster extends Remote {
    List<RemotePhilosopher> getPhilosophers() throws RemoteException;
    List<RemoteTablepart> getTableparts() throws RemoteException;
    List<RemoteSeat> getAllSeats() throws RemoteException;
    List<RemoteFork> getAllForks() throws RemoteException;
    void proxyBind (String name, Remote obj) throws RemoteException;
    void handleCrash(final int i) throws RemoteException;
    void setPhilosopher(final int id, final RemotePhilosopher rp) throws RemoteException;
}
