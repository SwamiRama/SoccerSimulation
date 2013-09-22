package SoSi.Model.GameRules;

import sep.football.GameInformation;
import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.SoSiTickInformation;
import SoSi.Model.Calculation.Team;
import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GamePhysics.PhysicsReboundOnBox;

/**
 * Die Klasse ist zuständig für die "Aus"-Regel.<br>
 * Diese wird hier "verletzt", wenn der Ball das Spielfeld in vollem Umfang überschritten hat.<br>
 * Dann gibt es für die Mannschaft, die den Ball nicht zuletzt gespielt hat, einen Stoß von der jeweiligen Stelle am
 * Spielfeldrand (Rückgabetyp: {@link #FREE_KICK} der Methode {@link #checkRule(SoSiTickInformation)}).<br>
 * Ist die Regel aktiv, ist es nötig, die Physik-Klasse {@link PhysicsReboundOnBox} nicht zu akivieren.
 */
public class RuleOff extends GameRule {

    /**
     * Erstellt die neue "Aus"-Regel.
     * 
     * @param gameInformation
     *            Die Informationen des Spiels.
     * @param teamA
     *            {@link Team}-Instanz des Teams A.
     * @param teamB
     *            {@link Team}-Instanz des Teams B.
     * @param ball
     *            Das Simulationsobjekt Ball vom Typ {@link BallGameObject}
     */
    public RuleOff(GameInformation gameInformation, Team teamA, Team teamB, BallGameObject ball) {
        super(gameInformation, ball, teamA, teamB);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TickEvent checkRule(SoSiTickInformation tickInformation) {

        double xBall = ball.getPosition().getX();
        double yBall = ball.getPosition().getY();
        double ballRadius = ball.getDiameter() / 2;

        if (yBall + ballRadius < 0 || yBall - ballRadius > gameInformation.getFieldWidth() || xBall + ballRadius < 0
                || xBall - ballRadius > gameInformation.getFieldLength()) {
            return TickEvent.FOUL_OFF;
        }

        return null;
    }

}
