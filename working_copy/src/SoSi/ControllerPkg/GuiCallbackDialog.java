package SoSi.ControllerPkg;

import SoSi.Model.PlaybackHandler;
import SoSi.View.IDisplayOptionsChanged;
import SoSi.View.SimulationOptionsFrame;
import SoSi.View.SoccerGUI;
import SoSi.View.SoccerPanel;

/**
 * Dient zur Darstellung des erweiterten Dialogfensters und zur Entgegennahme sowie Umsetzung der ausgewählten Optionen,
 * wobei eine Rückmeldung an die GUI notwendig ist.
 */
public abstract class GuiCallbackDialog extends ControllerFrameAction {

    /**
     * Referenz auf innere Klasse der GUI, welche aufgerufen wird, wenn der Benutzer im Dialog Änderungen an den
     * Optionen durchgeführt hat, welche eine Veränderung der GUI erfordern.
     * 
     * @see IDisplayOptionsChanged
     */
    protected IDisplayOptionsChanged confirmationUpdateView;

    /**
     * Erstellt einen neuen GuiCallbackDialog-Instanz, welche eine Referenz auf den {@link PlaybackHandler},
     * SimulationOptionsFrame und ConfirmationUpdateView besitzt. Diese dienen zur Interaktion mit
     * {@link PlaybackHandler} bei Änderungen an der Wiedergabe bzw. der Simulation, zum Anzeigen des Dialogs und zur
     * Rückmeldung an die GUI, wenn Optionen geändert wurden, welche eine Änderung der GUI erfordern.
     * 
     * @param playbackHandler
     *            Referenz auf den {@link PlaybackHandler}.
     * @param simulationOptionsFrame
     *            Referenz auf {@link SimulationOptionsFrame} zur Anzeige von Optionen.
     * @param confirmationUpdateView
     *            Referenz auf die innere Klasse DisplayOptionsChangedHandler der Klasse {@link SoccerGUI}.
     * 
     * @see SimulationOptionsFrame
     * @see IDisplayOptionsChanged
     */
    public GuiCallbackDialog(PlaybackHandler playbackHandler, SimulationOptionsFrame simulationOptionsFrame,
            IDisplayOptionsChanged confirmationUpdateView) {

        super(playbackHandler, simulationOptionsFrame);
        this.confirmationUpdateView = confirmationUpdateView;

    }

    /**
     * ActionListener-Instanz, welche beim Bestätigen des Dialogfensters ausgeführt wird und die GUI über die für sie
     * relevanten erfolgten Optionsänderungen informiert.
     */
    protected abstract class GuiCallbackDialogConfirmed extends ControllerFrameAction {

        /**
         * Referenz auf innere Klasse der GUI, welche aufgerufen wird, wenn der Benutzer im Dialog Änderungen an den
         * Optionen durchgeführt hat, welche eine Veränderung der GUI erfordern.
         * 
         * @see IDisplayOptionsChanged
         */
        private IDisplayOptionsChanged confirmationUpdateView;

        /**
         * Erstellt einen neuen GuiCallbackDialog-Instanz, welche eine Referenz auf den {@link PlaybackHandler},
         * SimulationOptionsFrame und ConfirmationUpdateView besitzt. Diese dienen zur Interaktion mit
         * {@link PlaybackHandler} bei Änderungen an der Wiedergabe bzw. der Simulation, zum Anzeigen des Dialogs und
         * zur Rückmeldung an die GUI, wenn Optionen geändert wurden, welche eine Änderung der GUI erfordern.
         * 
         * @param playbackHandler
         *            Referenz auf den {@link PlaybackHandler}.
         * @param simulationOptionsFrame
         *            Referenz auf {@link SimulationOptionsFrame} zur Anzeige von Optionen.
         * @param confirmationUpdateView
         *            Referenz auf die innere Klasse DisplayOptionsChangedHandler der Klasse {@link SoccerGUI}.
         * 
         * @see SimulationOptionsFrame
         * @see IDisplayOptionsChanged
         */
        public GuiCallbackDialogConfirmed(PlaybackHandler playbackHandler,
                SimulationOptionsFrame simulationOptionsFrame, IDisplayOptionsChanged confirmationUpdateView) {

            super(playbackHandler, simulationOptionsFrame);
            this.confirmationUpdateView = confirmationUpdateView;

        }

        public void actionPerformed(SoccerPanel soccerPanel) {
            // Hide simulationOptionsFrame if action was successful (no exception occured)
            simulationOptionsFrame.setVisible(false);

            this.confirmationUpdateView.changed(simulationOptionsFrame.getSelectionTeamAColor(),
                    simulationOptionsFrame.getSelectionTeamBColor(), soccerPanel,
                    simulationOptionsFrame.getSelectionSimulationDuration(),
                    simulationOptionsFrame.getSelectionSoundsActivated());
        }
    }

}
