package SoSi.ControllerPkg;

import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import SoSi.Model.PlaybackHandler;
import SoSi.View.DebuggingFrame;
import SoSi.View.IDisplayOptionsChanged;
import SoSi.View.SimulationOptionsFrame;
import SoSi.View.SoccerGUI;

/**
 * Die Klasse erstellt die von der GUI benötigten ActionListener-Referenzen, welche bei einer Interaktion des Users
 * aufgerufen werden.
 */
public class Controller {
    // Simulation
    private ActionListener newSimulation;
    private ActionListener restartSimulation;
    private ActionListener saveSimulation;
    private ActionListener loadSimulation;
    private ActionListener quit;

    // Steuerung
    private ActionListener play;
    private ActionListener pause;
    private ActionListener stop;
    private ActionListener setSpeed;
    private ActionListener simulationStepBack;
    private ActionListener simulationStepForward;
    private ActionListener replay;
    private ActionListener jumpToTime;
    private ActionListener rewindButton;
    private ActionListener forwardButton;

    // Optionen
    private ActionListener simulationOptions;
    private ActionListener newViewFrame;
    private ActionListener openDebugingFrame;

    // Hilfe
    private ActionListener openHelp;
    private ActionListener about;

    // Slider-Events
    private ChangeListener timelineChanged;
    private ChangeListener speedSelectorChanged;

    /**
     * Von {@link FileFilter} geerbte Instanz, welche in den Dateidialogen zum Filtern von Dateien mit der korrekten
     * Endung.
     */
    public static final FileFilter SOSI_SIMULATION_FILE_FILTER = new FileFilter() {
        @Override
        public String getDescription() {
            return "SoSi-Simulation-File";
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = f.getName().substring(f.getName().lastIndexOf(".") + 1, f.getName().length());

            if (extension.equals(SoccerGUI.SOSI_SIMULATION_FILE_EXTENSION))
                return true;

            return false;
        }
    };

    /**
     * Erstellt eine neue Controller-Instanz, welche alle für das Programm benötigten ActionListener-Instanz erstellt.
     * Dabei werden je nach Art bzw. Funktionalität des ActionListeners die ggf. dafür benötigten Referenzen auf
     * Instanzen von PlaybackHandler und/oder IDisplayOptionsChanged übergeben.
     * 
     * @param playbackHandler
     *            Referenz auf {@link PlaybackHandler}. Dessen Methoden werden aufgerufen, wenn Änderungen an der
     *            Wiedergabe bzw. der Simulation vorgenommen werden sollen.
     * @param confirmationUpdateView
     *            Referenz auf innere Klasse der GUI, welche aufgerufen wird, wenn der Benutzer im Dialog Änderungen an
     *            den Optionen durchgeführt hat, welche eine Veränderung der GUI erfordern.
     * @see IDisplayOptionsChanged
     */
    public Controller(PlaybackHandler playbackHandler, IDisplayOptionsChanged confirmationUpdateView) {
        // SimulationOptionsFrame anlegen
        SimulationOptionsFrame optionsFrame = new SimulationOptionsFrame();

        // ActionListener erstellen
        this.newSimulation = new NewSimulation(playbackHandler, optionsFrame, confirmationUpdateView);
        this.restartSimulation = new RestartSimulation(playbackHandler, optionsFrame);
        this.saveSimulation = new SaveSimulation(playbackHandler);
        this.loadSimulation = new LoadSimulation(playbackHandler, optionsFrame, confirmationUpdateView);
        this.quit = new Quit();

        // Steuerung
        this.play = new Play(playbackHandler);
        this.pause = new Pause(playbackHandler);
        this.stop = new Stop(playbackHandler);
        this.setSpeed = new SetSpeed(playbackHandler);
        this.simulationStepBack = new SimulationStepBack(playbackHandler);
        this.simulationStepForward = new SimulationStepForward(playbackHandler);
        this.replay = new Replay(playbackHandler);
        this.jumpToTime = new JumpToTime(playbackHandler);
        this.rewindButton = new RewindButton(playbackHandler);
        this.forwardButton = new ForwardButton(playbackHandler);

        // Optionen
        this.simulationOptions = new SimulationOptions(playbackHandler, optionsFrame, confirmationUpdateView);
        this.newViewFrame = new NewViewFrame(playbackHandler, optionsFrame);

        // Hilfe
        this.openHelp = new OpenHelp();
        this.about = new About();

        // Slider-Events
        this.timelineChanged = new TimelineChanged(playbackHandler);
        this.speedSelectorChanged = new SpeedSelectorChanged(playbackHandler);
    }

    /**
     * Getter für NewSimulation-ActionListener.
     * 
     * @return NewSimulation-ActionListener Instanz.
     */
    public ActionListener getNewSimulation() {

        return newSimulation;
    }

    /**
     * Getter für SaveSimulation-ActionListener.
     * 
     * @return SaveSimulation-ActionListener Instanz.
     */
    public ActionListener getSaveSimulation() {

        return saveSimulation;
    }

    /**
     * Getter für LoadSimulation-ActionListener.
     * 
     * @return LoadSimulation-ActionListener Instanz.
     */
    public ActionListener getLoadSimulation() {

        return loadSimulation;
    }

