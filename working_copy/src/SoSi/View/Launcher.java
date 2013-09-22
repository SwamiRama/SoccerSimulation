package SoSi.View;

import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import SoSi.Model.PlaybackHandler;

/**
 * Klasse, welche die Programmstartmethode enthält und die Anzeige des Hauptfensters startet,
 */
public class Launcher {

    /**
     * Dateipfad zur Policy-Datei
     */
    private static final String PATH_TO_POLICY = "/resources/policies/restrictedPolicy.policy";

    /**
     * Programmstartmethode, welche das Hauptfenster initialisiert und anzeigt. <br>
     * Dazu werden die notwendigen Klassen, wie PlaybackHandler und das Hauptfenster erstellt und anschließend
     * gestartet.<br>
     * Des Weiteren werden die Kommandozeilenargumente eingelesen und entsprechend in {@link RuntimeArguments} gesetzt.
     * 
     * @param args
     *            Kommandozeilenargumente (Werden vom Programm nicht benötigt und daher ignoriert).
     */
    public static void main(String[] args) {
        // Check for command line arguments
        for (String string : args) {
            if (string.equals("-fps")) {
                RuntimeArguments.getRuntimeArguments().setFpsDisplay(true);
            } else if (string.equals("-fpsUnlimited")) {
                RuntimeArguments.getRuntimeArguments().setFpsUnlimited(true);
            } else if (string.equals("-debugGraphics")) {
                RuntimeArguments.getRuntimeArguments().setDebugGraphicsActivated(true);
            } else if (string.equals("-noInterpolation")) {
                RuntimeArguments.getRuntimeArguments().setInterpolationDeactivated(true);
            } else if (string.equals("-noAnimation")) {
                RuntimeArguments.getRuntimeArguments().setAnimationsDeactivated(true);
            }
        }

        // Generate policy-file URL
        URL policeResource = Thread.currentThread().getClass().getResource(PATH_TO_POLICY);

        // Check if policy exists
        if (policeResource == null) {
            System.err.println("Policy file not found! Aborting.");
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, String.format(
                            "Die benötigte policy-Datei konnte nicht gefunden werden.\n" + "Pfad: %s", PATH_TO_POLICY),
                            "Policy-Datei nicht gefunden", JOptionPane.ERROR_MESSAGE);
                }
            });
        } else {
            // Load policy and start SecurityManager
            System.setProperty("java.security.policy", policeResource.toString());

            SecurityManager securityManager = System.getSecurityManager();
            if (securityManager == null) {
                securityManager = new SecurityManager();
                System.setSecurityManager(securityManager);
            }

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    PlaybackHandler playbackHandler = new PlaybackHandler(SoccerGUI.FIELD_WIDTH,
                            SoccerGUI.FIELD_LENGTH, SoccerGUI.GOAL_SIZE, SoccerGUI.PLAYER_DIAMETER,
                            SoccerGUI.BALL_DIAMETER);

                    SoccerGUI gui = new SoccerGUI(playbackHandler);
                    gui.setVisible(true);
                }
            });

        }
    }
}
