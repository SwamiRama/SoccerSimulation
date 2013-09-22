package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import SoSi.Model.PlaybackHandler;
import SoSi.Model.Calculation.AILoader.AiLoadingException;
import SoSi.View.SimulationOptionsFrame;

/**
 * Startet eine neue Simulation, welche mit denselben Parametern, wie die zuletzt Gestartete ausgeführt wird.
 * 
 * @see PlaybackHandler#startNewSimulation(int, int, String, String, java.util.List)
 */
public class RestartSimulation extends ControllerFrameAction {

    /**
     * @see ControllerFrameAction#ControllerFrameAction(PlaybackHandler, SimulationOptionsFrame)
     */
    public RestartSimulation(PlaybackHandler playbackHandler, SimulationOptionsFrame simulationOptionsFrame) {

        super(playbackHandler, simulationOptionsFrame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {

            int sel = JOptionPane.showConfirmDialog(null,
                    "Die aktuelle Simulationsberechnung wird hierdurch verworfen.\n"
                            + "Sind Sie sicher, dass Sie fortfahren möchten?", "Aktuelle Simulation verwerfen?",
                    JOptionPane.YES_NO_OPTION);
            if (sel == JOptionPane.YES_OPTION) {
                this.playbackHandler.startNewSimulation(simulationOptionsFrame.getSelectionPlayersPerTeam(),
                        simulationOptionsFrame.getSelectionSimulationDuration(),
                        simulationOptionsFrame.getSelectionTeamA_Path(),
                        simulationOptionsFrame.getSelectionTeamB_Path(),
                        simulationOptionsFrame.getSelectionSimulationOptions());
            }
        } catch (AiLoadingException e1) {
            JOptionPane.showMessageDialog(null, NewSimulation.AI_LOADING_ERROR_TEXT + e1.getMessage(),
                    NewSimulation.AI_LOADING_ERROR_TITLE, JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
