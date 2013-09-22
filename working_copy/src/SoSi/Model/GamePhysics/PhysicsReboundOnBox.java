package SoSi.Model.GamePhysics;

import sep.football.Position;
import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.SoSiTickInformation;
import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GameRules.RuleOff;

/**
 * Die Klasse ist zuständig für das Abprallen des Balles von der Bande, die das Spielfeld umfasst.<br>
 * Dabei wird der Ball gemäß physikalischer Gesetze (Einfallswinkel = Ausfallswinkel) entsprechend an der Bande
 * abgeprallt, wenn er sich über die Bande bewegt.<br>
 * Dabei verliert der Ball einen gewissen Betrag an Geschwindigkeit ({@link #BALL_REBOUND_DECELERATION}).<br>
 * Diese Physik-Klasse wird benötigt, falls die "Aus-Regel" {@link RuleOff} deaktiviert ist.
 */
public class PhysicsReboundOnBox extends GamePhysic {

	/**
	 * Prozent-Wert der aktuellen Geschwindigkeit, um den die Geschwindigkeit des Balles beim Aprallen von der Bande
	 * abnimmt.
	 */
	private static final double BALL_REBOUND_DECELERATION_PERCENT = 5d;

	/**
	 * Das Simulationsobjekt Ball vom Typ {@link BallGameObject}
	 */
	private BallGameObject ball;

	/**
	 * Die Position der linken, oberen Ecke der Box, von der der Ball abprallen soll.
	 */
	private Position topLeftBoxPosition;

	/**
	 * Die Position der rechten, unteren Ecke der Box, von der der Ball abprallen soll.
	 */
	private Position bottomRightBoxPosition;

	/**
	 * Erzeugt die neue Physik-Klasse, die für das Abprallen des Balles an der Bande zuständig ist.
	 * 
	 * @param ball
	 *            Das Simulationsobjekt Ball vom Typ {@link BallGameObject}
	 * @param topLeftBoxPosition
	 *            Die Position der linken, oberen Ecke der Box, von der der Ball abprallen soll.
	 * @param bottomRightPosition
	 *            Die Position der rechten, unteren Ecke der Box, von der der Ball abprallen soll.
	 */
	public PhysicsReboundOnBox(BallGameObject ball, Position topLeftBoxPosition, Position bottomRightPosition) {
		this.ball = ball;
		this.topLeftBoxPosition = topLeftBoxPosition;
		this.bottomRightBoxPosition = bottomRightPosition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void calculate(SoSiTickInformation tickInformationOfTeamA, TickEvent simulationState) {
		boolean boundCollision = false;

		BoundSides boundSides = GamePhysic.getObjectBoxCollisionState(this.ball, this.topLeftBoxPosition,
				this.bottomRightBoxPosition);

		// Abprallen vom linken Box-Rand
		if (boundSides.leftSide) {

			// zeigt die x-Koordinate der Bewegungsrichtung nach links -> Kollision
			if (this.ball.getMovementDirection().getX() < 0) {
				boundCollision = true;
				this.ball.setMovementDirection(this.ball.getMovementDirection().getHorizontalMirroredVector());
			}
		}

		// Abprallen vom rechten Box-Rand
		if (boundSides.rightSide) {

			// zeigt die x-Koordinate der Bewegungsrichtung nach rechts -> Kollision
			if (this.ball.getMovementDirection().getX() > 0) {
				boundCollision = true;
				this.ball.setMovementDirection(this.ball.getMovementDirection().getHorizontalMirroredVector());
			}
		}

		// Abprallen vom oberen Box-Rand
		if (boundSides.topSide) {

			// zeigt die y-Koordinate der Bewegungsrichtung nach oben -> Kollision
			if (this.ball.getMovementDirection().getY() < 0) {
				boundCollision = true;
				this.ball.setMovementDirection(this.ball.getMovementDirection().getVerticalMirroredVector());
			}
		}

		// Abprallen vom unteren Box-Rand
		if (boundSides.bottomSide) {

			// zeigt die y-Koordinate der Bewegungsrichtung nach unten -> Kollision
			if (this.ball.getMovementDirection().getY() > 0) {
				boundCollision = true;
				this.ball.setMovementDirection(this.ball.getMovementDirection().getVerticalMirroredVector());
			}
		}

		// verliere Geschwindigkeit im Falle einer Kollision
		if (boundCollision)
			this.ball.setMovementDirection(GamePhysic.looseMovementDirectionSpeedPercent(
					this.ball.getMovementDirection(), PhysicsReboundOnBox.BALL_REBOUND_DECELERATION_PERCENT));
	}
}
