package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import SoSi.Model.PlaybackHandler;

/**
 * Dient dazu, in der Wiedergabe einen Simulationsschritt zurückzuspringen.
 * 
 * @see PlaybackHandler#simulationStepBack()
 */
public class SimulationStepBack extends ControllerAction {

    /**
     * @see ControllerAction#ControllerAction(PlaybackHandler)
     */
    public SimulationStepBack(PlaybackHandler playbackHandler) {
        super(playbackHandler);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.playbackHandler.simulationStepBack();
    }

}
