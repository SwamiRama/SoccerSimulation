package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import SoSi.Model.PlaybackHandler;

/**
 * Dient dazu, ein bestimmtes Zeitintervall von der aktuellen Wiedergabeposition erneut zu betrachten. Die
 * Wiedergabeposition springt dabei ein festgelegtes Zeitintervall zurück und führt die Wiedergabe ab dieser Position
 * fort.
 * 
 * @see PlaybackHandler#replay()
 */
public class Replay extends ControllerAction {

    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public Replay(PlaybackHandler playbackHandler) {
        super(playbackHandler);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.playbackHandler.replay();
    }

}