    /**
     * Getter für Quit-ActionListener.
     * 
     * @return Quit-ActionListener Instanz.
     */
    public ActionListener getQuit() {

        return quit;
    }

    /**
     * Getter für Play-ActionListener.
     * 
     * @return Play-ActionListener Instanz.
     */
    public ActionListener getPlay() {

        return play;
    }

    /**
     * Getter für Pause-ActionListener.
     * 
     * @return Pause-ActionListener Instanz.
     */
    public ActionListener getPause() {

        return pause;
    }

    /**
     * Getter für Stop-ActionListener.
     * 
     * @return Stop-ActionListener Instanz.
     */
    public ActionListener getStop() {

        return stop;
    }

    /**
     * Getter für SetSpeed-ActionListener.
     * 
     * @return SetSpeed-ActionListener Instanz.
     */
    public ActionListener getSetSpeed() {

        return setSpeed;
    }

    /**
     * Getter für SimulationStepBack-ActionListener.
     * 
     * @return SimulationStepBack-ActionListener Instanz.
     */
    public ActionListener getSimulationStepBack() {

        return simulationStepBack;
    }

    /**
     * Getter für SimulationStepForward-ActionListener.
     * 
     * @return SimulationStepForward-ActionListener Instanz.
     */
    public ActionListener getSimulationStepForward() {

        return simulationStepForward;
    }

    /**
     * Getter für Replay-ActionListener.
     * 
     * @return Replay-ActionListener Instanz.
     */
    public ActionListener getReplay() {

        return replay;
    }

    /**
     * Getter für JumpToTime-ActionListener.
     * 
     * @return JumpToTime-ActionListener Instanz.
     */
    public ActionListener getJumpToTime() {

        return jumpToTime;
    }

    /**
     * Getter für Rewind-ActionListener.
     * 
     * @return Rewind-ActionListener Instanz.
     */
    public ActionListener getRewind() {

        return rewindButton;
    }

    /**
     * Getter für Forward-ActionListener.
     * 
     * @return Forward-ActionListener Instanz.
     */
    public ActionListener getForward() {

        return forwardButton;
    }

    /**
     * Getter für SimulationOptions-ActionListener.
     * 
     * @return SimulationOptions-ActionListener Instanz.
     */
    public ActionListener getSimulationOptions() {

        return simulationOptions;
    }

    /**
     * Getter für NewViewFrame-ActionListener.
     * 
     * @return NewViewFrame-ActionListener Instanz.
     */
    public ActionListener getNewViewFrame() {

        return newViewFrame;
    }

    /**
     * Getter für OpenHelp-ActionListener.
     * 
     * @return OpenHelp-ActionListener Instanz.
     */
    public ActionListener getOpenHelp() {

        return openHelp;
    }

    /**
     * Getter für About-ActionListener.
     * 
     * @return About-ActionListener Instanz.
     */
    public ActionListener getAbout() {

        return about;
    }

    /**
     * Getter für TimelineChanged-ChangedListener.
     * 
     * @return TimelineChanged-ChangedListener Instanz.
     */
    public ChangeListener getTimelineChanged() {
        return timelineChanged;
    }

    /**
     * Getter für SpeedSelectorChanged-ChangedListener.
     * 
     * @return SpeedSelectorChanged-ChangedListener Instanz.
     */
    public ChangeListener getSpeedSelectorChanged() {
        return speedSelectorChanged;
    }

    /**
     * Getter für RestartSimulation-ActionListener.
     * 
     * @return RestartSimulation-ActionListener Instanz.
     */
    public ActionListener getRestartSimulation() {
        return restartSimulation;
    }

    /**
     * Erstellt einen neue {@link OpenDebuggingFrame}-Instanz, mit einer Referenz auf {@link DebuggingFrame}, welches
     * bei Aufruf angezeigt werden soll. <br>
     * Die Methode ist zusätzlich von Nöten, da der Konstuktor von {@link DebuggingFrame} von Controller abhängig ist
     * und daher zuerst der Controller erstellt werden muss. Die Übergabe einer {@link DebuggingFrame} Instanz als
     * Parameter des Controller-Konstruktors ist aus diesem Grund nicht möglich.
     * 
     * @param debuggingFrame
     *            Referenz auf Instanz von {@link DebuggingFrame} zur Anzeige von Debuggingnachrichten.
     */
    public void createOpenDebugingFrame(DebuggingFrame debuggingFrame) {
        this.openDebugingFrame = new OpenDebuggingFrame(debuggingFrame);
    }

    /**
     * Getter für OpenDebuggingFrame-ActionListener. Liefert null, falls
     * {@link #createOpenDebugingFrame(DebuggingFrame)} zuvor nicht aufgerufen wurde.
     * 
     * @return OpenDebuggingFrame-ActionListener Instanz.
     */
    public ActionListener getOpenDebugingFrame() {
        if (this.openDebugingFrame == null)
            throw new IllegalStateException("Es wurde kein ActionListener für OpenDebugingFrame angelegt. "
                    + "Dieser wird durch createOpenDebugingFrame() erstellt.");

        return this.openDebugingFrame;
    }

}
