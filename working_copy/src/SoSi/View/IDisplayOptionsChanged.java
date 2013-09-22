package SoSi.View;

import java.awt.Color;

/**
 * Interface, welches verwendet wird, um die GUI bei einer Optionsänderung im SimulationOptionsFrame über die neuen
 * Parameter zu informieren.<br>
 * Dabei werden ausschließlich die Parameter übergeben, welche zur Darstellung der View notwendig sind.
 */
public interface IDisplayOptionsChanged {

    /**
     * Wird aufgerufen, wenn sich zur Darstellung relevante Daten geändert haben.
     * 
     * @param teamAColor
     *            Farbe, mit welcher die Spielfiguren des Teams A gezeichnet werden sollen.
     * @param TeamBColor
     *            Farbe, mit welcher die Spielfiguren des Teams B gezeichnet werden sollen.
     * @param soccerPanel
     *            Eine SoccerPanel-Instanz, welche bereits entsprechend der gewünschten Darstellung initialisiert wurde.
     * @param simulationDuration
     *            Die gegebenfalls veränderte Dauer des Simulation in Ticks.
     * @param soundsActivated
     *            Boolean-Flag, ob Sounds aktiviert werden sollen.
     */
    void changed(Color teamAColor, Color TeamBColor, SoccerPanel soccerPanel, int simulationDuration,
            boolean soundsActivated);
}
