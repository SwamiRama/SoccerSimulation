package SoSi.Model.Calculation;

import java.util.List;

import sep.football.ActionHandler;
import sep.football.Position;
import sep.football.TickInformation;

import SoSi.Debugging.DebugManager;
import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GameObjects.PlayerGameObject;
import SoSi.Model.GamePhysics.GamePhysic;

/**
 * Beinhaltet Spielaktionen, die einer KI während der Simulation zur Verfügung stehen.
 */
public abstract class SoSiActionHandler implements ActionHandler {

    /**
     * Liste aller Spieler eines Teams, je nach dem von welcher KI der ActionHandler aufgerufen wird.
     */
    protected List<PlayerGameObject> players;

    /**
     * Eine Referenz auf den Ball der aktuellen Simulation. Der ActionHandler kann einen bestimmten Wert innerhalb des
     * Ballobjekts ändern.
     */
    protected final BallGameObject ball;

    /**
     * Boolean-flag, ob der ActionHandler bereits verwendet worden/ausgeführt worden ist.
     */
    protected boolean actionCompleted = false;

    /**
     * Referenz auf Spieler, welcher zu Beginn der Action in Ballbesitz war. (Dies kann sich z.B. durch acquireBall oder
     * kickBall ändern)
     */
    protected final PlayerGameObject originalPlayerWithBall;

    /**
     * Blockingtime nach einem Kick
     */

    protected static final int BLOCK_TIME_AFTER_KICK = 5;

    /**
     * Fehlermeldung, die ausgegeben wird, falls die KI versucht den selben ActionHandler zu verwenden.
     */
    protected static final String COMPLETED_ACTION_ERROR_MSG = "Fehler: Ein ActionHandler eines vergangenen Ticks "
            + "darf nicht erneut verwendet werden, Aktion %s ungültig!";

    /**
     * Debugmessage, falls ein geblockter Spieler eine Aktion ausführen soll.
     */

    /**
     * Fehlermeldung, die ausgegeben wird, falls die KI versucht diesen ActionHandler auf einen geblockten Spieler
     * anzuwenden.
     */
    protected static final String PLAYER_BLOCKED_ERROR_MSG = "Warnung: Der Spieler %d ist noch geblockt, "
            + "Aktion %s ungültig!";

    /**
     * Referenz auf {@link DebugManager}, um in der Entwicklerkonsole über nicht erlaubte Aktionen zu informieren.
     */
    protected final DebugManager debugManager;

    /**
     * Konstruktor für die SoSiActionHandler Klasse.<br>
     * Eine SoSiActionHandler braucht eine Referenz auf die Spieler, für welche eine Aktion gelten soll, sowie eine
     * Referenz auf den Spielball.
     * 
     * @param player
     *            Eine Referenz auf die Spieler.
     * @param ball
     *            Eine Referenz auf den Spielball.
     * @param debugManager
     *            Eine Referenz auf den DebugManager für das aktuelle Team
     */
    public SoSiActionHandler(List<PlayerGameObject> player, BallGameObject ball, DebugManager debugManager) {
        this.players = player;
        this.ball = ball;
        this.originalPlayerWithBall = this.ball.getBallPossession();

        this.debugManager = debugManager;
    }

