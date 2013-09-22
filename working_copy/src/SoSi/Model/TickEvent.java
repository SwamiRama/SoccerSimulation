package SoSi.Model;

/**
 * Beinhaltet Spiel-Ereignisse, die während eines Ticks in der Simulation auftreten können. Diese stellen Key-Events
 * dar, auf die die Berechnung und die Wiedergabe besonders reagieren.
 */
public enum TickEvent {

    // Anmerkung: Events sind aufsteigend ihrer Gewichtigkeit angeordnet!

    /**
     * Foul: Durch Missglückte Ballabhnahme
     */
    FOUL_TACKLING,

    /**
     * Foul: Ball im Aus
     */
    FOUL_OFF,

    /**
     * Foul: Abseits
     */
    FOUL_OFFSIDE,

    /**
     * Freistoß
     */
    FREE_KICK,

    /**
     * Anstoß
     */
    KICK_OFF,

    /**
     * Tor erzielt
     */
    GOAL_SCORED,

    /**
     * Halbzeit
     */
    HALFTIME,

    /**
     * Spielunterbrechung
     */
    GAME_INTERRUPTED,

}
