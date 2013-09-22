package SoSi.Model.GameRules;

import sep.football.GameInformation;
import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.SoSiTickInformation;
import SoSi.Model.Calculation.Team;
import SoSi.Model.GameObjects.BallGameObject;

/**
 * Die Klasse beschreibt eine Regel-Klasse, die die Methode {@link #checkRule(SoSiTickInformation)} besitzt, in der die
 * Regelauswertung der jeweiligen Spielsituation erfolgt.<br>
 * Diese Klasse bildet einen Bauplan für die Definition der Regelimplementierung. Die Methode
 * {@link #checkRule(SoSiTickInformation)} ist in jeder geerbten Mehode enthalten, damit der {@link GameRulesHandler}
 * mit den Regelobjekten umgehen und sie anwenden kann. In der Methode {@link #checkRule(SoSiTickInformation)} wertet
 * die Regel die Spielsituation aus und liefert ein {@link TickEvent} zurück, falls in diesem Tick etwas vorgefallen ist
 * und <b>null</b>, falls die Regel nicht greift. Die Regel-Auswertung des Spiels werden dadurch komfortabel in
 * dazugehörige Regel-Klassen aufgeteilt.<br>
 * Diese werden im {@link GameRulesHandler} verwaltet und können ganz einfach aktiviert oder deaktiviert werden.
 */
public abstract class GameRule {

    /**
     * Eine Referenz auf ein SoSiGameInformation-Objekt, das einzelnen Regeln die Rahmenbedingungen der Simulation
     * mitteilt.
     */
    protected GameInformation gameInformation;

    /**
     * Eine Referenz auf das Ball-Objekt der Simulation.
     */
    protected BallGameObject ball;

    /**
     * Referenz auf das Team-Objekt des Teams A.
     */
    protected Team teamA;

    /**
     * Referenz auf das Team-Objekt des Teams B.
     */
    protected Team teamB;

    /**
     * Konstruktor für eine neue Spielregel.
     * 
     * @param gameInformation
     *            Die allgemeinen Spiel-Information.
     * @param teamA
     *            Team-Objekt des Teams A.
     * @param teamB
     *            Team-Objekt des Teams B.
     */
    public GameRule(GameInformation gameInformation, BallGameObject ball, Team teamA, Team teamB) {
        this.gameInformation = gameInformation;
        this.ball = ball;
        this.teamA = teamA;
        this.teamB = teamB;
    }

    /**
     * Die Methode, die von jeder geerbten Regel-Klasse implementiert werden muss.<br>
     * Darin befindet sich die regeltechnische Auswertung anhand der aktuellen Spielsituation (tickInformation).<br>
     * Die Methode prüft, ob die implementierte Regel nicht beachtet wurde.<br>
     * Ist dies der Fall, wird als nachfolgende Konsequenz ein entsprechendes {@link TickEvent} zurückgegeben (z.B.
     * FREE_KICK bei Abseits). Ansonsten wird <b>null</b> zurückgegeben.
     * 
     * @param tickInformation
     *            Die Tick-Informationen.
     * @return Das Event, das als Konsequenz eintritt, wenn gegen die Regel verstoßen wurde, ansonsten <b>null</b>.
     */
    public abstract TickEvent checkRule(SoSiTickInformation tickInformation);
}