    /**
     * <p>
     * Ein Spieler wird angewiesen einen Schuss in eine bestimmte Richtung mit einer bestimmten Stärke durchzuführen.
     * </p>
     * 
     * <p>
     * Diese Action ist nur erfolgreich, wenn sich der Spieler nahe am Ball befindet. Diverse weitere Faktoren
     * beeinflussen den Erfolg des Schusses:
     * <ul>
     * <li>Die Geschwindigkeit des schießenden Spielers. Ein Schuss ist genauer, je langsamer der Spieler ist.</li>
     * 
     * <li>Die Stärke des Schusses. Ein Schuss mit wenig Kraft is genauer, als ein Schuss mit viel Kraft.</li>
     * 
     * <li>Die Richtung des Schusses. Je ähnlicher die Schussrichtung und die Laufrichtung des Spielers, desto genauer
     * wird der Schuss.</li>
     * </ul>
     * </p>
     * 
     * @param playerId
     *            Die Nummer des Spieler im Team der KI.
     * @param direction
     *            Die Richtung des Schusses abhängig von der Position des Spielers.
     * @param strength
     *            Die Stärke des Schusses. Muss einen Wert zwischen ]0;1] haben, wobei 1 die maximale Schussstärke eines
     *            Spielers ist und der Wert 0 würde einen Schuss ohne Kraft darstellen.
     */
    public void kickBall(int playerId, Position direction, double strength) {
        if (!actionCompleted) {
            if (playerId >= this.players.size()) {
                throw new ArrayIndexOutOfBoundsException(String.format("Spieler-ID %d existiert nicht!", playerId));
            }
            PlayerGameObject player = players.get(playerId);
            if (player.getBlockTimeReamining() <= 0 && strength > 0) {
                if (originalPlayerWithBall == player) {
                    Vector2D desiredBallMovement = new Vector2D(direction.getX(), direction.getY());

                    if (strength > 1.0d) {
                        strength = 1.0d;
                    }

                    double delta;
                    if (desiredBallMovement.getLength() == 0
                            || players.get(playerId).getMovementDirection().getLength() == 0) {
                        delta = 0;
                    } else {
                        delta = Vector2D.getSmallestAngleBetweenVectors(desiredBallMovement, players.get(playerId)
                                .getMovementDirection());
                    }

                    double playerMaxSpeed = GamePhysic.convertVelocity(PlayerGameObject.MAX_SPEED_WITH_BALL);
                    double currentPlayerSpeed = player.getMovementDirection().getLength();

                    double beta = strength * ((currentPlayerSpeed + 0.5 * playerMaxSpeed) / (1.5 * playerMaxSpeed))
                            * ((delta + 90) / 3d);

                    double newRandomBallAngle = (-Math.abs(beta)) + (Math.random() * 2 * Math.abs(beta));

                    Vector2D newBallMovement = new Vector2D(0, 0);

                    // Berechnung des Schussvektors mittels des berechneten Streuungswinkel
                    newBallMovement = desiredBallMovement.getRotatedVector(newRandomBallAngle, true);

                    // testcode only: (stets exakte schüsse) [in finaler Abgabe unbedingt auskommentiert lassen ;) ]
                    // newBallMovement = desiredBallMovement;

                    // Betrag der Schussstärke setzen
                    if (newBallMovement.getLength() > 0) {
                        newBallMovement = newBallMovement.getNewLengthVector(strength
                                * GamePhysic.convertVelocity(BallGameObject.MAX_SHOOTING_SPEED));
                    }

                    // Ballgeschwindigkeit und Ballbesitz aktualisieren
                    ball.setMovementDirection(newBallMovement);
                    ball.setBallPossession(null);

                    // Ballposition von Spieler weg bewegen
                    if (newBallMovement.getLength() > 0) {
                        Vector2D newBallPosition = new Vector2D(ball.getPosition().getX(), ball.getPosition().getY());
                        newBallPosition = Vector2D.addVectors(newBallPosition,
                                newBallMovement.getNewLengthVector((player.getDiameter() + ball.getDiameter()) / 2d));

                        ball.setPosition(newBallPosition.convertToPosition());
                    }

                    player.setBlockTimeRemaining(BLOCK_TIME_AFTER_KICK);
                } else {
                    this.debugManager.print(String.format("Spieler %d ist nicht in Ballbesitz in Aktion kickBall",
                            playerId));
                }
            } else {
                if (strength > 0) {
                    this.debugManager.print(String.format(
                            "Strength muss größer 0 sein (Spieler %d in Aktion kickBall)", playerId));
                } else {
                    this.debugManager.print(String.format(PLAYER_BLOCKED_ERROR_MSG, playerId, "kickBall"));
                }
            }
        } else {
            this.debugManager.print(String.format(COMPLETED_ACTION_ERROR_MSG, "kickBall"));
        }
    }

