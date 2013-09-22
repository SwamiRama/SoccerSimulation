package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import SoSi.Model.PlaybackHandler;
import SoSi.Model.TickData;
import SoSi.Model.Calculation.DataHandler.SimulationSaveFileException;
import SoSi.View.IDisplayOptionsChanged;
import SoSi.View.SimulationOptionsFrame;

/**
 * Öffnet ein Dateiauswahl-Dialogfenster, mit welchem der Benutzer eine gespeicherte Simulation auswählen kann. Ist der
 * Pfad gültig, die Datei lesbar und der Inhalt konform, so erfolgt das Laden der Simulation. Ansonsten erfolgt eine
 * Fehlermeldung.
 * 
 * @see PlaybackHandler#loadFromFile(String)
 */
public class LoadSimulation extends GuiCallbackDialog {

    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public LoadSimulation(PlaybackHandler playbackHandler, SimulationOptionsFrame simulationOptionsFrame,
            IDisplayOptionsChanged confirmationUpdateView) {
        super(playbackHandler, simulationOptionsFrame, confirmationUpdateView);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Create a file chooser
        final JFileChooser fc = new JFileChooser();

        fc.setFileFilter(Controller.SOSI_SIMULATION_FILE_FILTER);
        fc.setFileSelectionMode(JFileChooser.OPEN_DIALOG);

        fc.setAcceptAllFileFilterUsed(false);

        int returnVal = fc.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();

            if (!file.exists()) {
                JOptionPane.showMessageDialog(null, "Datei existiert nicht",
                        String.format("Die gewählte Datei \"%s\" existiert nicht!", file.getAbsolutePath()),
                        JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    playbackHandler.loadFromFile(file.getAbsolutePath());

                    // Update GUI:
                    int simulationDuration = playbackHandler.getGameInformation().getMaximumTickNumber();
                    playbackHandler.pause();
                    playbackHandler.jumpToTime(0);

                    confirmationUpdateView.changed(simulationOptionsFrame.getSelectionTeamAColor(),
                            simulationOptionsFrame.getSelectionTeamBColor(),
                            simulationOptionsFrame.getSelectionSoccerPanel(false), simulationDuration,
                            simulationOptionsFrame.getSelectionSoundsActivated());

                    simulationOptionsFrame.setSelectionSimulationDuration(simulationDuration);

                    TickData tickData = playbackHandler.getCurrentTickData();
                    if (tickData != null)
                        simulationOptionsFrame.setSelectionPlayersPerTeam(tickData.getPlayerPositionsTeamA().size());

                    playbackHandler.play();

                } catch (SimulationSaveFileException e1) {
                    String errorMessage = "Die Simulation konnte nicht geladen werden.";
                    if (!e1.getMessage().isEmpty())
                        errorMessage += "\nDetails: " + e1.getMessage();

                    if (e1.getCause() != null)
                        errorMessage += "\nAusgelöst durch:\n" + e1.getCause().getClass().getName();

                    JOptionPane.showMessageDialog(null, errorMessage, "Simulation konnte nicht geladen werden!",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

}
