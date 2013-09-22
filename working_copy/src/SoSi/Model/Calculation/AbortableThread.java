package SoSi.Model.Calculation;

/**
 * Von {@link Thread} erbende abstrakte Klasse, welche um eine Methode zum Abbrechen von Threads erweitert wurde.<br>
 * Bei einem entsprechenden Aufruf von {@link #abort()} gibt die Methode {@link #getIsAborted()} true zurück, was der
 * erbenden Klasse signalisiert, dass die Berechnung abgebrochen soll. Dabei muss die erbende Klasse stets den aktuellen
 * Status prüfen, da die abstrakte Klasse selbst nicht die eigentliche Berechnung unterbrechen kann.
 */
public abstract class AbortableThread extends Thread {

    /**
     * Flag, ob Berechnung abgebrochen wurde bzw. werden soll.
     */
    private volatile boolean isAborted = false;

    /**
     * Erstellt eine neue AbortableThread-Instanz, welche einen neuen DefaultUncaughtExceptionHandler festlegt. In
     * diesem werden erwartete Exceptions, wie ThreadDeath und IllegalMonitorStateException abgefangen und ignoriert.
     * Andere Exceptions werden wie gewohnt in der Konsole ausgegeben.
     */
    public AbortableThread() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if ((e instanceof IllegalMonitorStateException) || (e instanceof ThreadDeath)) {
                    // Durch .stop() verursachte Exceptions werden abgefangen und ignoriert
                } else {
                    // Alle anderen Exceptions auf Konsole ausgeben.
                    System.err.print(String.format("Exception in thread \"%s\" ", t.getName()));
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Gibt den aktuellen Status zurück, ob der Thread bzw. die aktuelle Aktion abgebrochen werden soll.
     * 
     * @return <code>true</code>, falls die aktuelle Aktion abgebrochen werden soll, <code>false</code> andernfalls.
     */
    public boolean getIsAborted() {
        return this.isAborted;
    }

    /**
     * Methode, um den Thread abzubrechen und das entsprechende Abbruch-Flag {@link #isAborted} für
     * {@link #getIsAborted()} zu setzen
     */
    public void abort() {
        this.isAborted = true;
    }

}
