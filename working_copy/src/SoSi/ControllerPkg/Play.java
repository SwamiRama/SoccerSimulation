package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import SoSi.Model.PlaybackHandler;

/**
 * Dient dazu, die Wiedergabe der Simulation zu starten bzw. eine pausierte Wiedergabe fortzusetzen.
 * 
 * @see PlaybackHandler#play()
 */
public class Play extends ControllerAction {

    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public Play(PlaybackHandler playbackHandler) {
        super(playbackHandler);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.playbackHandler.play();
    }
    
}
