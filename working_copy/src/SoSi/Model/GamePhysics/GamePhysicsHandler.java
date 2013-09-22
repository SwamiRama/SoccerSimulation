package SoSi.Model.GamePhysics;

import java.util.LinkedList;
import java.util.List;

import sep.football.GameInformation;
import sep.football.Position;

import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GameObjects.GoalGameObject;
import SoSi.Model.SimulationOptions;
import SoSi.Model.SoSiPosition;
import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.SoSiTickInformation;
import SoSi.Model.Calculation.Team;

/**
 * Die Klasse verwaltet sämtliche physikalischen Berechnungen.<br>
 * Sie besitzt eine interne Liste mit aktiven {@link GamePhysic}s. Die Liste wird im Konstruktor mit den default-
 * {@link GamePhysic}s erstellt, die standardmäßig im Spiel immer aktiv sein sollen.<br>
 * Des Weiteren besteht die Möglichkeit, weitere Physik-Berechnungen im Konstruktor über das Enum
 * {@link SimulationOptions} zu aktivieren. Diese werden ebenfalls der Liste hinzugefügt.<br>
 * Die Reihenfolge, in der die {@link GamePhysic}s der Liste hinzugefügt werden, bestimmt auch die Reihenfolge deren
 * physikalischen Berechnung.<br>
 * Um die einzelnen Physik-Berechnungen aufzurufen, bekommt der {@link GamePhysicsHandler} alle dafür benötigten
 * Referenzen auf die Spiel-Objekte bzw Spiel-Informationen.
 */
public class GamePhysicsHandler {

	/**
	 * Der Wert, um die die outer-Box (= äußeres Rechteck um das das Spielfeld) an jeder Seite größer ist als das
	 * Spielfeld.
	 */
	private static final double OUTER_BOX_OFFSET = 2.5d;

	/**
	 * Beinhaltet Referenzen zu allen aktiven Physik-Berechnungsklassen.
	 */
	private List<GamePhysic> activeGamePhysics;

	/**
	 * Erzeugt einen neuen {@link GamePhysicsHandler}.<br>
	 * Dieser bekommt alle relevanten Informationen, wie Referenzen auf die Spiel-Objekte und Spiel-Information, die von
	 * den einzelnen {@link GamePhysik}-Klassen verwendet werden.
	 * 
	 * @param gameInformation
	 *            Die Spiel-Informaionen.
	 * @param teamA
	 *            {@link Team}-Instanz des Teams A.
	 * @param teamB
	 *            {@link Team}-Instanz des Teams B.
	 * @param ball
	 *            Das Simulationsobjekt Ball vom Typ {@link BallGameObject}
	 * @param goal1
	 *            Ein Simulationsobjekt Tor vom Typ {@link GoalGameObject}
	 * @param goal2
	 *            Ein Simulationsobjekt Tor vom Typ {@link GoalGameObject}
	 * @param simulationOptions
	 *            Die Liste aller aktivierten, zusätzlichen Simulations-Optionen.
	 */
	public GamePhysicsHandler(GameInformation gameInformation, Team teamA, Team teamB, BallGameObject ball,
			GoalGameObject goal1, GoalGameObject goal2, List<SimulationOptions> simulationOptions) {
		this.activeGamePhysics = new LinkedList<GamePhysic>();

		// Die Positionen des Spielfelds
		Position fieldTopLeftPosition = new SoSiPosition(0, 0);
		Position fieldBottomRightPosition = new SoSiPosition(gameInformation.getFieldLength(),
				gameInformation.getFieldWidth());

		// Die Positionen der outer-Box
		Position outerBoxTopLeftPosition = new SoSiPosition(fieldTopLeftPosition.getX()
				- GamePhysicsHandler.OUTER_BOX_OFFSET, fieldTopLeftPosition.getY()
				- GamePhysicsHandler.OUTER_BOX_OFFSET);
		Position outerBoxBottomRightPosition = new SoSiPosition(fieldBottomRightPosition.getX()
				+ GamePhysicsHandler.OUTER_BOX_OFFSET, fieldBottomRightPosition.getY()
				+ GamePhysicsHandler.OUTER_BOX_OFFSET);

		// add default game physics
		this.activeGamePhysics.add(new PhysicsCalculatePlayerDirections(teamA, teamB, ball));
		this.activeGamePhysics.add(new PhysicsApplyFriction(ball));
		this.activeGamePhysics.add(new PhysicsApplyMovementDirections(teamA, teamB, ball));
		this.activeGamePhysics.add(new PhysicsHandleCollisions(teamA, teamB, ball, goal1, goal2));
		if (!simulationOptions.contains(SimulationOptions.OFF_RULE)) {
			// verwendet die outer-Box als Spielbegrenzungs-Box
			this.activeGamePhysics.add(new PhysicsReboundOnBox(ball, fieldTopLeftPosition, fieldBottomRightPosition));
			this.activeGamePhysics.add(new PhysicsCorrectPositions(teamA, teamB, ball, goal1, goal2,
					fieldTopLeftPosition, fieldBottomRightPosition, fieldTopLeftPosition, fieldBottomRightPosition));
		} else {
			// verwende das Spielfeld als Spielbegrenzungs-Box
			this.activeGamePhysics.add(new PhysicsReboundOnBox(ball, outerBoxTopLeftPosition,
					outerBoxBottomRightPosition));
			this.activeGamePhysics.add(new PhysicsCorrectPositions(teamA, teamB, ball, goal1, goal2,
					fieldTopLeftPosition, fieldBottomRightPosition, outerBoxTopLeftPosition,
					outerBoxBottomRightPosition));
		}
	}

	/**
	 * Die Methode, die die physikalischen Berechnungen aller aktiven Physik-Klassen in der Reihenfolge aufruft, wie sie
	 * in der Liste gespeichert wurden.
	 * 
	 * @param simulationState
	 *            Der aktuelle Simulationszustand, in welchem sich die Simulation befindet. <br>
	 *            Die Physik reagiert darauf ggf. entsprechend, um z.B. einen Mindestabstand zum Ball bei einem
	 *            Freistoß, nicht jedoch während des normalen Spielgeschehens, einzuhalten.
	 */
	public void calculatePhysics(SoSiTickInformation tickInformationOfTeamA, TickEvent simulationState) {

		for (GamePhysic gamePhysic : this.activeGamePhysics) {
			gamePhysic.calculate(tickInformationOfTeamA, simulationState);
		}
	}
}
