package sep.football;

public interface FreePlayActionHandler extends ActionHandler {

	/**
	 * <p>This method lets a player take over control of the ball.</p>
	 *
	 * <p>If the ball is in possession of another player, the action consists of
	 * acquiring the ball via tackling. If successful, the ball is subsequently in
	 * possession of the AI's player. If unsuccessful, the ball remains in the
	 * possession of the tackled player and the tackling player is blocked for
	 * several ticks. This action may result in a foul.</p>
	 *
	 * <p>If the ball is not in possession of another player, the player tries
	 * to acquire possession of the ball. An unsuccessful attempt will block the
	 * player. An unsuccessful attempt may change the ball's direction.</p>
	 *
	 * <p>Success of this action depends on several factors:
	 * <ol>
	 *   <li>The distance between the player and the ball.
	 *     The further away the player is from the ball, the more likely the action
	 *     will fail.</li>
	 *
	 *   <li>The speed of the ball.
	 *     A ball with higher speed is more difficult to acquire.</li>
	 *
	 *   <li>The speed of the player.
	 *     A faster player has more difficulty acquiring a ball.</li>
	 *
	 *   <li>The speed of the opposing player if the ball is
	 *     in control of another player. If the opposing player's speed is
	 *     higher, it is easier to acquire the ball.</li>
	 * </ol>
	 * </p>
	 *
	 *
	 * @param playerId
	 *            The id of the player in the AI's team.
	 * @param determination
	 * 			  A value in ]0,1] that indicates the determination of a player
	 * 			  to acquire the ball, with 1 being the highest. Higher values
	 * 			  lead to a greater chance of success, but a longer blocking
	 * 			  penalty in case of failure. If another player is in possession
	 * 	          of the ball, higher values bear a greater risk of foul.
	 */
	public void acquireBallControl(int playerId, double determination);
}
