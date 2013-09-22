package SoSi.View;

/**
 * Diese Klasse bedient sich dem Singleton-Entwurfsmodell. Beinhaltet erweiterte, technische, Informationen, die in der
 * GUI dargestellt werden (wie das Anzeigen der Frames/Second oder das Anzeigen von Debug-Grafiken), sowie eine Methode,
 * um mit den maximal möglichen Fames/Seconds die Simulation abspielen zu lassen.
 */
public final class RuntimeArguments {

    /**
     * Gibt an, ob die Frames/Second angezeigt werden sollen.
     */
    private boolean fpsDisplay = false;

    /**
     * Gibt an, ob die Simulation mit den maximal möglichen Fames/second abgespielt werden soll.
     */
    private boolean fpsUnlimited = false;

    /**
     * Gibt an, ob die Debug-Grafiken (Umrandung des Spielfelds, des Anstoßkreises und der Torpfosten).
     */
    private boolean debugGraphicsActivated = false;

    /**
     * Gibt an, ob bei der Wiedergabe der Simulation die Schritte zwischen zwei Ticks bei der Anzeige interpoliert
     * werden sollen.
     */
    private boolean interpolationDeactivated = false;

    /**
     * Gibt an, ob die Wiedergabe von animierten Hintergründen deaktiviert werden soll.
     */
    private boolean animationsDeactivated = false;

    /**
     * Gibt an, ob die Wiedergabe von animierten Hintergründen deaktiviert werden soll.
     * 
     * @return <code>true</code>, falls die animierte Darstellung deaktiviert werden soll, <code>false</code>
     *         andernfalls
     */
    public boolean isAnimationsDeactivated() {
        return animationsDeactivated;
    }

    /**
     * Setzt den Wert, ob die Wiedergabe von animierten Hintergründen deaktiviert werden soll.
     * 
     * @param animationsDeactivated
     *            <code>true</code>, falls die animierte Darstellung deaktiviert werden soll, <code>false</code>
     *            andernfalls
     */
    public void setAnimationsDeactivated(boolean animationsDeactivated) {
        this.animationsDeactivated = animationsDeactivated;
    }

    /**
     * Gibt an, ob bei der Wiedergabe der Simulation die Schritte zwischen zwei Ticks bei der Anzeige interpoliert
     * werden sollen.
     * 
     * @return <code>true</code>, falls die Interpolation deaktiviert werden soll, <code>false</code> andernfalls
     */
    public boolean isInterpolationDeactivated() {
        return interpolationDeactivated;
    }

    /**
     * Setzt den Wert, ob bei der Wiedergabe der Simulation die Schritte zwischen zwei Ticks bei der Anzeige
     * interpoliert werden sollen.
     * 
     * @param interpolationDeactivated
     *            <code>true</code>, falls die Interpolation deaktiviert werden soll, <code>false</code> andernfalls
     */
    public void setInterpolationDeactivated(boolean interpolationDeactivated) {
        this.interpolationDeactivated = interpolationDeactivated;
    }

    /**
     * Die gespeicherte Referenz auf die Singleton-Klasse.
     */
    private static final RuntimeArguments singletonRuntimeArguments = new RuntimeArguments();

    /**
     * Erstellt die neue Singleton-Klasse.
     */
    private RuntimeArguments() {
    }

    /**
     * Gibt die (einzige) Referenz auf die Singleton-Klasse zuück.
     * 
     * @return Die Referenz auf die Singleton-Klasse.
     */
    public static RuntimeArguments getRuntimeArguments() {
        return singletonRuntimeArguments;
    }

    /**
     * Gibt zurück, ob die Frames/Seconds in der GUI dargestellt werden sollen.
     * 
     * @return True, wenn die Frames/Seconds in der GUI dargestellt werden sollen, sonst false.
     */
    public boolean isFpsDisplayActivated() {
        return fpsDisplay;
    }

    /**
     * Gibt zurück, ob die Simulation mit der maximal möglichen Anzahl an Frames/Seconds wiedergegeben werden soll.
     * 
     * @return True, wenn die Simulation mit der maximal möglichen Anzahl an Frames/Seconds wiedergegeben werden soll,
     *         sonst false.
     */
    public boolean isFpsUnlimited() {
        return fpsUnlimited;
    }

    /**
     * Gibt zurück, ob die Debug-Grafiken in der GUI argestellt werden sollen.
     * 
     * @return True, wenn die Debug-Grafiken in der GUI argestellt werden sollen, sonst false.
     */
    public boolean isDebugGraphicsActivated() {
        return debugGraphicsActivated;
    }

    /**
     * Bestimmt, ob die Frames/Seconds in der GUI dargestellt werden sollen.
     * 
     * @param fpsDisplay
     *            True, wenn die Frames/Seconds in der GUI dargestellt werden sollen, sonst false.
     */
    public void setFpsDisplay(boolean fpsDisplay) {
        this.fpsDisplay = fpsDisplay;
    }

    /**
     * Bestimmt, ob die Simulation mit der maximal möglichen Anzahl an Frames/Seconds wiedergegeben werden soll.
     * 
     * @param fpsUnlimited
     *            True, wenn die Simulation mit der maximal möglichen Anzahl an Frames/Seconds wiedergegeben werden
     *            soll, sonst false.
     */
    public void setFpsUnlimited(boolean fpsUnlimited) {
        this.fpsUnlimited = fpsUnlimited;
    }

    /**
     * Bestimmt, ob die Debug-Grafiken in der GUI argestellt werden sollen.
     * 
     * @param debugGraphicsActivated
     *            True, wenn die Debug-Grafiken in der GUI argestellt werden sollen, sonst false.
     */
    public void setDebugGraphicsActivated(boolean debugGraphicsActivated) {
        this.debugGraphicsActivated = debugGraphicsActivated;
    }
}
