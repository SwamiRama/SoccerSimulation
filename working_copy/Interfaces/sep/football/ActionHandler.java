package sep.football;

/**
 * Actions are the AIs way of controlling the behavior of its players.
 * This interface provides several methods to control player behavior.
 */
public interface ActionHandler {

	/**
	 * <p>Makes a player kick the ball in a desired direction with a given
	 * strength.</p>
	 *
	 * <p>
	 * This action is only successful if the player is in close proximity to the
	 * ball. Several factors influence the accuracy of a successful kick:
	 * <ul>
	 *   <li>The speed of the player. A kick is more accurate, if the player is
	 *       slower.</li>
	 *
	 *   <li>The strength of the kick. A kick with low strength is more accurate
	 *       than a kick with high strength.</li>
	 *
	 *   <li>The direction of the kick. If the direction is in accordance with the
	 *       running direction of the player, the shot is more accurate.</li>
	 * </ul>
	 * </p>
	 *
	 * @param playerId
	 *            The id of the player in the AI's team.
	 * @param direction
	 *            The direction of the shot relative to the player's position.
	 * @param strength
	 *            The strength of the shot. Must be a value in ]0,1], where 1 is
	 *            the maximum strength of the player and 0 is a kick with no
	 *            strength.
	 */
	public void kickBall(int playerId, Position direction, double strength);


	/**
	 * <p>Change the running direction and speed of a player.</p>
	 *
	 * <p>This action can only be used for players that are not
	 * blocked (see {@link TickInformation#getMinimalBlockingTime(int)}).</p>
	 *
	 * @param playerId
	 *            The id of the player in the AI's team.
	 * @param targetDirection
	 *            A position relative to the position of the player in which the
	 *            player is to accelerate. This gives a vector in which the player
	 *            is to accelerate, but not necessarily a position that will
	 *            eventually be reached.
	 * @param targetSpeed
	 * 	          Target speed of the player relative to his maximum speed.
	 *            Must be a value in [0,1], where 0 is stopping and 1 is full
	 *            speed.
	 */
	public void changePlayerDirection(int playerId, Position targetDirection,
			double targetSpeed);

}
