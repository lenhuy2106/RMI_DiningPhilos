/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import vss.rmi.diningphilos.server.n.remote.objects.Philosopher;

/**
 *
 * @author T500
 */
public interface RemoteMaster extends Remote {
    Philosopher[] getPhilosophers() throws RemoteException;
}