package SoSi.Model;

/**
 * Interface, um andere Klasseninstanzen über den Fortschritt einer Aktion zu informieren. Dabei wird i.d.R. ein
 * Wertebereich von 0 bis 100 angenommen.
 */
public interface IProgressInformation {

    /**
     * Gibt den Fortschritt der aktuellen Aktion zurück. Der Wertebereich der Rückgabe liegt zwischen 0 und 100.
     * 
     * @return Fortschritt der Aktion in Prozent von 0 bis 100.
     */
    public int getProgress();

}
