package SoSi.Model.GamePhysics;

import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.SoSiTickInformation;
import SoSi.Model.Calculation.Team;
import SoSi.Model.Calculation.Vector2D;
import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GameObjects.PlayerGameObject;

/**
 * Diese Klasse ist zuständig für die Berechnung der nächsten movementDirection der Spieler in einem Tick.<br>
 * Mit Hilfe der Spieler-Attribute movementDirection und targetDirection, wird deren aktuelle movementDirection an ihre
 * targetDirection angenähert.<br>
 * Durch diese Annäherung bewegt sich der Spieler sehr flüssig fort.<br>
 * Im Falle einer Richtungsänderung, durch das Setzten einer neuen targetDirection, läuft der Spieler dadurch eine (von
 * der Geschwindigkeit abhängige) Kurve und ändert nicht schlagartig seine Richtung in Richtung der targetDirection.<br>
 * <b>Eine genauere Beschreibung, sowie die einzelnen Berechnungsschritte befinden sich in dem dazugehörigen Dokument im
 * Implementierungsbericht.</b>
 */
public class PhysicsCalculatePlayerDirections extends GamePhysic {

	/**
	 * {@link Team}-Instanz des Teams A.
	 */
	private Team teamA;

	/**
	 * {@link Team}-Instanz des Teams B.
	 */
	private Team teamB;

	/**
	 * {@link BallGameObject}-Instanz.
	 */
	private BallGameObject ball;

	/**
	 * Erzeugt die neue Physik-Klasse, die für das Berechnen der neuen movementDirection aller Spieler in einem Tick
	 * zuständig ist.
	 * 
	 * @param teamA
	 *            {@link Team}-Instanz des Teams A.
	 * @param teamB
	 *            {@link Team}-Instanz des Teams B.
	 * @param ball
	 *            {@link BallGameObject}-Instanz.
	 */
	public PhysicsCalculatePlayerDirections(Team teamA, Team teamB, BallGameObject ball) {
		this.teamA = teamA;
		this.teamB = teamB;
		this.ball = ball;
	}

	/**
	 * Berechnet die neue Richtung (nicht Länge!) der neuen movementDirection des Spieler.<br>
	 * Dabei wird die Konstante {@link PlayerGameObject#ROTATE_CONSTANT} verwendet, die zusammen mit der Geschwindigkeit
	 * der aktuellen movementDirection bestimmt, um welchen Winkel der Spieler sich maximal drehen kann, wenn er sich
	 * der targetDirection nähert. Der Spieler dreht sich (über mehrere Ticks gesehen) um diesen Winkel, bis seine
	 * Bewegungsrichtung mit der der targetDirection übereinstimmt.<br>
	 * <b>Eine genauere Beschreibung, sowie die einzelnen Berechnungsschritte befinden sich in dem dazugehörigen
	 * Dokument im Implementierungsbericht.</b>
	 * 
	 * @param player
	 *            Der Spieler, dessen neue Bewegungsrichtung berechnet werden soll.
	 * @return Die neue Bewegungsrichtung des Spielers.
	 */
	private static Vector2D calculateOnlyNewPlayerMovementDirection(PlayerGameObject player) {

		Vector2D newMovementDirection;

		// ist die Länge der targetDirection 0?
		if (player.getTargetDirection().getLength() == 0)
			newMovementDirection = player.getMovementDirection();
		else
		// ist die Länge der movementDirection 0?
		if (player.getMovementDirection().getLength() == 0)
			newMovementDirection = player.getTargetDirection();
		else {
			// berechne den Winkel, um den sich der Spieler maximal drehen kann
			double maxRotateAngle = Math.atan(GamePhysic.convertVelocity(PlayerGameObject.ROTATE_CONSTANT)
					/ player.getMovementDirection().getLength())
					* (180d / Math.PI);

			// berechne den Winkel, zwischen der aktuellen movementDirection und der targetDirection
			double movementTargetAngle = Vector2D.getSmallestAngleBetweenVectors(player.getMovementDirection(),
					player.getTargetDirection());

			// kann sich der Spieler gleich im aktuellen Tick zur targetDirection drehen?
			if (movementTargetAngle <= maxRotateAngle)
				newMovementDirection = player.getTargetDirection();
			else {

				// berechne die 2 Normalvektoren (mit der Länge: ROTATE_CONSTANT) der aktuellen movementDirection
				Vector2D v = Vector2D.addVectors(
						player.getMovementDirection(),
						player.getMovementDirection().getLeftSideNormalVector()
								.getNewLengthVector(GamePhysic.convertVelocity(PlayerGameObject.ROTATE_CONSTANT)));
				Vector2D w = Vector2D.addVectors(
						player.getMovementDirection(),
						player.getMovementDirection().getRightSideNormalVector()
								.getNewLengthVector(GamePhysic.convertVelocity(PlayerGameObject.ROTATE_CONSTANT)));

				// suche den Normalvektor mit dem kleinsten Winkel zur targetDirection
				if (Vector2D.getSmallestAngleBetweenVectors(v, player.getTargetDirection()) < Vector2D
						.getSmallestAngleBetweenVectors(w, player.getTargetDirection()))
					newMovementDirection = v;
				else
					newMovementDirection = w;
			}
		}

		return newMovementDirection;
	}

