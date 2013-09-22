package SoSi.View;

/**
 * Zustandsangaben, welche Komponenten SimulationOptionsFrame darstellen und anbieten soll.
 */
public enum SimulationOptionsFrameType {
    /**
     * Zustand für "Neue Simulation erstellen" (Dabei werden dem Benutzer sämtliche vorhandenen Optionen angeboten).
     */
    NEW_SIMULATION,

    /**
     * Zustand für "Simulationseinstellungen" (Dabei werden dem Benutzer nur Optionen angeboten, welche während einer
     * aktuellen Simulation geändert werden können, wie z.B. Farben der Spieler, Perspektive der Darstellung etc.).
     */
    CHANGE_OPTIONS,

    /**
     * Zustand für "Neues Darstellungsfenster" (Dabei werden dem Benutzer nur Optionen angeboten, welche zum Erstellen
     * eines zusätzlichen Darstellungsfensters relevant sind. Dies sind weitgehend die selben Optionen wie bei
     * CHANGE_OPTIONS).
     */
    NEW_VIEW_FRAME
}
