package SoSi.Model.Calculation;

import java.util.ArrayList;
import java.util.List;

import SoSi.Model.GameObjects.PlayerGameObject;

import sep.football.Position;
import sep.football.TickInformation;

/**
 * Teambezogene Klasse, die die jeweilige Team-KI mit Informationen über den aktuellen Tick der Simulation versorgt.
 */
public class SoSiTickInformation implements TickInformation {

    /**
     * Die aktuelle Position des Balles.
     */
    private final Position ballPosition;

    /**
     * Die aktuellen Positionen aller eigenen Spieler auf dem Feld.
     */
    private final Team ownTeam;

    /**
     * Die aktuellen Positionen aller gegnerischen Spieler auf dem Feld.
     */
    private final Team opponentTeam;

    /**
     * Gibt an, ob die Mannschaft, die die tickInformation bekommt, auf der linken Spielfeldseite spielt oder nicht.
     * 
     * @see #isPlayingOnTheLeft()
     */
    private final boolean isPlayingLeft;

    /**
     * Aktuelle TickPosition
     * 
     * @see #getCurrentTickNumber()
     */
    private final int currentTickNumber;

    /**
     * Referenz auf Spieler mit Ballbesitz
     */
    private final PlayerGameObject playerWithBall;

    /**
     * Konstruktor eines TickInformation-Objekts. Nimmt alle für die KI benötigten Informationen entgegen und bietet
     * diese der KI mittels Getter-Methoden an.
     * 
     * @param ballPosition
     *            Die Position des Balles.
     * @param ownTeam
     *            Referenz auf das eigene Team
     * @param opponentTeam
     *            Referenz auf das gegnerische Team
     * @param isPlayingLeft
     *            Flag, ob das eigene Team gerade auf der linken Spielfeldhälfte spielt
     * @param currentTickNumber
     *            Aktuelle Tick-Position
     * @param playerWithBall
     *            Referenz auf den Spieler, welcher in Ballbesitz ist
     */
    public SoSiTickInformation(Position ballPosition, Team ownTeam, Team opponentTeam, boolean isPlayingLeft,
            int currentTickNumber, PlayerGameObject playerWithBall) {
        this.ballPosition = ballPosition;
        this.ownTeam = ownTeam;
        this.opponentTeam = opponentTeam;
        this.isPlayingLeft = isPlayingLeft;
        this.currentTickNumber = currentTickNumber;
        this.playerWithBall = playerWithBall;
    }

    /**
     * Getter-Methode, um sich die aktuelle tick Positionen auszugeben, an dem sich das Spiel befindet.
     * 
     * @return Die Position der aktuellen tick Berechnung.
     */
    public int getCurrentTickNumber() {
        return this.currentTickNumber;
    }

    /**
     * Getter-Methode für die Positionen der eigenen (der KI zugehörigen) Spieler auf dem Feld.
     * 
     * @return Liste aller Position der Spieler des eigenen Teams.
     */
    public List<Position> getPlayerPositions() {
        ArrayList<Position> result = new ArrayList<Position>();

        for (PlayerGameObject player : this.ownTeam.getPlayers()) {
            result.add(player.getPosition());
        }

        return result;
    }

    /**
     * Getter-Methode für die Positionen der gegnerischen Spieler.
     * 
     * @return Liste aller Position der Spieler des gegnerischen Teams.
     */
    public List<Position> getOpponentPlayerPositions() {
        ArrayList<Position> result = new ArrayList<Position>();

        for (PlayerGameObject player : this.opponentTeam.getPlayers()) {
            result.add(player.getPosition());
        }

        return result;
    }

    /**
     * Getter-Methode für die Position des Balles.
     * 
     * @return Die Position des Balles.
     */
    public Position getBallPosition() {
        return this.ballPosition;
    }

    /**
     * Getter-Methode für die Anzahl der Tore, die die eigenen Mannschaft bis zum aktuellen Tick erzielt hat.
     * 
     * @return Die Anzahl der Tore der eigenen Mannschaft.
     */
    public int getTeamGoals() {
        return this.ownTeam.getGoalCount();
    }

    /**
     * Getter-Methode für die Anzahl der Tore, die die gegnerische Mannschaft bis zum aktuellen Tick erzielt hat.
     * 
     * @return Die Anzahl der Tore der gegnerischen Mannschaft.
     */
    public int getOpponentTeamGoals() {
        return this.opponentTeam.getGoalCount();
    }

    /**
     * Getter-Methode für die tick Dauer, die ein eigener Spieler geblockt (unfähig Befehle entgegen zu nehmen) ist.
     * 
     * @return Die Anzahl der Ticks, die ein Spieler geblockt ist.
     */
    public int getMinimalBlockingTime(int playerId) {
        if (playerId >= this.ownTeam.getPlayers().size())
            throw new ArrayIndexOutOfBoundsException(String.format("Die geforderte Spieler-ID %d ist nicht vorhanden!",
                    playerId));

        return this.ownTeam.getPlayers().get(playerId).getBlockTimeReamining();
    }

    /**
     * Stellt fest, ob die Mannschaft, welche eine Referenz auf diese Instanz besitzt, ihr Tor auf der linken Seite des
     * Spielfelds hat.
     * 
     * @return <b>True</b>, falls die eigenen Mannschaft auf der linken Spielfeldseite ihr Tor hat und <b>False</b>,
     *         falls die eigenen Mannschaft nicht auf der linken Spielfeldseite ihr Tor hat.
     */
    public boolean isPlayingOnTheLeft() {
        return this.isPlayingLeft;
    }

    @Override
    public boolean hasTeamBall(boolean ownTeam) {
        if (this.playerWithBall == null) {
            return false;
        } else {
            if (ownTeam)
                return this.ownTeam.getPlayers().contains(this.playerWithBall);
            else
                return this.opponentTeam.getPlayers().contains(this.playerWithBall);
        }
    }

    @Override
    public int getPlayerWithBall() throws IllegalStateException {
        if (this.playerWithBall == null) {
            throw new IllegalStateException("Kein Spieler ist im Ballbesitz");
        } else {
            int id = this.ownTeam.getPlayers().indexOf(playerWithBall);

            if (id < 0)
                id = this.opponentTeam.getPlayers().indexOf(playerWithBall);

            assert id >= 0 : "Spieler-ID mit Ballbesitz konnte nicht ermittelt werden";

            return id;
        }
    }
}
