package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import SoSi.Model.PlaybackHandler;

/**
 * Dient dazu, in der Wiedergabe einen Simulationsschritt vorzuspringen.
 * 
 * @see PlaybackHandler#simulationStepForward()
 */
public class SimulationStepForward extends ControllerAction {

    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public SimulationStepForward(PlaybackHandler playbackHandler) {
        super(playbackHandler);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.playbackHandler.simulationStepForward();
    }

}
