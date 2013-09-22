package SoSi.Model;

import java.util.List;

import javax.activation.DataHandler;

import SoSi.Model.Calculation.Tick;

import sep.football.Position;

/**
 * Dient dazu, alle zur Wiedergabe relevanten Informationen eines bestimmten Ticks zu speichern. In dieser Klasse werden
 * die Positionen der Spieler und des Balles, die Anzahl der Tore für jedes Team und ein gegebenenfalls eingetretenes
 * TickEvent festgehalten. Ein TickData Objekt wird für jeden einzelnen Tick erstellt. Dies geschieht in der Klasse
 * {@link Tick}. Der {@link DataHandler} speichert eine Liste von TickData-Objekten, die eine Simulation repräsentieren.
 */
public class TickData{

    /**
     * Liste der Positionen der Spieler des ersten Teams, das ausgewählt wurde zu einem bestimmten Tick.
     */
    private List<Position> playerPositionsTeamA;

    /**
     * Liste der Positionen der Spieler des zweiten Teams, das ausgewählt wurde zu einem bestimmten Tick.
     */
    private List<Position> playerPositionsTeamB;

    /**
     * Die Position des Balles zu einem bestimmten Tick.
     */
    private Position ballPosition;

    /**
     * Die Anzahl der Tore des zuerst gewählten Teams.
     */
    private int goalsTeamA;

    /**
     * Die Anzahl der Tore des zweitgewählten Teams.
     */
    private int goalsTeamB;

    /**
     * Das eventuelle Ereignis, das während des Ticks eingetreten ist.
     */
    private TickEvent tickEvent;

    /**
     * Die Stelle innerhalb der Liste, an der die Tick-Daten gespeichert wurden.
     */
    private int tickPosition;

    /**
     * Die erstellte Debugging-Nachricht des Teams A.
     */
    private String debugMessageTeamA;

    /**
     * Die erstellte Debugging-Nachricht des Teams B.
     */
    private String debugMessageTeamB;

    /**
     * Konstruktor der Klasse TickData. Hier werden alle Daten zusammengefasst, die für die Darstellung eines Ticks
     * notwendig sind. Diese werden in der Klasse {@link Tick} erstellt.
     * 
     * @param tickPosition
     *            Die Position innerhalb der Liste aller berechneten Tick-Daten.
     * @param goalsTeamA
     *            Die Anzahl der Tore des erstgewählten Teams.
     * @param goalsTeamB
     *            Die Anzahl der Tore des zweitgewählten Teams.
     * @param ballPosition
     *            Die Position des Balles zum Tick an der Stelle {@link tickPostion} innerhalb der Liste aller
     *            berechneten Tick-Daten.
     * @param playerPositionsTeamA
     *            Die Positionen der Spieler des erstgewählten Teams zum Tick an der Stelle {@link tickPostion}
     *            innerhalb der Liste.
     * @param playerPositionsTeamB
     *            Die Positionen der Spieler des zweitgewählten Teams zum Tick an der Stelle {@link tickPostion}
     *            innerhalb der Liste.
     * @param tickEvent
     *            Ein eventuell eingetretenes Event(FOUL, FREE_KICK etc.) zum Tick an der Stelle {@link tickPostion}
     *            innerhalb der Liste aller berechneten Tick-Daten.
     * @param debugMessageTeamA
     *            Die debugging Nachricht des zuerst gewählten Teams.
     * @param debugMessageTeamB
     *            Die debugging Nachricht des zweitgewählten Teams.
     */
    public TickData(int tickPosition, int goalsTeamA, int goalsTeamB, Position ballPosition,
            List<Position> playerPositionsTeamA, List<Position> playerPositionsTeamB, TickEvent tickEvent,
            String debugMessageTeamA, String debugMessageTeamB) {
        if (tickPosition < 0 || goalsTeamA < 0 || goalsTeamB < 0) {
            throw new IllegalArgumentException("Int-Values must be positive");
        }
        
        if (debugMessageTeamA == null || debugMessageTeamB == null) {
            throw new IllegalArgumentException("Debugmessages must not be null");
        }
        
        if (playerPositionsTeamA == null || playerPositionsTeamB == null) {
            throw new IllegalArgumentException("PlayPositions must not be null");
        }
        
        this.tickPosition = tickPosition;
        this.goalsTeamA = goalsTeamA;
        this.goalsTeamB = goalsTeamB;
        this.ballPosition = ballPosition;
        this.playerPositionsTeamA = playerPositionsTeamA;
        this.playerPositionsTeamB = playerPositionsTeamB;
        this.tickEvent = tickEvent;
        this.debugMessageTeamA = debugMessageTeamA;
        this.debugMessageTeamB = debugMessageTeamB;
    }

    /**
     * Getter-Methode für die Position der Spieler des zuerst ausgewählten Teams zu einem diesem Tick.
     * 
     * @return Liste mit allen Positionen der Spieler des zuerst gewählten Teams.
     */
    public List<Position> getPlayerPositionsTeamA() {
        return playerPositionsTeamA;
    }

    /**
     * Getter-Methode für die Position der Spieler des zweitgewählten Teams zu einem diesem Tick.
     * 
     * @return Liste mit allen Positionen der Spieler des zweitgewählten Teams.
     */
    public List<Position> getPlayerPositionsTeamB() {
        return playerPositionsTeamB;
    }

    /**
     * Getter-Methode für die Position des Balles zu einem diesem Tick.
     * 
     * @return Die Postion des Balles.
     */
    public Position getBallPosition() {
        return ballPosition;
    }

    /**
     * Getter-Methode, um sich die Anzahl der Tore des erstgewählten Teams zu holen.
     * 
     * @return Anzahl der Tore des erstgewählten Teams.
     */
    public int getGoalsTeamA() {
        return goalsTeamA;
    }

    /**
     * Getter-Methode, um sich die Anzahl der Tore des zweitgewählten Teams zu holen.
     * 
     * @return Anzahl der Tore des zweitgewählten Teams.
     */
    public int getGoalsTeamB() {
        return goalsTeamB;
    }

    /**
     * Getter-Methode für eventuell aufgetretene Tick-Events. Falls nichts passsiert wird null zurückgegeben.
     * 
     * @return Null oder das aufgetretene Tick-Event.
     */
    public TickEvent getTickEvent() {
        return tickEvent;
    }

    /**
     * Getter-Methode für die Position, an denen sich die übrigen Tick-Daten in der Liste aller Tick-Daten befinden.
     * 
     * @return Position innerhalb der Liste aller Tick-Daten.
     */
    public int getTickPosition() {
        return this.tickPosition;
    }

    /**
     * Getter-Methode für die debugging Nachricht des zuerst gewählten Teams.
     * 
     * @return debugging Nachricht des ersten Teams.
     */
    public String getDebugMessageTeamA() {
        return debugMessageTeamA;
    }

    /**
     * Getter-Methode für die debugging Nachricht des zweitgewählten Teams.
     * 
     * @return debugging Nachricht des zweiten Teams.
     */
    public String getDebugMessageTeamB() {
        return debugMessageTeamB;
    }

}
