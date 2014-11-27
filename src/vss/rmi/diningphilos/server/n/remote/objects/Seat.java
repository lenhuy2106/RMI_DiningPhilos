/*
 * (C) Nhu-Huy Le, nle@hm.edu
 * (C) Mathias Long Yan, myan@hm.edu
 * Oracle Corporation Java 1.8.0
 * Microsoft Windows 7 Professional
 * 6.1.7601 Service Pack 1 Build 7601
 */

package vss.rmi.diningphilos.server.n.remote.objects;

/**
 *Ein Platz ist eine gemeinsam genutzte Klasse.
 * Er weiß, ob er besetzt ist und wer auf ihm sitzt. Er kann von Philosophen
 * besetzt, wenn noch keiner auf dem Platz sitzt, und falls er besetzt ist,
 * von demjenigen verlassen werden. Diese Methoden müssen atomar und
 * synchronisiert sein.
 * @author Nhu-Huy Le, Mathias Long Yan
 */
public class Seat {

    /** Current holder of seat. */
    private Philosopher holder;
    /** Free or not free. */
    private boolean free = true;
    private final Table table;

    public Seat(final Table table) {
        this.table = table;
    }

    /**
     * Sit on the seat.
     * @param examiner Philosopher trying to sit.
     * @return Success or no success.
     */
    public synchronized boolean sit(Philosopher examiner) {
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
    public void leave() {
        holder = null;
        free = true;
        table.callAll();
    }
}