package SoSi.Model.GamePhysics;

import SoSi.Model.SoSiPosition;
import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.SoSiTickInformation;
import SoSi.Model.Calculation.Team;
import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GameObjects.MoveableRadialGameObject;
import SoSi.Model.GameObjects.PlayerGameObject;

/**
 * Diese Klasse ist zuständig für das Setzen der neuen Positionen der {@link MoveableRadialGameObject}s, wie die Spieler
 * und der Ball.<br>
 * Dabei wird die movementDirection jedes bewegbaren Spielobjekts als Vektor an die aktuelle Position angetragen.<br>
 * Die Position, die sich daraus ergibt, wird als neue Position gesetzt.
 */
public class PhysicsApplyMovementDirections extends GamePhysic {

	/**
	 * {@link Team}-Instanz des Teams A.
	 */
	private Team teamA;

	/**
	 * {@link Team}-Instanz des Teams B.
	 */
	private Team teamB;

	/**
	 * Das Simulationsobjekt Ball vom Typ {@link BallGameObject}
	 */
	private BallGameObject ball;

	/**
	 * Erzeugt die neue Physik-Klasse, die für das Setzen der neuen Positionen der Spieler eines Teams und des Balles
	 * zuständig ist.
	 * 
	 * @param teamA
	 *            {@link Team}-Instanz des Teams A.
	 * @param teamB
	 *            {@link Team}-Instanz des Teams B.
	 * @param ball
	 *            Das Simulationsobjekt Ball.
	 */
	public PhysicsApplyMovementDirections(Team teamA, Team teamB, BallGameObject ball) {
		this.teamA = teamA;
		this.teamB = teamB;
		this.ball = ball;
	}

	/**
	 * Wendet die Bewegungsrichtung an das Objekt an. Das Objekt erhält dadurch eine neue Position.
	 * 
	 * @param object
	 *            Das Objekt, dessen Bewegungsrichtung angewandt werden soll.
	 */
	private static void applyMovementDirection(MoveableRadialGameObject object) {
		object.setPosition((SoSiPosition) object.getMovementDirection().getAppliedPosition(object.getPosition()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void calculate(SoSiTickInformation tickInformationOfTeamA, TickEvent simulationState) {

		for (PlayerGameObject playerTeamA : this.teamA.getPlayers()) {
			PhysicsApplyMovementDirections.applyMovementDirection(playerTeamA);
		}
		for (PlayerGameObject playerTeamB : this.teamB.getPlayers()) {
			PhysicsApplyMovementDirections.applyMovementDirection(playerTeamB);
		}

		PhysicsApplyMovementDirections.applyMovementDirection(this.ball);
	}

}
