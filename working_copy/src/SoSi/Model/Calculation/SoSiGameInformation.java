package SoSi.Model.Calculation;

import SoSi.Model.GamePhysics.PhysicsCorrectPositions;
import sep.football.GameInformation;

/**
 * Beinhaltet allgemeine Informationen, die während der gesamten Simulation gelten, wie die Maße des Balls, eines
 * Spielers, eines Tores, des Spielfelds, sowie die Gesamtzahl der Ticks einer Simulation. Diese Daten bilden sozusagen
 * die Rahmenbedingungen der Simulation. Die Klasse wird unter anderem von den KIs benutzt, um ihre Entscheidungen zu
 * treffen.
 */
public class SoSiGameInformation implements GameInformation {

    /**
     * Die Weite des Spielfeldes auf dem gespielt wird.
     */
    private final double fieldWidth;

    /**
     * Die Länge des Spielfeldes auf dem gespielt wird.
     */
    private final double fieldLength;

    /**
     * Die Länge des Tores.
     */
    private final double goalSize;

    /**
     * Der Durchmesser eines Spielers.
     */
    private final double playerDiameter;

    /**
     * Der Durchmesser eines Balles.
     */
    private final double ballDiameter;

    /**
     * Die Höchstanzahl an ticks, die berechnet werden.
     */
    private final int maximumTickNumber;

    /**
     * Konstruktor für die spielrelevanten Information, um in einem Objekt die allgemeinen Spielrahmenbedingungen
     * festzulegen und festzuhalten.
     * 
     * @param fieldWidth
     *            Die Weite des Spielfeldes.
     * @param fieldLength
     *            Die Länge des Spielfeldes.
     * @param goalSize
     *            Die Länge des Tores.
     * @param playerDiameter
     *            Der Durchmesser eines Spielers.
     * @param ballDiameter
     *            Der Durchmesser eines Balles.
     * @param maximumTickNumber
     *            Die Höchstanzahl an ticks, die berechnet werden.
     */
    public SoSiGameInformation(double fieldWidth, double fieldLength, double goalSize, double playerDiameter,
            double ballDiameter, int maximumTickNumber) {
        this.fieldWidth = fieldWidth;
        this.fieldLength = fieldLength;
        this.goalSize = goalSize;
        this.playerDiameter = playerDiameter;
        this.ballDiameter = ballDiameter;
        this.maximumTickNumber = maximumTickNumber;
    }

    /**
     * Getter-Methode für die Spielfeldweite.
     * 
     * @return Die Weite des Spielfeldes.
     */
    public double getFieldWidth() {
        return this.fieldWidth;
    }

    /**
     * Getter-Methode für die Spielfeldlänge.
     * 
     * @return Die Länge des Spielfeldes.
     */
    public double getFieldLength() {
        return this.fieldLength;
    }

    /**
     * Getter-Methode für die Länge des Torbereichs.
     * 
     * @return Die Länge eines Tores.
     */
    public double getGoalSize() {
        return this.goalSize;
    }

    /**
     * Getter-Methode für den Durchmesser eines Spielers.
     * 
     * @return Durchmesser eines Spielers.
     */
    public double getPlayerDiameter() {
        return this.playerDiameter;
    }

    /**
     * Getter-Methode für den Durchmesser des Balles.
     * 
     * @return Durchmesser des Balles.
     */
    public double getBallDiameter() {
        return this.ballDiameter;
    }

    /**
     * Getter-Methode für die maximale Anzahl der Ticks, die berechnet werden/wurden.
     * 
     * @return Die maximale Anzahl der ticks, die berechnet werden sollen.
     */
    public int getMaximumTickNumber() {
        return this.maximumTickNumber;
    }

    @Override
    public double getCircleDiameter() {
        return PhysicsCorrectPositions.MINIMUM_DISTANCE_TO_FREE_KICK_POSITION;
    }
}
