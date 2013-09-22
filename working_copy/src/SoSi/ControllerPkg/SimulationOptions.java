package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;

import SoSi.Model.PlaybackHandler;
import SoSi.View.IDisplayOptionsChanged;
import SoSi.View.SimulationOptionsFrame;
import SoSi.View.SimulationOptionsFrameType;

/**
 * Dient zum starten eines zusätzlichen Dialogs, in welchem Optionen geändert werden können, welche während einer
 * aktuell laufenden Wiedergabe einer Simulation angepasst werden können. Dies sind z.B. die Darstellung des Spielfelds
 * und die Farben der Spieler.
 */
public class SimulationOptions extends GuiCallbackDialog {

	/**
	 * @see GuiCallbackDialog#GuiCallbackDialog(PlaybackHandler, SimulationOptionsFrame, IDisplayOptionsChanged)
	 */
	public SimulationOptions(PlaybackHandler playbackHandler, SimulationOptionsFrame simulationOptionsFrame,
			IDisplayOptionsChanged confirmationUpdateView) {

		super(playbackHandler, simulationOptionsFrame, confirmationUpdateView);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// e.g.:
		this.simulationOptionsFrame.show(SimulationOptionsFrameType.CHANGE_OPTIONS, new SimulationOptionsConfirmed(
				playbackHandler, simulationOptionsFrame, confirmationUpdateView));
	}

	/**
	 * Wird ausgeführt, wenn der Benutzer das Erstellen einer neuen Simulation bestätigt.
	 */
	private class SimulationOptionsConfirmed extends GuiCallbackDialogConfirmed {
		/**
		 * @see GuiCallbackDialogConfirmed#GuiCallbackDialogConfirmed(PlaybackHandler, SimulationOptionsFrame,
		 *      IDisplayOptionsChanged)
		 */
		public SimulationOptionsConfirmed(PlaybackHandler playbackHandler,
				SimulationOptionsFrame simulationOptionsFrame, IDisplayOptionsChanged confirmationUpdateView) {

			super(playbackHandler, simulationOptionsFrame, confirmationUpdateView);
			// do nothing else

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// Update GUI
			super.actionPerformed(simulationOptionsFrame.getSelectionSoccerPanel(false));
		}
	}

}
