package SoSi.Model.GameRules;

import java.util.ArrayList;
import java.util.List;

import sep.football.GameInformation;

import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.SoSiTickInformation;
import SoSi.Model.Calculation.Team;
import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GameObjects.PlayerGameObject;

/**
 * Die Klasse ist zuständig für die Abseits-Regel.<br>
 * Diese wird hier "verletzt", wenn ein Spieler, der näher an der Torlinie des gegnerischen Tors steht als alle
 * gegnerischen Spieler, einen Ball annimmt, dessen letzter Spielerkontakt mit einem Spieler der gleichen Mannschaft
 * war.<br>
 * Dann wird an der Position dieses Spielers ein Freistoß für die gegnerische Mannschaft gegeben (Rückgabetyp:
 * {@link #FREE_KICK} der Methode {@link #checkRule(SoSiTickInformation)}).<br>
 */
public class RuleOffside extends GameRule {

    private PlayerGameObject playerWithBallContactInLastTick = null;

    /**
     * Sammlung der Spieler, die im Abseits stehen zum Zeitpunkt eines Schusses.
     */
    private List<PlayerGameObject> offsidePlayers = new ArrayList<PlayerGameObject>();

    /**
     * Erstellt die neue Abseits-Regel.
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
    public RuleOffside(GameInformation gameInformation, Team teamA, Team teamB, BallGameObject ball) {
        super(gameInformation, ball, teamA, teamB);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TickEvent checkRule(SoSiTickInformation tickInformationOfTeamA) {
        TickEvent result = null;

        // Der Ball hat gerade einen Spieler verlassen bzw berührt
        if ((this.ball.getBallContact() == null) && (this.playerWithBallContactInLastTick != null)) {

            Team teamWithPlayerWithLastBallContact = (this.teamA.getPlayers()
                    .contains(this.playerWithBallContactInLastTick)) ? this.teamA : this.teamB;
            boolean isTeamWithPlayerWithLastBallContactPlayingOnTheLeft = this.isTeamPlayingOnTheLeft(
                    tickInformationOfTeamA, teamWithPlayerWithLastBallContact);
            Team enemyTeam = (this.teamA == teamWithPlayerWithLastBallContact) ? this.teamB : this.teamA;

            // Erstelle die Liste aller Spieler, die im selben Team sind als playerWithLastBallContact und die gerade im
            // Abseits stehen (besser: den Ball nicht berühren dürfen).
            this.offsidePlayers.clear();
            for (PlayerGameObject player : teamWithPlayerWithLastBallContact.getPlayers()) {
                if (this.playerWithBallContactInLastTick != player) {

                    PlayerGameObject nearestEnemyPlayerToEnemyGoal = this.getNearestPlayerToGoalSide(
                            tickInformationOfTeamA, enemyTeam);

                    if (isTeamWithPlayerWithLastBallContactPlayingOnTheLeft) {
                        if ((this.playerWithBallContactInLastTick.getPosition().getX() < player.getPosition().getX())
                                && (nearestEnemyPlayerToEnemyGoal.getPosition().getX() < player.getPosition().getX())
                                && ((this.gameInformation.getFieldLength() / 2d) < player.getPosition().getX()))
                            this.offsidePlayers.add(player);
                    } else {
                        if ((this.playerWithBallContactInLastTick.getPosition().getX() > player.getPosition().getX())
                                && (nearestEnemyPlayerToEnemyGoal.getPosition().getX() > player.getPosition().getX())
                                && ((this.gameInformation.getFieldLength() / 2d) > player.getPosition().getX()))
                            this.offsidePlayers.add(player);
                    }
                }
            }
        }

        // Prüfen, ob der Ball von einem Spieler berührt wurde, im Tick davor allerdings nicht
        if ((ball.getBallContact() != null) && (this.playerWithBallContactInLastTick == null)) {
            // Wenn der Spieler mit der Ballberührung in der Liste der Abseits-Spielern ist -> Abseits-Event
            if (this.offsidePlayers.contains(ball.getBallContact())) {
                result = TickEvent.FOUL_OFFSIDE;
            }
        }

        this.playerWithBallContactInLastTick = ball.getBallContact();

        return result;
    }

    /**
     * Gibt an, ob das Team auf der linken Spielfeldseite spielt
     * 
     * @param tickInformationOfTeamA
     *            TickInformation Referenz
     * @param team
     *            Team, zu welchem ermittelt werden soll, ob es auf der linken Spielfeldseite spielt
     * @return <code>true</code>, falls Parameter team auf der linken Spielfeldseite spielt, ansonsten
     *         <code>false</code>
     */
    private boolean isTeamPlayingOnTheLeft(SoSiTickInformation tickInformationOfTeamA, Team team) {
        return (((team == this.teamA) && (tickInformationOfTeamA.isPlayingOnTheLeft())) || ((team == this.teamB) && (!tickInformationOfTeamA
                .isPlayingOnTheLeft())));
    }

    private PlayerGameObject getNearestPlayerToGoalSide(SoSiTickInformation tickInformationOfTeamA, Team team) {

        boolean isTeamPlayingOnTheLeft = this.isTeamPlayingOnTheLeft(tickInformationOfTeamA, team);

        PlayerGameObject nearestPlayerToGoalSide = null;
        double minPlayerDistanceToGoalSide = Double.MAX_VALUE;

        for (PlayerGameObject player : team.getPlayers()) {
            double distanceToGoalSide;
            if (isTeamPlayingOnTheLeft)
                distanceToGoalSide = Math.abs(player.getPosition().getX());
            else
                distanceToGoalSide = Math.abs(this.gameInformation.getFieldLength() - player.getPosition().getX());

            if (distanceToGoalSide < minPlayerDistanceToGoalSide) {
                minPlayerDistanceToGoalSide = distanceToGoalSide;
                nearestPlayerToGoalSide = player;
            }
        }

        return nearestPlayerToGoalSide;
    }

}
