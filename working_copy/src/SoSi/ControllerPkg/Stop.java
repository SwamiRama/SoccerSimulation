package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import SoSi.Model.PlaybackHandler;

/**
 * Dient dazu, die Wiedergabe zu stoppen. Falls die Berechnung der Simulation abgeschlossen ist, springt die
 * Wiedergabeposition an den Anfang der Simulation. Ist die Berechnung noch nicht abgeschlossen, erscheint ein Dialog
 * mit folgenden Optionen: "Zum Anfang springen und Simulationsberechnung fortsetzen" oder "Simulation verwerfen".
 * 
 * @see PlaybackHandler#abort()
 */
public class Stop extends ControllerAction {

    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public Stop(PlaybackHandler playbackHandler) {
        super(playbackHandler);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Stets Wiedergabe anhalten & zu Anfang springen
        this.playbackHandler.pause();
        this.playbackHandler.jumpToTime(0);

        // Sonderbehanldung, falls Berechnung noch nicht abgeschlossen
        if (!this.playbackHandler.isSimulationCalculationFinished()) {
            int sel = JOptionPane.showConfirmDialog(null,
                    "Die Simulationsberechnung ist noch nicht abgeschlossen. Möchten Sie, dass diese nun "
                            + "abgebrochen und verworfen wird?\n\n"
                            + "(Dadurch werden derzeit verwendete Ressourcen wieder freigegeben)",
                    "Berechnung abbrechen?", JOptionPane.YES_NO_OPTION);
            if (sel == JOptionPane.YES_OPTION) {
                // Dialog bestätigt. Berechnung nur abbrechen, falls inzwischen nicht doch abgeschlossen
                if (!this.playbackHandler.isSimulationCalculationFinished()) {
                    this.playbackHandler.abort();
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Die Berechnung ist inzwischen abgeschlossen, daher wurde sie nicht verworfen.",
                            "Berechnung nicht abgebrochen", JOptionPane.INFORMATION_MESSAGE);
                }

            }
        }
    }
}
