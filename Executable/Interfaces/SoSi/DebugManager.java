package SoSi.Debugging;

/**
 * Der DebugManager erlaubt Ausgaben auf einer Entwicklerkonsole.<br>
 * Durch die implementierten Methoden ist es dem Entwickler möglich, zu jedem Tick Ausgaben zu erstellen. Dies
 * ermöglicht dem Entwickler ein vereinfachtes Debuggen, da Entscheidungen einer KI leichter nachvollzogen werden
 * können.
 */
public interface DebugManager {

    /**
     * Fügt einen String als Debugnachricht dem DebugManager hinzu, welcher diese verwaltet. Dadurch ist es möglich,
     * jeder KI einen einzelnen DebugManager zu übergeben, wodurch unterschieden werden kann, welche KI welche
     * Debugnachricht erzeugt hat.
     * 
     * @param message
     *            Die Debugnachricht, welche Informationen zum Nachvollziehen von Verhaltensweisen oder Fehlerbehebung
     *            von KIs anbietet.
     */
    public void print(String message);

}
