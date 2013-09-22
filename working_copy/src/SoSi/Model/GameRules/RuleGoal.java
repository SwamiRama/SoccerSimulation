package SoSi.Model.GameRules;

import sep.football.GameInformation;
import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.SoSiTickInformation;
import SoSi.Model.Calculation.Team;
import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GameObjects.GoalGameObject;
import SoSi.Model.GameObjects.MoveableRadialGameObject;

/**
 * Die Klasse ist zuständig für die Tor-Regel.<br>
 * Diese löst ein Event aus, wenn ein Tor gefallen ist.<br>
 * Dann wird in der Methode {@link #checkRule(SoSiTickInformation)} die Anzahl der erzielten Tore des dazgehörigen Teams
 * erhöht und das entsprechende {@link #GOAL_SCORED}-Event zurückgegeben.
 */
public class RuleGoal extends GameRule {

    /**
     * Referenz auf das GoalGameObject der aktuellen Simulation.
     */
    public GoalGameObject goalLeft;

    /**
     * Referenz auf das GoalGameObject der aktuellen Simulation.
     */
    public GoalGameObject goalRight;

    private final boolean boundsActivated;

    /**
     * Multiplikator, um zu entscheiden, ob ein Tor gefallen ist, nachdem die Torlinie berührt (mit Bande) oder der Ball
     * die Torlinie vollständig überschritten hat (ohne Bande)
     */
    private final int multiplier;

    private double yGoalLeftPostRight;

    private double yGoalLeftPostLeft;

    private double yGoalRightPostRight;

    private double yGoalRightPostLeft;

    private double postRadius;

    /**
     * Erstellt die Tor-Regel.
     * 
     * @param gameInformation
     *            Die Informationen des Spiels.
     * @param ball
     *            Das Ball-Objekt.
     * @param teamA
     *            Das Team-Objekt des Teams A.
     * @param teamB
     *            Das Team-Objekt des Teams B.
     * @param goalLeft
     *            Das linke Tor
     * @param goalRight
     *            Das rechte Tor
     * @param boundsActivated
     *            Flag, ob die Banden aktiviert sind
     */
    public RuleGoal(GameInformation gameInformation, BallGameObject ball, Team teamA, Team teamB,
            GoalGameObject goalLeft, GoalGameObject goalRight, boolean boundsActivated) {
        super(gameInformation, ball, teamA, teamB);

        this.goalLeft = goalLeft;
        this.goalRight = goalRight;
        this.boundsActivated = boundsActivated;

        this.multiplier = (boundsActivated) ? 1 : -1;
        this.yGoalLeftPostRight = goalLeft.getPostRight().getPosition().getY();
        this.yGoalLeftPostLeft = goalLeft.getPostLeft().getPosition().getY();
        this.yGoalRightPostRight = goalRight.getPostRight().getPosition().getY();
        this.yGoalRightPostLeft = goalRight.getPostLeft().getPosition().getY();
        this.postRadius = goalLeft.getPostLeft().getDiameter() / 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TickEvent checkRule(SoSiTickInformation tickInformationOfTeamA) {

        boolean TeamALeft = tickInformationOfTeamA.isPlayingOnTheLeft();
        double xBall = ball.getPosition().getX();
        double yBall = ball.getPosition().getY();
        MoveableRadialGameObject checkObject;
        
        if (boundsActivated) {
            checkObject = (this.ball.getBallPossession() != null) ? this.ball.getBallPossession() : this.ball;
        } else {
            checkObject = this.ball;
        }

        double radius = checkObject.getDiameter() / 2d;

        // Überprüfung: Ball geht ins linke Tor
        if ((xBall - (multiplier * radius)) <= 0 && (yBall > yGoalLeftPostRight + postRadius)
                && (yBall < yGoalLeftPostLeft - postRadius)) {
            if (TeamALeft) {
                teamB.goalScored();
            } else {
                teamA.goalScored();
            }
            return TickEvent.GOAL_SCORED;
        }
        // gameInformation.getFieldLength() eventuell Umrechnung notwendig

        // Überprüfung: Ball geht ins rechte Tor
        if ((xBall + (multiplier * radius)) >= gameInformation.getFieldLength()
                && (ball.getPosition().getY() < yGoalRightPostRight - postRadius)
                && (ball.getPosition().getY() > yGoalRightPostLeft + postRadius)) {
            if (TeamALeft) {
                teamA.goalScored();
            } else {
                teamB.goalScored();
            }
            return TickEvent.GOAL_SCORED;
        }
        return null;
    }
}
