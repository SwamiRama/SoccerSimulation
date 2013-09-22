package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import SoSi.View.AboutDialog;

/**
 * Öffnet ein Fenster, welches die Versionsnummer und Informationen über das Simulationsprogramm enthält.
 */
public class About implements ActionListener {

    /**
     * Der Frame, indem der Inhalt angzeigt werden soll.
     */
    private final JFrame aboutFrame;

    /**
     * Konstruktor des About-Frames
     */
    public About() {
        this.aboutFrame = new AboutDialog();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.aboutFrame.setVisible(true);
    }
}
