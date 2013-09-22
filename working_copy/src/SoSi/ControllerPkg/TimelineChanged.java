package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import SoSi.Model.PlaybackHandler;
import SoSi.View.SoccerTimeLine;

/**
 * Wird ausgeführt, wenn der Benutzer die Position des Reglerknopfes der Zeitleiste (= Wiedergabeposition) verändert.
 * Die Wiedergabeposition wird entsprechend gesetzt.
 * 
 * @see PlaybackHandler#jumpToTime(int)
 */
public class TimelineChanged extends ControllerAction implements ChangeListener {

    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public TimelineChanged(PlaybackHandler playbackHandler) {
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
        if (source instanceof SoccerTimeLine)
            // Only call jumpToTime if slider is changed by human
            if (((SoccerTimeLine) source).isChangedByUser()) {
                playbackHandler.jumpToTime(((JSlider) source).getValue());
            }
    }

}
