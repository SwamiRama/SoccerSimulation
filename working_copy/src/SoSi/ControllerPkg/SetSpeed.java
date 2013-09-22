package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import SoSi.Model.PlaybackHandler;

/**
 * Dient dazu, die Wiedergabegeschwindigkeit über das Menü zu erhöhen. Dabei kann der Benutzer einen diskreten Wert
 * wählen, welcher anschließend gesetzt wird.<br>
 * Der Wert für die Geschwindigkeit wird aus dem ActionCommand des JMenuItem entnommen.
 * 
 * @see PlaybackHandler#setPlaybackSpeedRate(double)
 */
public class SetSpeed extends ControllerAction {

    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public SetSpeed(PlaybackHandler playbackHandler) {
        super(playbackHandler);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String speed_str = e.getActionCommand();

        try {
            // try to convert ActionCommand to double
            double speed_value = Double.parseDouble(speed_str);

            // set parsed double as speed
            this.playbackHandler.setPlaybackSpeedRate(speed_value);
            
            if (!this.playbackHandler.isRunning())
                this.playbackHandler.play();
            
        } catch (NumberFormatException nfex) {
            System.out.println("Invalid String for speed-value");
        }
    }

}
