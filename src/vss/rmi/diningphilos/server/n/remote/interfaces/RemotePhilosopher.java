/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.interfaces;

import java.lang.Thread.State;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author T500
 */
public interface RemotePhilosopher extends Remote {
    int lookForSeat() throws RemoteException;
    State getThreadState() throws RemoteException;
    void setThreadDaemon(final boolean flag) throws RemoteException;
    void threadStart() throws RemoteException;
    void threadInterrupt() throws RemoteException;
    void eat()throws InterruptedException, RemoteException;
    void run() throws RemoteException;
    String getPhilName() throws RemoteException;
    int getMeals() throws RemoteException;
    void ban() throws RemoteException;
}
