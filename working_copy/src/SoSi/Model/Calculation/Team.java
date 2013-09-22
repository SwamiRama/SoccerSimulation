package SoSi.Model.Calculation;

import java.util.ArrayList;
import java.util.List;

import sep.football.AI;
import sep.football.Position;
import SoSi.Model.GameObjects.PlayerGameObject;

/**
 * Beinhaltet allgemeine Daten eines Teams, wie eine Liste seiner Spieler (PlayerGameObjects), sowie eine KI um
 * Entscheidungen für diese zu treffen und die Summe der bereits erzielten Tore seiner Spieler, sowie Methoden, um den
 * Wert auszulesen oder den Torestand zu erhöhen, falls sich dieser geändert hat.
 */
public class Team {

    /**
     * Liste der Spieler einer KI.
     */
    private List<PlayerGameObject> players;

    /**
     * Die KI, die für das betreffende Team die Entscheidungen trifft.
     */
    private AI ai;

    /**
     * Die Anzahl der Tore, die ein Team erzielt hat.
     */
    private int goalCount = 0;

    /**
     * Konstruktor der Team Klasse.<br>
     * Ein Team benötigt die Anzahl seiner {@link PlayerGameObject Spieler}, sowie eine Referenz auf seine {@link AI KI}
     * .
     * 
     * @param playerCount
     *            Die Anzahl der Spieler im Team.
     * @param ai
     *            Eine Referenz auf die KI, welche dieses Team steuert.
     * @param playerDiameter
     *            Durchmesser des Spielers
     * @param defaultPosition
     *            Die Standardposition, an welcher die Spieler beim Erstellen gesetzt werden sollen
     */
    public Team(int playerCount, AI ai, double playerDiameter, Position defaultPosition) {
        this.players = new ArrayList<PlayerGameObject>();

        for (int i = 0; i < playerCount; i++) {
            this.players.add(new PlayerGameObject(defaultPosition, playerDiameter));
        }

        this.ai = ai;
    }

    /**
     * Getter-Methode, um die Liste aller Spieler eines Teams zu erhalten.
     * 
     * @return Liste aller Spieler.
     */
    public List<PlayerGameObject> getPlayers() {
        return this.players;
    }

    /**
     * Getter-Methode, um die KI zu erhalten, die die Entscheidungen für das betreffende Team trifft.
     * 
     * @return Die AI, die dieser Instanz der Team-Klasse zugeordnet ist.
     */
    public AI getAI() {
        return this.ai;
    }

    /**
     * Getter-Methode für die Anzahl der Tore des betreffenden Teams.
     * 
     * @return Die Anzahl der erzielten Tore.
     */
    public int getGoalCount() {
        return this.goalCount;
    }

    /**
     * Dient dazu, die Anzahl der erzielten Tore hochzuzählen. <br>
     * Bei einem Aufruf wird dabei die Anzahl der Tore um eins erhöht. Dadurch wird verhindert, dass bereits erzielte
     * Tore wieder rückgängig gemacht werden könnten.
     */
    public void goalScored() {
        goalCount++;
    }

}
