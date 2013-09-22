package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import SoSi.Model.PlaybackHandler;
import SoSi.View.SoccerControlPanel;

/**
 * Wird ausgeführt, wenn der Reglerknopf des Wiedergabegeschwindigkeitsregler geändert wird. Dabei wird die
 * Wiedergabegeschwindigkeit entsprechend der Auswahl im Slider gesetzt.
 * 
 * @see PlaybackHandler#setPlaybackSpeedRate(double)
 */
public class SpeedSelectorChanged extends ControllerAction implements ChangeListener {

    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public SpeedSelectorChanged(PlaybackHandler playbackHandler) {
        super(playbackHandler);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.changed(e.getSource());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        this.changed(e.getSource());
    }

    /**
     * Aktualisiert die Wiedergabeposition, wenn der Slider vom Benutzer verschoben wird.
     * 
     * @param source
     *            Das Objekt aus dem der neue Wert ausgelesen wird.
     */
    private void changed(Object source) {
        if (source instanceof JSlider) {
            int value = ((JSlider) source).getValue();
            double speed = SoccerControlPanel.convertSpeedSliderValueToSpeedRate(value);

            playbackHandler.setPlaybackSpeedRate(Math.signum(playbackHandler.getPlaybackSpeedRate()) * speed);
        }
    }
}
