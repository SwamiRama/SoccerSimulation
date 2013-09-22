package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import SoSi.Model.PlaybackHandler;

/**
 * Dient dazu, eine laufende Wiedergabe der Simulation zu pausieren.
 * 
 * @see PlaybackHandler#pause()
 */
public class Pause extends ControllerAction {

    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public Pause(PlaybackHandler playbackHandler) {
        super(playbackHandler);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.playbackHandler.pause();
    }
    
}
