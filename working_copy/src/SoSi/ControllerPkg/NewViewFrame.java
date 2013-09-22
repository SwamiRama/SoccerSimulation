package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import SoSi.Model.PlaybackHandler;
import SoSi.View.SimulationOptionsFrame;
import SoSi.View.SimulationOptionsFrameType;
import SoSi.View.SoccerGUI;

/**
 * Dient dazu, ein neues Ansichtsfenster des Spiels zu erstellen. Dabei öffnet sich ein Fenster, welches die Art der
 * Ansicht abfragt.
 */
public class NewViewFrame extends ControllerFrameAction {
    /**
     * @see ControllerFrameAction#ControllerFrameAction(PlaybackHandler, SimulationOptionsFrame)
     */
    public NewViewFrame(PlaybackHandler playbackHandler, SimulationOptionsFrame simulationOptionsFrame) {
        super(playbackHandler, simulationOptionsFrame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.simulationOptionsFrame.show(SimulationOptionsFrameType.NEW_VIEW_FRAME, new NewViewFrameConfirmed(
                playbackHandler, simulationOptionsFrame));
    }

    /**
     * ActionListener, welcher ausgeführt wird, wenn der Benutzer das Starten eines neuen Darstellungsfensters im
     * Dialogfenster bestätigt.
     */
    private class NewViewFrameConfirmed extends ControllerFrameAction {

        /**
         * @see ControllerFrameAction#ControllerFrameAction(PlaybackHandler, SimulationOptionsFrame)
         */
        public NewViewFrameConfirmed(PlaybackHandler playbackHandler, SimulationOptionsFrame simulationOptionsFrame) {
            super(playbackHandler, simulationOptionsFrame);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    SoccerGUI newViewFrame = new SoccerGUI(playbackHandler, false, simulationOptionsFrame
                            .getSelectionSoccerPanel(true));
                    
                    simulationOptionsFrame.setVisible(false);
                    newViewFrame.setVisible(true);
                }
            });
        }
    }

}
