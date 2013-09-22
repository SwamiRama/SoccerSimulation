package SoSi.Model.Calculation;

import java.util.List;

import SoSi.Debugging.DebugManager;
import SoSi.Model.SoSiPosition;
import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GameObjects.PlayerGameObject;
import SoSi.Model.GameRules.GameRulesHandler;
import sep.football.KickActionHandler;
import sep.football.Position;

/**
 * Beinhaltet Spielaktionen, die jeder KI in den Ticks zur Verfügung stehen, in denen das Spiel unterbrochen ist, wie
 * z.B. die Anzahl der eigenen/gegnerischen Tore, die Ballposition und die verbleibende Blockdauer der Spieler.
 */
public class SoSiKickActionHandler extends SoSiActionHandler implements KickActionHandler {

    /**
     * Konstruktor für die SoSiKickActionHandler Klasse.<br>
     * Eine SoSiKickActionHandler braucht eine Referenz auf die Spieler, für welche eine Aktion gelten soll, sowie eine
     * Referenz auf den Spielball.
     * 
     * @param player
     *            Eine Referenz auf die Spieler.
     * @param ball
     *            Eine Referenz auf den Spielball.
     * @param debugManager
     *            Eine Referenz auf den DebugManager für das aktuelle Team
     */
    public SoSiKickActionHandler(List<PlayerGameObject> player, BallGameObject ball, DebugManager debugManager) {
        super(player, ball, debugManager);
    }

    /**
     * <p>
     * Setzt den Spieler auf eine vorgegebene Position auf dem Spielfeld.
     * </p>
     * 
     * <p>
     * <dl>
     * <dt>Während des Kick-Offs:</dt>
     * <dd>Die Spieler können auf dem ganzen Feld platziert werden mit Ausnahme eines Kreies mit einem
     * {@link GameRulesHandler#FREEKICK_MINIMUM_DISTANCE gegebenen Durchmesser}, der sich um den Ball herum befindet.
     * Ein Spieler des Teams, das den Kick-Off durchführt, muss auf die Position des Balles gesetzt werden.</dd>
     * 
     * <dt>Während eines Freistoßes:</dt>
     * <dd>Die Spieler können auf der Seite des Feldes ihres Teams platziert werden. Nur ein Spieler darf sich innerhalb
     * des Kreises mit einem {@link GameRulesHandler#FREEKICK_MINIMUM_DISTANCE gegebenen Durchmesser} um den Ball
     * aufhalten. Dieser Spieler, der den Freistoß ausführt, muss auf die Position des Balles gesetzt werden.</dd>
     * </dl>
     * </p>
     * 
     * @param playerId
     *            Die Nummer des Spieler im Team der KI.
     * @param pos
     *            Die Position, auf die der Spieler gesetzt werden soll.
     */
    public void placePlayer(int playerId, Position pos) {
        if (!this.actionCompleted) {
            if (playerId >= this.players.size())
                throw new ArrayIndexOutOfBoundsException();

            PlayerGameObject playerToMove = this.players.get(playerId);

            // Nicht zulassen, falls Spieler in Ballbesitz
            if (playerToMove != this.originalPlayerWithBall) {
                // Falls Spieler auf Ballposition gesetzt werden soll, diese Position abändern
                if (pos.getX() == ball.getPosition().getX() && pos.getY() == ball.getPosition().getY()) {

                    // Aufgrund offenem Bug in CorrectPositions, wurde folgender Codeausschnitt auskommentiert.
                    // Dadurch wird erreicht, dass die Spielerpositionierung wieder fest voraussagbar ist und nicht vom
                    // Zufall abhängt, womit das Debugging erleichtert wird. (Des weiteren hat die Einstellung keinen
                    // großen Einfluss auf die Simulation)

                    // double moveX = Math.random() - 0.5d;
                    // double moveY = Math.random() - 0.5d;
                    // if (moveX == 0 && moveY == 0)
                    // moveX = 0.1;

                    double moveX = 0.01d;
                    double moveY = 0.01d;

                    pos = new SoSiPosition(pos.getX() + moveX, pos.getY() + moveY);
                }

                playerToMove.setPosition(pos);
            }
        } else {
            this.debugManager.print(String.format(COMPLETED_ACTION_ERROR_MSG, "placePlayer"));
        }
    }
}
