package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import SoSi.Model.PlaybackHandler;

/**
 * Dient dazu, die Wiedergabe der Simulation mit Vorlaufgeschwindigkeit vorlaufen zu lassen. Durch mehrfaches Aufrufen
 * dieses ActionListeners kann die Vorlaufgeschwindigkeit um ein Vielfaches der Normalwiedergabegeschwindigkeit erhöht
 * werden.
 * 
 * @see PlaybackHandler#forward()
 */
public class ForwardButton extends ControllerAction {
    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public ForwardButton(PlaybackHandler playbackHandler) {
        super(playbackHandler);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.playbackHandler.forward();
    }
    
}
