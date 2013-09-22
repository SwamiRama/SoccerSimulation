package SoSi.Model.GameObjects;

import sep.football.Position;
import sep.football.TickInformation;
import SoSi.Model.Calculation.Vector2D;
import SoSi.Model.GamePhysics.GamePhysic;

/**
 * Beinhaltet allgemeine Daten eines Spielers, wie eine blockingTime, die maximale Beschleunigung, Geschwindigkeit und
 * Verzögerung, sowie eine targetDirection. Diese gibt die neue Richtung, die durch einen Richtungswechsel des Spielers
 * (festgelegt durch die KI) erreicht werden soll, an.
 */
public class PlayerGameObject extends MoveableRadialGameObject {

    /**
     * Die maximale Beschleunigung eines Spielers in Metern/Sekunde^2. Dieser Wert wird dem Bewegungsvektor
     * hinzuaddiert.
     */
    public static final double MAX_ACCELERATION_WITHOUT_BALL = 4;

    /**
     * Die maximale Beschleunigung eines Spielers in Metern/Sekunde^2, wenn er den Ball besitzt.
     */
    public static final double MAX_ACCELERATION_WITH_BALL = 3;

    /**
     * Die maximale Abbremsung in Metern/Sekunde^2, mit der ein Spieler seine Bewegung ohne Ball verlangsam kann.
     */
    public static final double MAX_DECELERATION_WITHOUT_BALL = 4;

    /**
     * Die maximale Abbremsung in Metern/Sekunde^2, mit der ein Spieler seine Bewegung mit Ball verlangsamen kann.s
     */
    public static final double MAX_DECELERATION_WITH_BALL = 3;

    /**
     * Die maximale Geschwindigkeit in Metern/Sekunde, die ein Spieler ohne Ball erreichen kann.
     */
    public static final double MAX_SPEED_WITHOUT_BALL = 8;

    /**
     * Die maximale Geschwindigkeit in Metern/Sekunde, die ein Spieler mit Ball erreichen kann.
     */
    public static final double MAX_SPEED_WITH_BALL = 6;

    /**
     * 
     */
    public static final double ROTATE_CONSTANT = 5;

    /**
     * Vektor, in welche der Spieler beschleunigt werden soll in Metern/Tick. <br>
     * Der Vektor gibt vor, welche Richtung (und Geschwindigkeit) der Spieler laut KI anstreben soll. Dabei wird im
     * laufe weiterer Ticks, die {@link MoveableRadialGameObject#movementDirection} der targetDirection angenähert. Dies
     * erfolgt jedoch nicht abprubt, so dass der Spieler bei einem Richtungswechsel abbremsen und/oder Kurven laufen
     * muss.
     */
    private Vector2D targetDirection;

    /**
     * Verbleibende Blocking- bzw. Sperrzeit in Anzahl Ticks
     * 
     * @see TickInformation#getMinimalBlockingTime(int)
     */
    private int blockTimeRemaining;

    /**
     * Erstellt ein Spielobjekt auf dem Feld und setzt es gleich.
     * 
     * @param position
     *            Die Position innerhalb des Spielfeldes.
     * @param diameter
     *            Der Durchmesser des Spielers.
     */
    public PlayerGameObject(Position position, double diameter) {
        super(position, diameter);

        this.targetDirection = new Vector2D(0, 0);
    }

    /**
     * Setzt die targetDirection in Metern/Tick, in welche der Spieler beschleunigt werden soll.
     * 
     * @param targetDirection
     *            Vektor, in welche der Spieler beschleunigt werden soll in Metern/Tick.
     * @throws IllegalArgumentException
     *             Die Exception, wenn targetDirection den Wert null besitzt.
     */
    public void setTargetDirection(Vector2D targetDirection) {

        if (targetDirection == null)
            throw new IllegalArgumentException("targetDirection is null.");

        this.targetDirection = targetDirection;
    }

    /**
     * Gibt die derzeitig gesetzte targetDirection in Metern/Tick, in welche der Spieler beschleunigt werden soll,
     * zurück.
     * 
     * @return Vektor, in welche der Spieler beschleunigt werden soll in Metern/Tick.
     */
    public Vector2D getTargetDirection() {
        return this.targetDirection;
    }

    /**
     * Gibt die maximal erlaubte Bewegungsgeschwindigkeit in Metern/Tick des Spielers zurück. Diese ist abhängig davon,
     * ob der Spieler zur Zeit im Ballbesitz ist oder nicht.
     * 
     * @param ball
     *            Der Ball, der den Wert des aktuellen. ballführenden Spielers besitzt.
     * @return Die maximal erlaubte Bewegungsgeschwindigleit des Spielers.
     */
    public double getMaxSpeed(BallGameObject ball) {
        if (ball.getBallPossession() == this)
            return GamePhysic.convertVelocity(PlayerGameObject.MAX_SPEED_WITH_BALL);
        else
            return GamePhysic.convertVelocity(PlayerGameObject.MAX_SPEED_WITHOUT_BALL);
    }

    /**
     * Gibt die maximal erlaubte Beschleunigung bzw. Verlangsamung in Metern/Tick² des Spielers zurück. Diese ist
     * abhängig davon, ob der Spieler zur Zeit im Ballbesitz ist oder nicht und ob dieser besch
     * 
     * @param ball
     *            Der Ball, der den Wert des aktuellen. ballführenden Spielers besitzt.
     * @return Die maximal erlaubte Bewegungsgeschwindigleit des Spielers.
     */
    public double getConvertedAccelerationOrDeceleration(BallGameObject ball, boolean useAcceleration) {
        if (ball.getBallPossession() == this) {
            return GamePhysic
                    .convertAccelerationOrDeceleration(useAcceleration ? PlayerGameObject.MAX_ACCELERATION_WITH_BALL
                            : PlayerGameObject.MAX_DECELERATION_WITH_BALL);
        } else {
            return GamePhysic
                    .convertAccelerationOrDeceleration(useAcceleration ? PlayerGameObject.MAX_ACCELERATION_WITHOUT_BALL
                            : PlayerGameObject.MAX_DECELERATION_WITHOUT_BALL);
        }
    }

    /**
     * Gibt die BlockTime des Spielobjekts zurück.
     * 
     * @return BlockTime des Spielobjekts
     */
    public int getBlockTimeReamining() {
        return blockTimeRemaining;
    }

    /**
     * Setzt die BlockTime für ein Spielobjekt.<br>
     * (Der interne Wert wird dabei um +1 gerechnet. Dadurch wird erreicht, dass die Anzahl der Ticks der KOMMENDEN
     * Ticks entspricht, in welcher der Spieler gesperrt ist.)<br>
     * Zum Dekrementieren muss daher die Funktion {@link #decrementBlockTimeRemaining()} verwendet werden oder diese
     * Methode mit (blockTime-1) aufgerufen werden.
     * 
     * @param blockTime
     *            BlockTime für das Spielobjekt
     */
    public void setBlockTimeRemaining(int blockTime) {
        this.blockTimeRemaining = blockTime + 1;
    }

    /**
     * Wird benutzt, um die Anzahl an Ticks herabzusetzen, die dieses Spielerobjekt geblockt wird.
     */
    public void decrementBlockTimeRemaining() {
        this.blockTimeRemaining = Math.max(0, this.blockTimeRemaining - 1);
    }

}
