package sep.football;


public interface AI {

	/**
	 * This method is called for a kick off.
	 *
	 * @param game
	 * @param tick
	 * @param actionHandler
	 */
	public void kickOff(GameInformation game, TickInformation tick, KickActionHandler actionHandler);

	/**
	 * This method is called for a free kick.
	 *
	 * @param game
	 * @param tick
	 * @param actionHandler
	 */
	public void freeKick(GameInformation game, TickInformation tick, KickActionHandler actionHandler);

	/**
	 * This method is called if the game is running normally.
	 *
	 * @param game
	 * @param tick
	 * @param actionHandler
	 */
	public void freePlay(GameInformation game, TickInformation tick, FreePlayActionHandler actionHandler);

}
