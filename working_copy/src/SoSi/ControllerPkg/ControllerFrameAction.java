package SoSi.ControllerPkg;

import SoSi.Model.PlaybackHandler;
import SoSi.View.SimulationOptionsFrame;

/**
 * Dient zur Darstellung des erweiterten Dialogfensters und zur Entgegennahme, sowie Umsetzung der ausgewählten
 * Optionen. Wird bei einer entsprechenden Interaktion des Users aufgerufen, bei welcher zusätzlich eine Referenz auf
 * SimulationOptionsFrame zur Darstellung eines erweiterten Dialogs notwendig ist.
 */
public abstract class ControllerFrameAction extends ControllerAction {

	/**
	 * Referenz auf {@link SimulationOptionsFrame} zur Anzeige der Simulations- und Darstellungsoptionen.
	 */
	protected SimulationOptionsFrame simulationOptionsFrame;

	/**
	 * Erstellt eine neue "ControllerFrameAction", die eine Referenz auf den PlaybackHander und auf den
	 * SimulationOptionsFrame enthält. Eventuelle Änderungen, die in der GUI angegeben werden, werden aus dem
	 * SimulationOptionsFrame entgegen genommen und auf den PlaybackHandler angewandt.
	 * 
	 * @param playbackHandler
	 *            Referenz auf {@link PlaybackHandler}.
	 * @param simulationOptionsFrame
	 *            Referenz auf {@link SimulationOptionsFrame} zur Darstellung eines erweiterten Dialogs.
	 * 
	 * @see PlaybackHandler
	 * @see SimulationOptionsFrame
	 */
	public ControllerFrameAction(PlaybackHandler playbackHandler, SimulationOptionsFrame simulationOptionsFrame) {
		super(playbackHandler);
		this.simulationOptionsFrame = simulationOptionsFrame;
	}

}
