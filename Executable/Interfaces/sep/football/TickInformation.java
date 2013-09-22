package sep.football;

import java.util.List;

public interface TickInformation {

	/**
	 * Returns the current tick number.
	 *
	 * @return The number of ticks, starting with 0.
	 */
	public int getCurrentTickNumber();

	/**
	 * <p>Returns the positions of the players in the AI's team.</p>
	 *
	 * <p>The id of players, ie. their position in the returned list, does not
	 * change throughout the game.</p>
	 *
	 * @return The positions of the players in the AI's team.
	 */
	public List<Position> getPlayerPositions();

	/**
	 * Returns the positions of the players in the opposing team.
	 *
	 * <p>The id of players, ie. their position in the returned list, does not
	 * change throughout the game.</p>
	 *
	 * @return The positions of the players in the opposing team.
	 */
	public List<Position> getOpponentPlayerPositions();

	/**
	 * Returns the position of the ball.
	 *
	 * @return The current position of the ball.
	 */
	public Position getBallPosition();

	/**
	 * Returns if a team given as a parameter is in possession of the ball.
	 *
	 * @param ownTeam If <code>true</code>, returns if the AI's team is in
	 * possession of the ball, if <code>false</code> returns if the opponent's team
	 * is in possession of the ball.
	 *
	 * @return <code>true</code> if the team indicated as a parameter has the ball,
	 * <code>false</code> otherwise.
	 */
	public boolean hasTeamBall(boolean ownTeam);

	/**
	 * <p>Returns the id of the player of the team with the ball.</p>
	 *
	 * @return The id of the player with the ball.
	 * @throws IllegalStateException Thrown, if no team has the ball.
	 */
	public int getPlayerWithBall() throws IllegalStateException;

	/**
	 * Gets the number of goals the AI's team has scored since the beginning of
	 * the game.
	 *
	 * @return The number of goals scored by the own team.
	 */
	public int getTeamGoals();

	/**
	 * Returns the number of goals the opposing team has scored since the
	 * beginning of the game.
	 *
	 * @return The number of goals scored by the opposing team.
	 */
	public int getOpponentTeamGoals();

	/**
	 * Returns the number of ticks a certain player is blocked.
	 * This provides a lower boundary.
	 *
	 * @param playerId
	 *            The id of the player in the AI's team.
	 * @return The minimal number of ticks the player remains blocked. If 0, the
	 *            player is not blocked.
	 */
	public int getMinimalBlockingTime(int playerId);

	/**
	 * Determines whether the team is on the left.
	 *
	 * @return <code>true</code> if the AI's team currently plays on the left,
	 * <code>false</code> otherwise.
	 */
	public boolean isPlayingOnTheLeft();

}
