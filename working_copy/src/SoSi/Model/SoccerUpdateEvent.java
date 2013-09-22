package SoSi.Model;

/**
 * Repräsentiert Ereignisse, die während der Wiedergabe der Simulation per Observer-Pattern an die Observer von
 * PlaybackHandler übergeben werden.
 * 
 * @see TickEvent
 */
public enum SoccerUpdateEvent {
    /**
     * Wiedergabe gestartet
     */
    PLAYBACK_STARTED,

    /**
     * Wiedergabe pausiert
     */
    PLAYBACK_PAUSED,

    /**
     * Wiedergabe abgebrochen
     */
    PLAYBACK_ABORTED,

    /**
     * Wiedergabe: Ende erreicht
     */
    PLAYBACK_END_REACHED,

    /**
     * Ein einzelner Simulationsschritt (Wiedergabe)
     */
    PLAYBACK_STEP,

    /**
     * Simulationsberechnung vollständig abgeschlossen
     */
    SIMULATION_FINISHED, 
    
    /**
     * Die Wiedergabegeschwindigkeit wurde geändert
     */
    PLAYBACK_SPEED_CHANGED,

    /**
     * @see TickEvent#GOAL_SCORED
     */
    GOAL_SCORED,
    /**
     * @see TickEvent#FREE_KICK
     */
    FREE_KICK,

    /**
     * @see TickEvent#KICK_OFF
     */
    KICK_OFF,

    /**
     * @see TickEvent#HALFTIME
     */
    HALFTIME,

    /**
     * @see TickEvent#FOUL_TACKLING
     */
    FOUL_TACKLING,

    /**
     * @see TickEvent#FOUL_OFF
     */
    FOUL_OFF,

    /**
     * @see TickEvent#FOUL_OFFSIDE
     */
    FOUL_OFFSIDE
}
