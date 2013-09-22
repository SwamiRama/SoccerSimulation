package SoSi.Model.Calculation;

import java.util.List;

import sep.football.FreePlayActionHandler;
import SoSi.Debugging.DebugManager;
import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GameObjects.PlayerGameObject;

/**
 * Beinhaltet Spielaktionen, die jeder KI in den Ticks zur Verfügung stehen, in denen das Spiel nicht unterbrochen ist.
 */
public class SoSiFreePlayActionHandler extends SoSiActionHandler implements FreePlayActionHandler {

    private static final double W_B_NP = 1.0;
    private static final double W_S_NP = 1.0;
    private static final double W_DIST_SB_NP = 1.0;
    private static final double W_DET_NP = 1.0;

    private static final double W_G_P = 1.0;
    private static final double W_S_P = 1.0;
    private static final double W_DIST_SG_P = 1.0;
    private static final double W_DET_P = 1.0;

    private static final double DIST_MAX = 1.0;

    private static final double K_ERFOLG = 0.75;
    private static final double K_FOUL = 0.75;

    private static final int T_MAX = 10;
    private static final int T_G = 2;

    /**
     * Flag, ob ein Foul (bei versuchter Ballabnahme) eingetreten ist
     * 
     * @see #hasFoulHappend()
     */
    private boolean hasFoulHappened = false;

    /**
     * Konstruktor für die SoSiFreePlayActionHandler Klasse.<br>
     * Eine SoSiFreePlayActionHandler braucht eine Referenz auf die Spieler, für welche eine Aktion gelten soll, sowie
     * eine Referenz auf den Spielball.
     * 
     * @param player
     *            Eine Referenz auf die Spieler.
     * @param ball
     *            Eine Referenz auf den Spielball.
     * @param debugManager
     */
    public SoSiFreePlayActionHandler(List<PlayerGameObject> player, BallGameObject ball, DebugManager debugManager) {
        super(player, ball, debugManager);
    }

    /**
     * <p>
     * Diese Methode lässt einen Spieler die Kontrolle über den Ball erlangen.
     * </p>
     * 
     * <p>
     * Wenn der Ball im Besitz eines anderen Spielers ist, besteht die Action aus der Erlangung der des Ballbesitzes
     * durch tackeling. Wenn die Action erfolgreich ist, dann befindet sich der Ball daraufhin im Besitz des Spielers
     * der KI, dem sie diese Handlungsweisung gegeben hat. Wenn die Action nicht erfolgreich war, bleibt der Ball beim
     * getackelten Spieler und der tackelnde Spieler ist für einige ticks geblockt. Diese Action kann ein Foul ergeben.
     * </p>
     * 
     * <p>
     * Wenn der Ball nicht im Besitz eines anderen Spielers ist, dann kann der Spieler versuchen die Kontrolle über den
     * Ball zu erlangen. Wenn die Action nicht erfolgreich ist, dann wird der Spieler für einige ticks geblockt. Ein
     * erfolgloser Versuch den Ballbesitz zu erlangen, kann die Richtung des Balles ändern.
     * </p>
     * 
     * <p>
     * Der Erfolg dieser Action hängt von einigen Faktoren ab:
     * <ol>
     * <li>Die Distanz zwischen dem Spieler und dem Ball. Je weiter der Spieler vom Ball entfernt ist, desto
     * wahrscheinlicher wird diese Action misslingen. the action will fail.</li>
     * 
     * <li>Die Geschwindigkeit des Balles. Ein Ball mit einer höheren Geschwindigkeit, ist schwieriger unter Kontrolle
     * zu bringen.</li>
     * 
     * <li>Die Geschwindigkeit des Spielers. Je schneller der Spieler ist, desto schwierig ist es für ihn, die Kontrolle
     * über den Ball zu erlangen.</li>
     * 
     * <li>Die Geschwindigkeit des gegnerischen Spielers, wenn der Ball im Besitz eines anderen Spielers ist. Wenn die
     * Geschwindigkeit des gegnerischen Spielers höher ist, ist es leichter die Kontrolle über den Ball zu erlangen.</li>
     * </ol>
     * </p>
     * 
     * @param playerId
     *            Die Nummer des Spieler im Team der KI.
     * @param determination
     *            Ein Wert zwischen ]0;1], der die Entschlossenheit des Spielers angibt, der die Ballkontrolle erlangen
     *            möchte. 1 ist der höchste Werte. Höhere Werte erhöhen die Chance die Ballkontrolle zu erlangen,
     *            erhöhen aber auch die Länge, die der Spieler nach einem erfolglosen Versuch geblockt ist. Wenn ein
     *            anderer Spieler im Besitz des Balles ist, erhöht ein höhrer Wert auch das Risiko ein Foul zu begehen.
     * 
     */
    public void acquireBallControl(int playerId, double determination) {
        if (!actionCompleted) {

            if (determination <= 0 || determination > 1)
                throw new IllegalArgumentException("Determination nicht im Bereich ]0;1]");
            if (playerId >= this.players.size())
                throw new ArrayIndexOutOfBoundsException(String.format("Spieler-ID %d existiert nicht!", playerId));

            if (players.get(playerId).getBlockTimeReamining() <= 0) {
                PlayerGameObject actualPossessingPlayer = this.ball.getBallPossession();
                PlayerGameObject possessingPlayer = this.players.get(playerId);

                double dist_SB = new Vector2D(possessingPlayer.getPosition(), ball.getPosition()).getLength()
                        - (possessingPlayer.getDiameter() / 2) - (ball.getDiameter() / 2);

                if (dist_SB < DIST_MAX) {
                    if (actualPossessingPlayer == null) {
                        // Der Ball ist frei im Feld
                        double p = calcProbabilityNoPossession(possessingPlayer, ball, determination);
                        double random = Math.random();

                        if (random <= p) {
                            ball.setBallPossession(possessingPlayer);
                        } else {
                            possessingPlayer.setBlockTimeRemaining((int) Math.round(T_MAX * determination));
                            possessingPlayer.setTargetDirection(new Vector2D(0, 0));
                        }

                    } else if (!players.contains(actualPossessingPlayer)) {
                        // Der Ball ist im Besitz eines Spieler, darf jedoch nicht von einem Mitspieler aus dem eigene
                        // Team abgenommen werden
                        double p = calcProbabilityPossession(possessingPlayer, actualPossessingPlayer, ball,
                                determination);
                        double random = Math.random();

                        if (random < p) {
                            actualPossessingPlayer.setBlockTimeRemaining(T_G);
                            ball.setBallPossession(possessingPlayer);
                        } else {
                            double p_foul = calcProbabilityFoul(determination, K_FOUL);
                            double random_foul = Math.random();
                            if (random_foul <= p_foul) {
                                hasFoulHappened = true;
                            } else {
                                possessingPlayer.setBlockTimeRemaining((int) Math.round(T_MAX * determination));
                                possessingPlayer.setTargetDirection(new Vector2D(0, 0));
                            }
                        }
                    }
                }
            } else {
                this.debugManager.print(String.format(PLAYER_BLOCKED_ERROR_MSG, playerId, "acquireBallControl"));
            }
        } else {
            this.debugManager.print(String.format(COMPLETED_ACTION_ERROR_MSG, "acquireBallControl"));
        }
    }