    /**
     * <p>
     * Ändert die Laufrichtung und die Geschwindigkeit eines Spielers.
     * </p>
     * 
     * <p>
     * Diese Action kann nur auf Spieler angewandt werden, die nicht geblockt sind(siehe
     * {@link TickInformation#getMinimalBlockingTime(int)}).
     * </p>
     * 
     * @param playerId
     *            Die Nummer des Spieler im Team der KI.
     * @param targetDirection
     *            Eine Position relativ zu der Richtung in die der Spieler beschleunigen soll. Es soll dabei nur ein
     *            Vektor angegeben werden, in dessen Richtung der Spieler sich bewegen soll. Die angegebene Position
     *            wird letzendlich nicht unbedingt erreicht.
     * @param targetSpeed
     *            Die Geschwindigkeit, abhängig von der Maximalgeschwindkeit eines Spieler, mit der der Spieler sich
     *            bewegen soll. Der Wert dieser Variable bewegt sich im Bereich ]0;1], wobei 0 Stillstand und 1
     *            Maximalgeschwindigkeit bedeutet.
     */
    public void changePlayerDirection(int playerId, Position targetDirection, double targetSpeed) {
        if (!actionCompleted) {
            if (playerId >= this.players.size()) {
                throw new ArrayIndexOutOfBoundsException(String.format("Spieler-ID %d existiert nicht!", playerId));
            }

            PlayerGameObject player = players.get(playerId);
            if (player.getBlockTimeReamining() == 0) {
                if (playerId >= this.players.size()) {
                    throw new ArrayIndexOutOfBoundsException(String.format("Spieler-ID %d existiert nicht!", playerId));
                }

                targetSpeed = Math.abs(targetSpeed);
                if (targetSpeed > 1.0d) {
                    targetSpeed = 1.0d;
                }

                targetSpeed *= player.getMaxSpeed(this.ball);

                Vector2D calculatedTargetDirection = new Vector2D(targetDirection.getX(), targetDirection.getY());
                if (calculatedTargetDirection.getLength() != 0)
                    calculatedTargetDirection = calculatedTargetDirection.getNewLengthVector(targetSpeed);

                player.setTargetDirection(calculatedTargetDirection);
            } else {
                this.debugManager.print(String.format(PLAYER_BLOCKED_ERROR_MSG, playerId, "changePlayerDirection"));
            }
        } else {
            this.debugManager.print(String.format(COMPLETED_ACTION_ERROR_MSG, "changePlayerDirection"));
        }
    }

    /**
     * Bei Aufruf wird der ActionHandler "deaktiviert". <br>
     * Dies wird verwendet, um alte ActionHandler vergangener Ticks, welche nicht mehr benötigt werden, mitzuteilen,
     * dass sie keine Berechtigung mehr haben, Änderungen an Werten vorzunehmen.<br>
     * <br>
     * Im konkreten Fall wird dies verwendet, um zu verhindern, dass eine KI unerlaubter Weise die Referenz auf einen
     * alten ActionHandler speichert (wie z.B. {@link SoSiKickActionHandler}), um z.B. auf die Methode
     * {@link SoSiKickActionHandler#placePlayer} zuzugreifen, obwohl der aktuell geltende ActionHandler vom Typ
     * {@link SoSiFreePlayActionHandler} ist. Ohne diese Funktion könnte die KI auch während dem regulären
     * Spielgeschehen Spieler beliebig und schlagartig positionieren, was durch diesen Methodenaufruf verhindert wird.
     */
    public void completeAction() {
        this.actionCompleted = true;
    }
}
