/**
 * 
 */
package SoSi.Debugging;

import sep.football.AI;

/**
 * Dieses Interface erweitert eine {@link AI} zu einer Debugging AI.<br>
 * Eine Debugging AI verfügt über eine zusätzliche Methode {@link #debug(DebugManager)}, welche vom Framework zu jedem
 * Tick ausgeführt wird. Mit Hilfe des übergebenen {@link DebugManager} lassen sich vom Entwickler einer KI Nachrichten
 * auf einer Debugging Konsole ausgeben.
 */
public interface DebuggingAI extends AI {

	/**
	 * Diese Methode dient zum Debugging der KI und wird zu jedem Tick ausgeführt.
	 * 
	 * @param manager
	 *            Eine Referenz auf den übergebenen {@link DebugManager}.
	 */
	public void debug(DebugManager manager);

}
