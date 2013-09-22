package SoSi.View;

import SoSi.Model.TickData;

/**
 * Interface für Klassen der GUI, welche bei der Wiedergabe stetig aktualisiert werden müssen. Die Aktualisierung
 * erfolgt mittels den Informationen aus TickData.<br>
 * (Die Klasse dient primär zur Aktualisierung von Werten, welche bei der Wiedergabe geändert werden, wie
 * Positionsangaben, Tore etc. Das Eintreten von Ereignissen oder das Ändern von Wiedergabezuständen (wie Play/Pause)
 * erfolgt mittels Observable-notifies.)
 */
public interface IRefreshable {
    /**
     * Dient zur Aktualisierung der Darstellung der Wiedergabe. Der Klasse werden somit die aktuellen Daten, welche zur
     * Darstellung der Wiedergabe benötigt werden, übergeben.
     * 
     * @param tickData
     *            Die Daten, die zur Aktualisierung verwendet werden sollen.
     * @param playbackSpeed
     *            Die aktuelle Wiedergabegeschwindigkeit (Dient zur Aktualisierung von Komponenten, welche die aktuelle
     *            Wiedergabegeschwindigkeit anzeigen).
     * @param simulationTickPosition
     *            Der aktuelle Stand der Vorausberechnung der Simulation.
     */
    void refresh(TickData tickData, double playbackSpeed, int simulationTickPosition);
}
