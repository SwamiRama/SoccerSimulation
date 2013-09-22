package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import SoSi.Model.PlaybackHandler;
import SoSi.Model.Calculation.AILoader.AiLoadingException;
import SoSi.View.IDisplayOptionsChanged;
import SoSi.View.SimulationOptionsFrame;
import SoSi.View.SimulationOptionsFrameType;
import SoSi.View.SoccerPanel;

/**
 * Dient dazu, eine neue Simulation zu erstellen. Dabei öffnet sich ein Fenster mit diversen Einstellungsmöglichkeiten
 * für die neue Simulation.
 */
public class NewSimulation extends GuiCallbackDialog {

    /**
     * Fehlermeldung, die auftaucht, wenn eine KI nicht geladen werden kann oder etwas mit den Voraussetzungen für das
     * Laden der KIs nicht stimmt.
     */
    public static final String AI_LOADING_ERROR_TEXT = "Die Simulation konnte nicht gestartet werden, da mind. eine KI"
            + " nicht geladen werden konnte.\nDiese erfüllt scheinbar nicht die Voraussetzung oder es liegt keine "
            + "Leseberechtigung vor.\n\nFehlermeldung:\n";

    /**
     * Der Titel zur oben beschriebenen Fehlermeldung.
     */
    public static final String AI_LOADING_ERROR_TITLE = "Simulation konnte nicht gestartet werden";

    /**
     * Fehlermeldung, die auftaucht, wenn die SoccerPanel-Instanz nicht erstellt werden kann.
     */
    public static final String SOCCERPANEL_ERROR_TEXT = "Die Simulation konnte nicht gestartet werden, da die für die "
            + "Anzeige der Simulation \nnotwendigen Ressourcen nicht geladen werden konnten.";

    /**
     * Der Titel zur oben beschriebenen Fehlermeldung.
     */
    public static final String SOCCERPANEL__ERROR_TITLE = "Simulation konnte nicht gestartet werden";

    /**
     * @see GuiCallbackDialog#GuiCallbackDialog(PlaybackHandler, SimulationOptionsFrame, IDisplayOptionsChanged)
     */
    public NewSimulation(PlaybackHandler playbackHandler, SimulationOptionsFrame simulationOptionsFrame,
            IDisplayOptionsChanged confirmationUpdateView) {
        super(playbackHandler, simulationOptionsFrame, confirmationUpdateView);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        this.simulationOptionsFrame.show(SimulationOptionsFrameType.NEW_SIMULATION, new NewSimulationConfirmed(
                playbackHandler, simulationOptionsFrame, confirmationUpdateView));
    }

    /**
     * ActionListener, welcher ausgeführt wird, wenn der Benutzer das Erstellen einer neuen Simulation im Dialogfenster
     * bestätigt.
     */
    private class NewSimulationConfirmed extends GuiCallbackDialogConfirmed {

        /**
         * @see GuiCallbackDialogConfirmed#GuiCallbackDialogConfirmed(PlaybackHandler, SimulationOptionsFrame,
         *      IDisplayOptionsChanged)
         */
        public NewSimulationConfirmed(PlaybackHandler playbackHandler, SimulationOptionsFrame simulationOptionsFrame,
                IDisplayOptionsChanged confirmationUpdateView) {

            super(playbackHandler, simulationOptionsFrame, confirmationUpdateView);
            // do nothing else

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                SoccerPanel soccerPanel = simulationOptionsFrame.getSelectionSoccerPanel(false);

                try {
                    this.playbackHandler.startNewSimulation(simulationOptionsFrame.getSelectionPlayersPerTeam(),
                            simulationOptionsFrame.getSelectionSimulationDuration(),
                            simulationOptionsFrame.getSelectionTeamA_Path(),
                            simulationOptionsFrame.getSelectionTeamB_Path(),
                            simulationOptionsFrame.getSelectionSimulationOptions());

                    // Update GUI
                    super.actionPerformed(soccerPanel);
                } catch (AiLoadingException aiException) {
                    JOptionPane.showMessageDialog(null, AI_LOADING_ERROR_TEXT + aiException.getMessage(),
                            AI_LOADING_ERROR_TITLE, JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NullPointerException nullpException) {
                JOptionPane.showMessageDialog(null, SOCCERPANEL_ERROR_TEXT, SOCCERPANEL__ERROR_TITLE,
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

}
