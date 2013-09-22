package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import SoSi.Model.IProgressInformation;
import SoSi.Model.PlaybackHandler;
import SoSi.Model.Calculation.DataHandler;
import SoSi.Model.Calculation.DataHandler.SimulationSaveFileException;
import SoSi.View.SaveProgressDialog;
import SoSi.View.SoccerGUI;

/**
 * Dient dazu, die aktuelle Berechnung der Smulation abzuspeichern. Falls die Simulation noch nicht fertig berechnet
 * worden ist, ist diese Funktion deaktiviert.
 * 
 * @see PlaybackHandler#saveToFile(String)
 */
public class SaveSimulation extends ControllerAction {

    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public SaveSimulation(PlaybackHandler playbackHandler) {
        super(playbackHandler);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Create a file chooser
        final JFileChooser fc = new JFileChooser();

        fc.setFileFilter(Controller.SOSI_SIMULATION_FILE_FILTER);

        fc.setAcceptAllFileFilterUsed(false);

        int returnVal = fc.showSaveDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            String filePath = file.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith("." + SoccerGUI.SOSI_SIMULATION_FILE_EXTENSION))
                filePath += "." + SoccerGUI.SOSI_SIMULATION_FILE_EXTENSION;

            final String filePathToSave = filePath;
            File fileToSave = new File(filePathToSave);

            if (!fileToSave.exists()
                    || (JOptionPane.showConfirmDialog(null,
                            "Soll die bereits existierende Datei überschrieben werden?", "Datei ersetzen?",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)) {

                if (fileToSave.exists()) {
                    fileToSave.delete();
                }

                Thread fileSaveThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        IProgressInformation progressInformation = DataHandler.getNewSaveProgressInformation();
                        String filePath = filePathToSave;
                        SaveProgressDialog saveDialog = new SaveProgressDialog(progressInformation);
                        saveDialog.setVisible(true);
                        try {
                            playbackHandler.saveToFile(filePath);
                            saveDialog.setFinished(filePath);

                        } catch (SimulationSaveFileException e1) {
                            saveDialog.setFail(e1);
                        }
                    }
                });
                fileSaveThread.setPriority(Thread.MIN_PRIORITY);
                fileSaveThread.start();
            }
        }
    }
}
