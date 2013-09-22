package sep.football;

/**
 * This interface grants access to information global to the game and valid in
 * all ticks.
 */
public interface GameInformation {

	/**
	 * The width of the field.
	 *
	 * @return The width of the field.
	 */
	public double getFieldWidth();

	/**
	 * The length of the field.
	 *
	 * @return The length of the field.
	 */
	public double getFieldLength();

	/**
	 * Returns the size of the goal.
	 *
	 * @return The size of the goal.
	 */
	public double getGoalSize();

	/**
	 * Returns the diameter of the exclusive circle around the ball and player.
	 * Inside this circle, no other players are allowed during a free kick or kick
	 * off.
	 *
	 * @return The diameter of the exclusive circle around the ball and player in a
	 * free kick or kick off situation.
	 */
	public double getCircleDiameter();

	/**
	 * The diameter of a player.
	 *
	 * @return The diameter of a player.
	 */
	public double getPlayerDiameter();

	/**
	 * The diameter of the ball.
	 *
	 * @return The diameter of the ball.
	 */
	public double getBallDiameter();

	/**
	 * Returns the number of ticks in the entire game.
	 *
	 * @return The number of ticks in the game.
	 */
	public int getMaximumTickNumber();

}
