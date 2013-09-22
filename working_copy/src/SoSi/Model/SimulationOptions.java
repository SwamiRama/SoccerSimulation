package SoSi.Model;

import SoSi.Model.GamePhysics.GamePhysicsHandler;
import SoSi.Model.GameRules.GameRulesHandler;

/**
 * Simulationseinstellungen, die während der Erstellung einer neuen Simulation ausgewählt werden können.<br>
 * Diese Einstellungen beziehen sich ausschließlich auf das Aktivieren von Spielregeln und zusätzlicher physikalischer
 * Berechnungen.<br>
 * Diese Optionen werden in Form einer Liste in der Klasse {@link GamePhysicsHandler} und {@link GameRulesHandler}
 * verwendet, um die aktiven Regeln bzw physikalischen Berechnungen zu verwalten und auszuführen.
 */
public enum SimulationOptions {
    /**
     * Aus-Regel (= keine Bande)
     */
    OFF_RULE, 
    
    /**
     * Abseits-Regel
     */
    OFFSIDE_RULE
}
