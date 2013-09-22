package SoSi.Model.GameObjects;

import sep.football.Position;
import SoSi.Model.SoSiPosition;

/**
 * Beschreibt ein rundes Spielobjekt. Diese Klasse ist abstrakt angelegt und soll der Bauplan für alle Spielobjekte auf
 * dem Feld sein. Die grundlegendste Eigenschaft der Spielobjekte ist, dass sie eine Position innerhalb des Spielfeldes
 * und einen Durchmesser haben.
 */
public abstract class RadialGameObject {

    /**
     * Position des Spielobjekts, an dem es sich innerhalb der Simulationsfläche befindet.
     */
    protected Position position; 

    /**
     * Durchmesser des Spielobjekts
     */
    private final double diameter;

    /**
     * Erstellt ein Spielobjekt auf dem Feld und setzt es gleich.
     * 
     * @param position
     *            Die Position innerhalb des Spielfeldes.
     * @throws IllegalArgumentException
     *             Die Exception, wenn position den Wert null besitzt.
     */
    public RadialGameObject(Position position, double diameter) {
        if (position == null)
            throw new IllegalArgumentException("position is null");

        this.diameter = diameter;
        this.position = (SoSiPosition) position;
    }

    /**
     * Gibt die Position des Spielobjekts zurück.
     * 
     * @return Position des Spielobjekts.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Gibt den Durchmesser des Spielobjekts zurück.
     * 
     * @return Durchmesser des Spielobjekts.
     */
    public double getDiameter() {
        return diameter;
    }

}
