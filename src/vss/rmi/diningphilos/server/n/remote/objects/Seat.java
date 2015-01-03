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
import vss.rmi.diningphilos.server.n.remote.interfaces.RemotePhilosopher;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteSeat;
import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteTablepart;

/**
 *Ein Platz ist eine gemeinsam genutzte Klasse.
 * Er weiß, ob er besetzt ist und wer auf ihm sitzt. Er kann von Philosophen
 * besetzt, wenn noch keiner auf dem Platz sitzt, und falls er besetzt ist,
 * von demjenigen verlassen werden. Diese Methoden müssen atomar und
 * synchronisiert sein.
 * @author Nhu-Huy Le, Mathias Long Yan
 */
public class Seat implements RemoteSeat {

    /** Current holder of seat. */
    private RemotePhilosopher holder;
    /** Free or not free. */
    private boolean free = true;
    private final RemoteTablepart tablepart;
    private final RemoteSeat remoteThis;

    public Seat(final RemoteTablepart tablepart) throws RemoteException {
        this.tablepart = tablepart;
        remoteThis = (RemoteSeat) UnicastRemoteObject.exportObject(this, 0);
    }

    /**
     * Sit on the seat.
     * @param examiner Philosopher trying to sit.
     * @return Success or no success.
     */
    public synchronized boolean sit(RemotePhilosopher examiner) {
        boolean success = false;

        if (free) {
            holder = examiner;
            free = false;
            success = true;
        }
        return success;
    }

    /**
     * Leave the seat.
     */
    public void leave() throws RemoteException {
        holder = null;
        free = true;
        tablepart.callOne();
    }

    public RemoteTablepart getTablepart() throws RemoteException {
        return tablepart;
    }
}