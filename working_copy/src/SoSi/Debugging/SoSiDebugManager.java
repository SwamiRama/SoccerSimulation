package SoSi.Debugging;

/**
 * Sammelt die Eingaben einer {@link DebuggingAI} als String, welcher vom Framework ausgelesen werden kann.
 */
public class SoSiDebugManager implements DebugManager {

    /**
     * Die Debug-Nachrichteneingabe der KI.
     */
    private String debugMessage;

    /**
     * Initialisiert einen neuen SoSiDebugManager.
     */
    public SoSiDebugManager() {
        this.debugMessage = "";
    }

    @Override
    public void print(String message) {
        if (!debugMessage.isEmpty())
            this.debugMessage += "\n";
        
        this.debugMessage += message;
    }

    /**
     * Dient dazu, die Eingaben einer {@link DebuggingAI} zu erfragen.
     * 
     * @return Die Nachrichten, welche eine {@link DebuggingAI} bereits eingegeben hat.
     */
    public String getDebugMessage() {
        return this.debugMessage;
    }
    
    /**
     * Löscht den bisherigen Inhalt der Debugnachricht.
     */
    public void clear() {
        this.debugMessage = "";
    }

}