    /**
     * Gibt an, ob ein Foul eingetreten ist. <br>
     * Dies kann der Fall sein, wenn eine KI Versucht, einem anderen Spieler,
     * 
     * @return <code>true</code>, falls ein Foul eingetreten ist.
     */
    public boolean hasFoulHappend() {
        return this.hasFoulHappened;
    }

    private double calcProbabilityNoPossession(PlayerGameObject player, BallGameObject ball, double determination) {
        double dist_SB = new Vector2D(player.getPosition(), ball.getPosition()).getLength()
                - (player.getDiameter() / 2) - (ball.getDiameter() / 2);
        double p_vs = getInfluencePlayerVNoPoss(player.getMovementDirection().getLength(), player.getMaxSpeed(ball));
        double p_vb = getInfluenceBallVNoPoss(ball.getMovementDirection().getLength(), ball.getMaxShootingSpeed());
        double p_dist = getInfluenceDistNoPoss(dist_SB, DIST_MAX);
        double p_det = getInfluenceDetNoPoss(determination);
        return 1 - (((p_vs * W_S_NP) + (p_vb * W_B_NP) + (p_dist * W_DIST_SB_NP) + (p_det * W_DET_NP)) / (W_S_NP
                + W_B_NP + W_DIST_SB_NP + W_DET_NP));
    }

    private double calcProbabilityPossession(PlayerGameObject player, PlayerGameObject opponent, BallGameObject ball,
            double determination) {
        double dist_SG = new Vector2D(player.getPosition(), opponent.getPosition()).getLength()
                - (player.getDiameter() / 2) - (opponent.getDiameter() / 2);
        double p_vs = getInfluencePlayerVPoss(player.getMovementDirection().getLength(), player.getMaxSpeed(ball));
        double p_vg = getInfluenceOpponentVPoss(opponent.getMovementDirection().getLength(), opponent.getMaxSpeed(ball));
        double p_dist = getInfluenceDistPoss(dist_SG, DIST_MAX);
        double p_det = getInfluenceDetPoss(determination);
        if (dist_SG >= DIST_MAX) {
            return 0;
        } else {
            return (((p_vs * W_S_P) + (p_vg * W_G_P) + (p_dist * W_DIST_SG_P) + (p_det * W_DET_P)) / (W_S_P + W_G_P
                    + W_DIST_SG_P + W_DET_P))
                    * K_ERFOLG;
        }
    }

    private double getInfluencePlayerVNoPoss(double v_S, double v_S_MAX) {
        return v_S / v_S_MAX;
    }

    private double getInfluenceBallVNoPoss(double v_B, double v_B_MAX) {
        return v_B / v_B_MAX;
    }

    private double getInfluenceDistNoPoss(double dist_SB, double dist_MAX) {
        return dist_SB / dist_MAX;
    }

    private double getInfluenceDetNoPoss(double det) {
        return Math.pow((1 - det), 2);
    }

    private double getInfluencePlayerVPoss(double v_S, double v_S_MAX) {
        return 1 - (v_S / v_S_MAX);
    }

    // v_SB_MAX = v_GB_MAX
    private double getInfluenceOpponentVPoss(double v_G, double v_GB_MAX) {
        return v_G / v_GB_MAX;
    }

    private double getInfluenceDistPoss(double dist_SG, double dist_MAX) {
        return dist_SG / dist_MAX;
    }

    private double getInfluenceDetPoss(double det) {
        return Math.sqrt(det);
    }

    private double calcProbabilityFoul(double det, double k_foul) {
        return det * k_foul;
    }

}
