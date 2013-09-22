package SoSi.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import SoSi.Model.IProgressInformation;
import SoSi.Model.Calculation.DataHandler.SimulationSaveFileException;

/**
 * Zeigt den aktuellen Speicherfortschritt an.
 */
public class SaveProgressDialog extends JDialog {

    /**
     * Random generated.
     */
    private static final long serialVersionUID = -5200007803816992909L;

    private boolean isInProgress;

    private final JPanel confirmButtonPanel;

    private final JEditorPane informationText;

    private final JProgressBar workInProgress;

    private final Timer updateTimer;

    /**
     * Erstellt eine neue Dialog-Instanz, um über den Speicherfortschritt zu informieren.
     * 
     * @param progressInformation
     *            Instanz mit Speicherfortschrittsinformationen
     * @see IProgressInformation
     */
    public SaveProgressDialog(final IProgressInformation progressInformation) {
        super();
        this.isInProgress = true;

        this.setTitle("Die Simulation wird gespeichert");

        this.setResizable(false);

        workInProgress = new JProgressBar();
        workInProgress.setPreferredSize(new Dimension(400, 20));
        workInProgress.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        workInProgress.setStringPainted(true);

        this.updateTimer = new Timer(150, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Fortschritt aktualisieren
                workInProgress.setValue(progressInformation.getProgress());
            }
        });

        informationText = new JEditorPane();
        informationText.setContentType("text/html");
        informationText.setOpaque(false);
        // informationText.setLineWrap(true);
        informationText.setText("Simulation wird gespeichert...");
        informationText.setEditable(false);
        informationText.setFocusable(false);
        
        
        JPanel progressBarPanel = new JPanel();
        progressBarPanel.setLayout(new BorderLayout());
        progressBarPanel.add(workInProgress);
        progressBarPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        messagePanel.add(informationText);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        JButton confirmButton = new JButton("OK");
        confirmButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmButton.addActionListener(new ConfirmationListener());
        
        confirmButtonPanel = new JPanel();
        confirmButtonPanel.setLayout(new BoxLayout(confirmButtonPanel, BoxLayout.PAGE_AXIS));
        confirmButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        confirmButtonPanel.add(confirmButton);
        confirmButtonPanel.setVisible(false);

        this.getRootPane().setDefaultButton(confirmButton);

        this.add(progressBarPanel, BorderLayout.NORTH);
        this.add(messagePanel, BorderLayout.CENTER);
        this.add(confirmButtonPanel, BorderLayout.SOUTH);

        // Falls Anzeige vom Benutzer geschlossen wurde und Speichern noch nicht abgeschlossen wurde, User über das
        // Speichern im Hintergrund informieren
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isInProgress) {
                    disposeWithNotfification();
                } else {
                    dispose();
                }
            }
        });
        this.pack();

        this.setLocationRelativeTo(null);
    }

    @Override
    public void setVisible(boolean arg0) {
        super.setVisible(arg0);
        this.updateTimer.start();
    }

    /**
     * Beendet das Fenster, ohne einen Hinweis anzuzeigen
     */
    public void disposeWithNotfification() {
        this.dispose();
        String title = "Simulation wird gespeichert";
        String message = "Achtung! Das Speichern der Simulation erfolgt im Hintergrund weiterhin.";
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Setzt die Fortschrittsanzeige auf "abgeschlossen", zeigt den Pfad der Datei an und zeigt einen Button zum
     * Schließen des Dialogs an.<br>
     * Falls das Dialogfenster bereits geschlossen wurde, erscheint ein PopUp-Dialog um über den Abschluss des
     * Speichervorgangs zu informieren.
     * 
     * @param filePath
     *            Dateipfad, unter welchem die Datei vollständig gespeichert wurde.
     */
    public void setFinished(String filePath) {

        String message = "Die Simulation wurde erfolgreich unter folgendem Pfad gespeichert:<br>" + filePath;
        message = "<html>" + message + "</html>";
        this.updateTimer.stop();

        if (this.isVisible()) {
            this.isInProgress = false;

            this.informationText.setText(message);
            this.workInProgress.setValue(workInProgress.getMaximum());
            this.confirmButtonPanel.setVisible(true);
            this.pack();
        } else {
            String title = "Speichern abgeschlosen";

            JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Setzt den Status, dass ein Fehler aufgetreten ist.
     */
    public void setFail(SimulationSaveFileException e1) {
        this.updateTimer.stop();
        this.isInProgress = false;
        String message = "Die Simulation konnte nicht gespeichert werden!";

        if (!e1.getMessage().isEmpty())
            message += "\nDetails: " + e1.getMessage();

        if (e1.getCause() != null) {
            message += "\nAusgelöst durch:\n" + format(e1.getCause().getMessage(), 65);
        }

        if (this.isVisible()) {
            message = String.format("<html>%s</html>", message.replaceAll("\n", "<br>"));
            this.informationText.setText(message);
            this.confirmButtonPanel.setVisible(true);
            this.pack();
        } else {
            String title = "Simulation konnte nicht gespeichert werden!";
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
        }
    }

    private String format(String plainString, int maxCharsPerLine) {
        int lastBreakPoint = -1;
        for (int counter = 0; counter < plainString.length() && counter < maxCharsPerLine; ++counter) {
            if (plainString.charAt(counter) == ' ') {
                lastBreakPoint = counter;
            }
            if (counter == plainString.length() - 1) {
                lastBreakPoint = -1;
            }
        }
        if (lastBreakPoint != -1) {
            StringBuffer strBuffer = new StringBuffer(plainString);
            strBuffer.insert(lastBreakPoint + 1, '\n');
            return strBuffer.substring(0, lastBreakPoint + 1)
                    + format(strBuffer.substring(lastBreakPoint + 1), maxCharsPerLine);

        } else {
            return plainString;
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        this.updateTimer.stop();
    }

    private class ConfirmationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isInProgress) {
                disposeWithNotfification();
            } else {
                dispose();
            }
        }
    }
}
