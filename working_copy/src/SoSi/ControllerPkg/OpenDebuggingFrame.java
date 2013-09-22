package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import SoSi.View.DebuggingFrame;

/**
 * Dient dazu, die Entwicklerkonsole zu öffnen.
 */
public class OpenDebuggingFrame implements ActionListener {

    /**
     * Referenz auf anzuzeigende Instanz von {@link DebuggingFrame}
     */
    private final DebuggingFrame debuggingFrame;

    /**
     * Erstellt einen neuen ActionListener, welcher eine Referenz auf eine Instanz von {@link DebuggingFrame} erhält,
     * welches die Entwicklerkonsole (zum Debugging) enthält und auf Wunsch angezeigt werden kann.
     */
    public OpenDebuggingFrame(DebuggingFrame debuggingFrame) {
        this.debuggingFrame = debuggingFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        debuggingFrame.setVisible(true);
    }

}
