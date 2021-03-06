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
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteFork;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemotePhilosopher;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteSeat;

/**
 * Eine Gabel ist eine gemeinsam genutzte Klasse.
 * Sie kann nur ausschließlich von einem Philosophen, der auf einer der
 * zwei zugehörigen, nebeneinander liegenden Plätzen sitzt, aufgenommen
 * werden. Dieser kennt die Gabel und setzt ggf. den booleschen Zustand
 * aufgenommen. Danach kann er von diesem wieder abgelegt werden.
 * @author Nhu-Huy Le, Mathias Long Yan
 */
public class Fork implements RemoteFork {

    /** Current holder of fork. */
    private RemotePhilosopher holder;
    /** Free or not free. */
    private boolean free = true;

    private final RemoteFork remoteThis;

    public Fork() throws RemoteException {
        remoteThis = (RemoteFork) UnicastRemoteObject.exportObject(this, 0);
    }

    /**
     * Picks fork.
     * @param examiner Philosopher trying to pick.
     * @return Success or no success.
     */
    public synchronized boolean pick(RemotePhilosopher examiner) {
        boolean success = false;

        if (free) {
            holder = examiner;
            free = false;
            success = true;
        }
        return success;
    }

    /**
     * Drops fork.
     */
    public void drop() {
        holder = null;
        free = true;
    }
}