	/**
	 * Berechnet die neue Geschwindigkeit (=Länge) der neuen Spieler-Bewegungsrichtung. Dabei werden die Konstanten:
	 * {@link PlayerGameObject#MAX_ACCELERATION} und {@link PlayerGameObject#MAX_DECELERATION}. Diese dienen dazu, die
	 * Geschwindigkeit der movementDirection an die der targetDirection anzupassen.<br>
	 * Außerdem wird sichergestellt, dass die neu berechnete Geschwindigkeit der movementDirection den vorgegebenen
	 * Maximalwert nicht übersteigt.<br>
	 * <b>Eine genauere Beschreibung, sowie die einzelnen Berechnungsschritte befinden sich in dem dazugehörigen
	 * Dokument im Implementierungsbericht.</b>
	 * 
	 * @param player
	 *            Der Spieler, dessen Bewegungsgeschwindigkeit ermittelt werden soll.
	 * @param ball
	 *            Der Ball.
	 * @param newPlayerMovementDirection
	 *            Die neue Bewegungsrichtung des Spieler.
	 * @return Die Länge der neuen Bewegungsrichtung des Spielers.
	 */
	private static double calculateNewPlayerMovementDirectionLength(PlayerGameObject player, BallGameObject ball,
			Vector2D newPlayerMovementDirection) {

		double newMovementSpeed;

		// ist die Länge der neuen movementDirection 0?
		if (newPlayerMovementDirection.getLength() == 0)
			newMovementSpeed = 0;
		else
		// ist die Länge der targetDirection 0?
		if (player.getTargetDirection().getLength() == 0) {

			newMovementSpeed = player.getMovementDirection().getLength()
					- player.getConvertedAccelerationOrDeceleration(ball, false);

			// wurde nun zu viel abgezogen
			if (newMovementSpeed < 0)
				newMovementSpeed = 0;
		} else {
			// berechne den Winkel, der in den nächsten Ticks noch zu drehen ist
			double angleLeftToRotate = Vector2D.getSmallestAngleBetweenVectors(newPlayerMovementDirection,
					player.getTargetDirection());

			double preferredLength;

			// ist der Winkel innerhalb [0, 90]
			if ((0 <= angleLeftToRotate) && (angleLeftToRotate <= 90)) {

				preferredLength = player.getTargetDirection().getLength();

				// korrigiere auf die maximal erlaubte Geschwindigkeit der movementDirection
				if (preferredLength > player.getMaxSpeed(ball))
					preferredLength = player.getMaxSpeed(ball);

			} else
				preferredLength = 0;

			// ist die aktuelle Bewegungsgeschwindigkeit noch zu klein?
			if (player.getMovementDirection().getLength() < preferredLength) {
				newMovementSpeed = player.getMovementDirection().getLength()
						+ player.getConvertedAccelerationOrDeceleration(ball, true);

				// wurde nun zu viel addiert
				if (newMovementSpeed > preferredLength)
					newMovementSpeed = preferredLength;
			} else if (player.getMovementDirection().getLength() > preferredLength) {
				newMovementSpeed = player.getMovementDirection().getLength()
						- player.getConvertedAccelerationOrDeceleration(ball, false);

				// wurde nun zu viel abgezogen
				if (newMovementSpeed < preferredLength)
					newMovementSpeed = preferredLength;
			} else
				newMovementSpeed = preferredLength;
		}

		return newMovementSpeed;
	}

	/**
	 * Die Methode berechnet und setzt die neue movementDirection des übergebenen Spielers.<br>
	 * 
	 * @param player
	 *            Der Spieler, dessen movementDirection neu berechnet und gesetzt werden soll.
	 * @param ball
	 *            Der Ball.
	 */
	private static void calculatePlayerDirection(PlayerGameObject player, BallGameObject ball) {

		// berechne die Richtung der nächsten moving-Direction (aber noch nicht deren Länge/Geschwindigkeit)
		Vector2D newMovementDirection = PhysicsCalculatePlayerDirections
				.calculateOnlyNewPlayerMovementDirection(player);

		// berechne die Länge/Geschwindigkeit der nächsten moving-Direction
		double newMovementDirectionLength = PhysicsCalculatePlayerDirections.calculateNewPlayerMovementDirectionLength(
				player, ball, newMovementDirection);

		if (newMovementDirection.getLength() != 0)
			player.setMovementDirection(newMovementDirection.getNewLengthVector(newMovementDirectionLength));
		else
			player.setMovementDirection(newMovementDirection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void calculate(SoSiTickInformation tickInformationOfTeamA, TickEvent simulationState) {
		for (PlayerGameObject player : this.teamA.getPlayers()) {
			PhysicsCalculatePlayerDirections.calculatePlayerDirection(player, this.ball);
		}

		for (PlayerGameObject player : this.teamB.getPlayers()) {
			PhysicsCalculatePlayerDirections.calculatePlayerDirection(player, this.ball);
		}
	}

}
