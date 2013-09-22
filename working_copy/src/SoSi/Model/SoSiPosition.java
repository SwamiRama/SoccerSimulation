package SoSi.Model;

import sep.football.Position;

/**
 * Pair-Klasse, die die x - und y - Koordinaten einer zweidimensionalen Position besitzt. Objekte dieser Klasse sind
 * immutable.
 */
public class SoSiPosition implements Position {

    /**
     * x-Koordinate der Klasse.
     */
    private final double X;

    /**
     * y-Koordinate der Klasse.
     */
    private final double Y;

    /**
     * Konstruktor der Klasse SoSiPosition.
     * 
     * @param x
     *            X-Koordinate der Pair-Klasse.
     * @param y
     *            Y-Koordiante der Pair-Klasse.
     */
    public SoSiPosition(double x, double y) {
        this.X = x;
        this.Y = y;
    }

    /**
     * Abfragen des x-Wertes.
     * 
     * @return y-Koordinate des Positionsobjekts.
     */
    public double getX() {
        return this.X;
    }

    /**
     * Abfragen des y-Wertes.
     * 
     * @return y-Koordinate des Positionsobjekts.
     */
    public double getY() {
        return this.Y;
    }

    @Override
    public String toString() {
        return String.format("(%f | %f)", this.getX(), this.getY());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Position ? (((Position) obj).getX() == this.getX())
                && (((Position) obj).getY() == this.getY()) : false;
    }
}
