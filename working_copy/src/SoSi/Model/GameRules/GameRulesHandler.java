package SoSi.Model.GameRules;

import java.util.LinkedList;
import java.util.List;

import sep.football.GameInformation;

import SoSi.Model.SimulationOptions;
import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.SoSiTickInformation;
import SoSi.Model.Calculation.Team;
import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GameObjects.GoalGameObject;

/**
 * Die Klasse verwaltet sämtliche regeltechnischen Auswertungen des Spiels.<br>
 * Sie besitzt eine interne Liste mit aktiven {@link GameRule}s. Die Liste wird im Konstruktor mit den default-
 * {@link GameRule}s erstellt, die standardmäßig im Spiel immer ausgewertet werden sollen.<br>
 * Desweiteren besteht die Möglichkeit, weitere Spielregeln im Konstruktor über das Enum {@link SimulationOptions} zu
 * aktivieren. Diese werden ebenfalls der Liste hinzugefügt.<br>
 * Die Reihenfolge, in der die {@link GameRule}s der Liste hinzugefügt werden, bestimmt auch die Reihenfolge, in der die
 * Regeln ausgewertet werden.<br>
 * Um die einzelnen Regeln auswerten zu können, bekommt der {@link GameRulesHandler} alle dafür benötigten Referenzen
 * auf die Spiel-Objekte bzw Spiel-Informationen.
 */
public class GameRulesHandler {

    /**
     * Beinhaltet alle aktiven Spielregeln.
     */
    List<GameRule> activeGameRules;

    /**
     * Erzeugt einen neuen {@link GameRulesHandler}.<br>
     * Dieser bekommt alle relevanten Informationen, wie Referenzen auf die Spiel-Objekte und Spiel-Information, die von
     * den einzelnen {@link GameRule}-Klassen verwendet werden.
     * 
     * @param gameInformation
     *            Die Informationen des Spiels.
     * @param teamA
     *            {@link Team}-Instanz des Teams A.
     * @param teamB
     *            {@link Team}-Instanz des Teams B.
     * @param ball
     *            Das Simulationsobjekt Ball vom Typ {@link BallGameObject}
     * @param simulationOptions
     *            Die Liste aller aktivierten, zusätzlichen Simulationsoptionen.
     * @param goalLeft
     *            Referenz auf das linke Tor
     * @param goalRight
     *            Referenz auf das rechte Tor
     */
    public GameRulesHandler(GameInformation gameInformation, Team teamA, Team teamB, BallGameObject ball,
            List<SimulationOptions> simulationOptions, GoalGameObject goalLeft, GoalGameObject goalRight) {
        this.activeGameRules = new LinkedList<GameRule>();

        // add default game rules
        this.activeGameRules.add(new RuleGoal(gameInformation, ball, teamA, teamB, goalLeft, goalRight,
                !simulationOptions.contains(SimulationOptions.OFF_RULE)));

        // add optional game rules
        if (simulationOptions.contains(SimulationOptions.OFF_RULE)) {
            this.activeGameRules.add(new RuleOff(gameInformation, teamA, teamB, ball));
        }
        if (simulationOptions.contains(SimulationOptions.OFFSIDE_RULE)) {
            this.activeGameRules.add(new RuleOffside(gameInformation, teamA, teamB, ball));
        }
    }

    /**
     * Die Methode, die die regeltechnischen Auswertungen aller aktiven Regel-Klassen in der Reihenfolge aufruft, wie
     * sie in der Liste gespeichert wurden.<br>
     * Wird eine Regel davon nicht eingehalten, wird als Reaktion darauf, das passende Event (z.B. FREE_KICK) der
     * Regel-Klasse zurückgegeben, ansonsten null.
     * 
     * @param tickInformation
     *            Die Informationen eines Ticks.
     * @return Das Event als Reaktion einer nicht eingehaltenen Regel. Werden alle aktiven Regeln eingehalten, wird
     *         <b>null</b> zurückgegeben. Falls mehrere TickEvents eintreten würden, wird nach dem schwerwiegendsten
     *         gesucht und dieses dann ausgegeben.
     */
    public TickEvent checkIfActiveRulesNotComplied(SoSiTickInformation tickInformation) {
        TickEvent resultEvent = null;

        // Alle Regeln durchgehen und überprüfen
        for (int i = 0; i < this.activeGameRules.size(); ++i) {
            TickEvent event = this.activeGameRules.get(i).checkRule(tickInformation);

            // Das schwerwiegendste Event ermitteln
            if (resultEvent == null || (event != null && resultEvent.compareTo(event) < 0))
                resultEvent = event;
        }

        return resultEvent;
    }
}
