package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import SoSi.Model.PlaybackHandler;

/**
 * Dient dazu, die Wiedergabe der Simulation mit Rücklaufgeschwindigkeit zurücklaufen zu lassen. Durch mehrfaches
 * Aufrufen dieses ActionListeners kann die Rücklaufgeschwindigkeit auf ein Vielfaches der
 * Normalwiedergabegeschwindigkeit erhöht werden.
 * 
 * @see PlaybackHandler#rewind()
 */
public class RewindButton extends ControllerAction {

    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public RewindButton(PlaybackHandler playbackHandler) {
        super(playbackHandler);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.playbackHandler.rewind();
    }

}
