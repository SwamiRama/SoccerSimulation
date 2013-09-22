package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dient dazu, das Programm zu beenden. Dies erfolgt, indem sämtliche geöffneten Fenster geschlossen werden.
 */
public class Quit implements ActionListener {
    
    @Override
    public void actionPerformed(ActionEvent e) {
        for (java.awt.Frame frame : java.awt.Frame.getFrames()) {
            frame.dispose();
        }
    }
}
