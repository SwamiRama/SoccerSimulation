package SoSi.ControllerPkg;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * Dient dazu, eine PDF-Datei (das Benutzerhandbuch) zu öffnen und in einem entsprechenden PDF-Reader anzuzeigen.
 */
public class OpenHelp implements ActionListener {
    /**
     * Pfad zum Benutzerhandbuch
     */
    private static final String HELPFILE_PATH = "Hilfe/Benutzerhandbuch.pdf";

    @Override
    public void actionPerformed(ActionEvent e) {

        try {

            File pdfFile = new File(HELPFILE_PATH);
            if (pdfFile.exists()) {

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    System.out.println("Awt Desktop is not supported!");
                }

            } else {
                System.out.println("Helpfile does not exist!");
                JOptionPane
                        .showMessageDialog(
                                null,
                                String.format("Das Benutzerhandbuch konnte nicht gefunden werden.\nBitte stellen Sie "
                                        + "sicher, dass es unter folgendem Pfad vorhanden ist:\n%s",
                                        pdfFile.getAbsoluteFile()), "Benutzerhandbuch nicht gefunden",
                                JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Das Benutzerhandbuch konnte nicht geöffnet werden, da eine IOException aufgetreten ist. \n"
                            + "Bitte vergewissern Sie sich, dass sie über die benötigten Leserechte verfügen",
                    "Benutzerhandbuch konnte nicht geöffnet werden", JOptionPane.ERROR_MESSAGE);
        }
    }
}
