package sep.football;

public interface KickActionHandler extends ActionHandler {

	/**
	 * <p>Sets the player on a given position in the field.</p>
	 *
	 * <p>
	 * <dl>
	 *   <dt>During free kick:</dt>
	 *   <dd>The players can be placed on the entire field, with the exception
	 *   of a circle of diameter X around the position of the ball.
	 *   One player of the team that has the kick off must be placed at the position
	 *   of the ball.
	 *   </dd>
	 *
	 *   <dt>During kick off:</dt>
	 *   <dd>The players can be placed on the their teams part of the field.
	 *	 Only one player is allowed in the circle with the diameter X around
	 *   the ball.
	 *   One player of the team that has the free kick must be placed at the
	 *   position of the ball.
	 *   </dd>
	 * </dl>
	 * </p>
	 *
	 * @param playerId
	 *            The id of the player in the AI's team.
	 * @param pos
	 * 			  The position at which the player is to be placed.
	 */
	public void placePlayer(int playerId, Position pos);

}
