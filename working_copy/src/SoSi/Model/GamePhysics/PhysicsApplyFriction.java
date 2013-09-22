package SoSi.Model.GamePhysics;

import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.SoSiTickInformation;
import SoSi.Model.GameObjects.BallGameObject;

/**
 * Diese Klasse ist zuständig für das Berechnen des Geschwindigkeitsverlustes, der sich aufgrund der Reibung ergibt.<br>
 * Dabei wird die Länge der movementDirection des betroffenen Spiel-Objekts (hier: der Ball) um einen definierten
 * Geschwindigkeitsverlust ({@link #BALL_FRICTION}) verkürzt.
 */
public class PhysicsApplyFriction extends GamePhysic {

    /**
     * Das Simulationsobjekt Ball vom Typ {@link BallGameObject}
     */
    private BallGameObject ball;

    /**
     * Erzeugt die neue Physik-Klasse, die für die Berechnung der Reibung der Spiel-Objekte (hier: der Ball) zuständig
     * ist.
     * 
     * @param ball
     *            Das Simulationsobjekt Ball.
     * @see BallGameObject
     */
    public PhysicsApplyFriction(BallGameObject ball) {
        this.ball = ball;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void calculate(SoSiTickInformation tickInformationOfTeamA, TickEvent simulationState) {
        this.ball.setMovementDirection(GamePhysic.looseMovementDirectionSpeedValue(this.ball.getMovementDirection(),
                GamePhysic.convertAccelerationOrDeceleration(BallGameObject.FRICTION)));
    }

}
