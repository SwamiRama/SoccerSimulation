package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import SoSi.Model.PlaybackHandler;

/**
 * Öffnet ein Dialogfenster, in welchem der Benutzer einen Wiedergabezeitpunkt im Format "Minuten:Sekunden" oder
 * "Sekunden" eingeben kann, zu welchem die Wiedergabeposition anschließend wechselt, sofern die eingegebene Zeit gültig
 * ist und im erlaubten Bereich liegt.
 * 
 * @see PlaybackHandler#jumpToTime(int)
 */
public class JumpToTime extends ControllerAction {

    /**
     * Beschreibung der erlaubten Eingabeformate
     */
    private static final String ALLOWED_FORMATS_DESCRIPTION = " - Sekunden \n - Minuten:Sekunden\n"
            + " - #Tickposition";

    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public JumpToTime(PlaybackHandler playbackHandler) {
        super(playbackHandler);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean showInputDialog = true;

        while (showInputDialog) {
            showInputDialog = false;
            try {
                String input = JOptionPane.showInputDialog(null,
                        "Bitte geben Sie die Wiedergabeposition in einem der \n" + "folgenden Formate ein:\n"
                                + ALLOWED_FORMATS_DESCRIPTION, "Zu Zeitpunkt springen", JOptionPane.QUESTION_MESSAGE);
                if (input == null) {

                } else {
                    String[] splitted = input.split(":");

                    Integer valueLeft = null;
                    Integer valueRight = null;

                    // Eingabe in Integer konvertieren
                    if (splitted.length >= 2) {
                        valueLeft = Integer.parseInt(splitted[0]);
                        valueRight = Integer.parseInt(splitted[1]);
                    } else {
                        String valueDiamondStripped = splitted[0].startsWith("#") ? splitted[0].substring(1)
                                : splitted[0];
                        valueRight = Integer.parseInt(valueDiamondStripped);
                    }

                    // Neue Position berechnen
                    int tickPosition = 0;

                    // Unterscheidung, ob Tick-Position oder Zeitangabe
                    if (input.startsWith("#")) {
                        tickPosition = valueRight;
                    } else {
                        if (valueLeft != null) {
                            // valueLeft beinhaltet Minuten
                            tickPosition += valueLeft * 60 * PlaybackHandler.TICKS_PER_SECOND;
                        }

                        // valueRight beinhaltet Sekunden
                        tickPosition += valueRight * PlaybackHandler.TICKS_PER_SECOND;
                    }

                    playbackHandler.jumpToTime(tickPosition);
                }
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(null,
                        "Sie haben einen ungültigen Wert eingegeben. Bitte geben Sie ausschließlich \n"
                                + "positive Ganzzahlen im Format \"X\" (X=Sekunden), \"X:Y\" "
                                + "(X=Minuten, Y=Sekunden) \noder \"#X\" (X=Tickposition) ein.", "Ungültige Eingabe",
                        JOptionPane.ERROR_MESSAGE);
                showInputDialog = true;
            }
        }

    }

}
