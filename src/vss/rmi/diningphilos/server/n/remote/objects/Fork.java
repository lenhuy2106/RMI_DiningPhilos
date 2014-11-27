/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * (C) Mathias Long Yan, myan@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.objects;

import vss.rmi.diningphilos.server.n.remote.interfaces.RemoteFork;

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
    private Philosopher holder;
    /** Free or not free. */
    private boolean free = true;

    /**
     * Picks fork.
     * @param examiner Philosopher trying to pick.
     * @return Success or no success.
     */
    public synchronized boolean pick(Philosopher examiner) {
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