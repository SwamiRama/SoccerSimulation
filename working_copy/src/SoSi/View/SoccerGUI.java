package SoSi.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import SoSi.ControllerPkg.Controller;
import SoSi.Model.PlaybackHandler;
import SoSi.Model.SoccerUpdateEvent;
import SoSi.Model.TickData;

/**
 * Die SoccerGui ist das Hauptfenster, das die Darstellung, das Menü und die Steuerelemente enthält. Die Klasse
 * beinhaltet außerdem einen Timer, welcher in einem festen Intervall den aktuellen Wiedergabezustand aus dem
 * PlaybackHandler abfragt und alle in der GUI enthaltenen Komponenten, welche die Simulation repräsentieren, neu
 * zeichnen lässt.
 */
public class SoccerGUI extends JFrame implements Observer, IRefreshable {

    /**
     * Random generated.
     */
    private static final long serialVersionUID = -6596462911946955929L;

    /**
     * Minimale Größe der GUI, wenn Steuerungselemente angezeigt werden sollen
     */
    private static final Dimension MINIMUM_SIZE_MAIN_GUI = new Dimension(1000, 730); // screen min. is 1024x768

    /**
     * Minimale Größe der GUI, wenn keine Steuerungselemente angezeigt werden sollen
     */
    private static final Dimension MINIMUM_SIZE_ADDITIONAL_GUI = new Dimension(1000, 680);

    /**
     * Standardwert für Balldurchmesser in Metern.
     */
    public static final double BALL_DIAMETER = 1.0;

    /**
     * Standardwert für Spieler-Durchmesser in Metern.
     */
    public static final double PLAYER_DIAMETER = 1.4;

    /**
     * Standardwert für Pfosten-Durchmesser in Metern.
     */
    public static final double POST_DIAMETER = 0.3;

    /**
     * Standardwert für Spielfeldlänge in Metern.
     */
    public static final double FIELD_LENGTH = 60;

    /**
     * Standardwert für Spielfeldbreite in Metern.
     */
    public static final double FIELD_WIDTH = 35;

    /**
     * Standardwert für Torbreite in Metern.
     */
    public static final double GOAL_SIZE = 5;

    /**
     * Standardwert für Anzahl der GUI-Zeichenvorgänge pro Sekunde während einer aktiven Wiedergabe.
     */
    public static final int GUI_REFRESH_RATE = 40;

    /**
     * Standardwert für den Titel des Darstellungsfensters.
     */
    public static final String GUI_DEFAULT_TITLE = "SoSi - Soccer Simulation";

    /**
     * Die Datei-Endung der Simulationsdatei.
     */
    public static final String SOSI_SIMULATION_FILE_EXTENSION = "sosi";

    /**
     * Referenz auf den Haupt-PlaybackHandler, über welchen Simulationen gestartet, geladen, gespeichert und die
     * Wiedergabe gesteuert werden kann. Zusätzliche gestartete Darstellungsfenster erhalten die selbe Referenz, um eine
     * synchrone Darstellung der selben Simulation in mehreren Darstellungsfenstern zu ermöglichen.
     * 
     * @see PlaybackHandler
     */
    private final PlaybackHandler playbackHandler;

    /**
     * Panel zur Darstellung der Simulation, in welchem das Spielfeld und sämtliche Simulationsobjekte gezeichnet
     * werden.
     * 
     * @see SoccerPanel
     */
    private SoccerPanel soccerPanel;

    /**
     * Referenz auf Panel, welches Buttons zur Steuerung der Wiedergabe, den Zeitleistenslider und den
     * Geschwindigkeitsslider enthält.
     * 
     * @see SoccerControlPanel
     */
    private SoccerControlPanel soccerControlPanel;

    /**
     * Referenz auf Menüleiste, welche sämtliche für das Programm relevante Steuerungskomponenten enthält.
     * 
     * @see SoccerMenuBar
     */
    private SoccerMenuBar soccerMenuBar;

    /**
     * Anzeige von aktuellen Wiedergabeinformationen (Tore, Dauer, Namen der antretenden KIs).
     * 
     * @see SoccerGameInformationPanel
     */
    private SoccerGameInformationPanel soccerGameInformationPanel;

    /**
     * Referenz auf {@link DebuggingFrame}, welches zur Anzeige von Debugging-Nachrichten dient.
     * 
     * @see DebuggingFrame
     */
    private final DebuggingFrame debuggingFrame;

    /**
     * Referenz auf den SoundHandler, um Sounds ausgeben zu können und diesen, falls gewünscht, deaktivieren zu können
     */
    private final SoundHandler soundHandler;

    /**
     * Flag, ob es sich bei der aktuellen Instanz um die Haupt-GUI handelt. Diese beinhaltet Steuerugnskomponenten und
     * die Menüleiste. Beim Schließen der Haupt-GUI werden ebenfalls alle anderen Fenster geschlossen und das Programm
     * beendet. (Falls noch eine Aktion wie das Speichern ausgeführt wird, beendet sich das Programm erst nach Abschluss
     * der jeweiligen Situation)
     */
    private final boolean isMainGui;

    /**
     * Flag, ob das aktuelle Fenster gerade beendet. <br>
     * Dies verhindert eine Endlosschleife, in welcher das Fenster stets versucht sich selbst mittels .dispose() zu
     * schließen.
     */
    private boolean isDisposing = false;

    /**
     * Referenz auf Controller, welcher ActionListener für sämtliche Benutzerinteraktionen erstellt und beim erstellen
     * der GUI zur Verfügung stellt.
     * 
     * @see Controller
     */
    private Controller controller;

    /**
     * Timer, welcher in einem festen, vorgegeben Intervall die Aktualisierung der GUI anstößt. Dabei wird aus
     * PlaybackHandler die Daten der aktuellen Wiedergabeposition abgefragt und das Neuzeichnen angestoßen.
     */
    private Timer updateGuiTimer;

    /**
     * Erstellt ein neues Hauptfenster. Der Aufruf wird an den überladenen Konstruktor weitergegeben, indem der
     * Parameter, dass Benutzercontrols angezeigt werden sollen, auf "true" gesetzt wird und eine SoccerPanel-Instanz
     * übergeben wird, welche den Programmstartbildschirm darstellt.
     * 
     * @param playbackHandler
     *            Referenz auf PlaybackHandler, welcher zur Abwicklung der Wiedergabe und starten von Simulationen
     *            dient.
     */
    public SoccerGUI(PlaybackHandler playbackHandler) {
        this(playbackHandler, true, new SoccerPanelLogo());
    }

    /**
     * Erstellt ein neues Darstellungsfenster. Wird der Parameter "showPlaybackControls" auf true gesetzt, so werden dem
     * Fenster Steuerungselemente hinzugefügt und das Fenster als Hauptfenster (s.u.) festgelegt. <br>
     * Als Hauptfenster wird das Fenster bezeichnet, welche sämtliche Benutzerkomponenten enthält und beim Schließen
     * auch alle weitere, ggf. geöffneten Fenster schließt.<br>
     * Der Parameter SoccerPanel dient zur Übergabe einer SoccerPanel-Instanz, welche unmittelbar nach dem Programmstart
     * dargestellt werden soll. Im Falle des erstellens eines Hauptfensters, sollte diese Instanz zur Darstellung des
     * Programmstartbildschirm dienen, jedoch muss dies nicht notwendigerweise der Fall sein. Im Falle des erstellen
     * eines zusätzlichen Darstellungsfensters, sollte die SoccerPanel Instanz die Logik zur Darstellung der Wiedergabe
     * enthalten.
     * 
     * 
     * @param playbackHandler
     *            Referenz auf PlaybackHandler, welcher zur Abwicklung der Wiedergabe und starten von Simulationen
     *            dient.
     * @param showPlaybackControls
     *            Boolean-Flag, ob dem Fenster Steuerungselemente hinzugefügt werden sollen und das Fenster als
     *            Hauptfenster (siehe Methodenbeschreibung) gestartet werden soll.
     * @param soccerPanel
     *            Referenz auf SoccerPanel Instanz, welche zur Anzeige des Startbildschirm und/oder zur Darstellung der
     *            Wiedergabe dient.
     */
    public SoccerGUI(final PlaybackHandler playbackHandler, boolean showPlaybackControls, SoccerPanel soccerPanel) {
        super(GUI_DEFAULT_TITLE);

        this.playbackHandler = playbackHandler;

        // create Controller and ActionListeners
        this.controller = new Controller(playbackHandler, new DisplayOptionsChangedHandler());

        // DebuggingFrame intialisieren
        this.debuggingFrame = new DebuggingFrame(playbackHandler, controller);
        this.playbackHandler.addObserver(this.debuggingFrame);
        controller.createOpenDebugingFrame(debuggingFrame);

        // Set layout of JFrame
        this.setLayout(new BorderLayout());

        // create gui components
        this.soccerGameInformationPanel = new SoccerGameInformationPanel();

        // Only add controls & SoundHandler if desired
        this.isMainGui = showPlaybackControls;
        if (showPlaybackControls) {
            this.soccerControlPanel = new SoccerControlPanel(playbackHandler, this.controller);
            this.soccerMenuBar = new SoccerMenuBar(playbackHandler, this.controller);

            // Add controls to JFrame
            this.setJMenuBar(this.soccerMenuBar);
            this.getContentPane().add(this.soccerControlPanel, BorderLayout.SOUTH);
            this.getContentPane().add(this.soccerGameInformationPanel, BorderLayout.NORTH);

            this.soundHandler = new SoundHandler(playbackHandler);
        } else {
            this.soundHandler = null;
        }

        // Add remaining controls to JFrame
        this.addSoccerPanel(soccerPanel);
        this.playbackHandler.addObserver(soccerPanel);

        // Exit application, if main-windows was closed. Else, just close the JFrame
        if (isMainGui) {
            this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            this.setMinimumSize(MINIMUM_SIZE_MAIN_GUI);
        } else {
            this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            this.setMinimumSize(MINIMUM_SIZE_ADDITIONAL_GUI);
        }

        initGuiUpdateTimer();

        this.playbackHandler.addObserver(this);
        this.playbackHandler.notifyCurrentState();

        this.setSize(this.getMinimumSize());
        this.setLocationByPlatform(true);

        if (this.playbackHandler.getSimulationTickPosition() >= 0)
            this.refresh();

        Thread.currentThread().setPriority(8);
    }

    /**
     * Initialisiert den Update-Timer der Gui. Dabei wird die bei einem Timer-Tick auszuführende Logik festgelegt und
     * das Intervall des Timers (mittels GUI_REFRESH_RATE) festgelegt. Der Timer startet, falls die Wiedergabe im
     * PlaybackHandler bereits gestartet wurde.
     */
    private void initGuiUpdateTimer() {
        int delay = RuntimeArguments.getRuntimeArguments().isFpsUnlimited() ? 0 : 1000 / GUI_REFRESH_RATE;

        this.updateGuiTimer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });

                // Timer stoppen, falls Wiedergabe pausiert und Simulationsberechnung (inzwischen) abgschlossen)
                if (!playbackHandler.isRunning() && playbackHandler.isSimulationCalculationFinished())
                    updateGuiTimer.stop();
            }
        });

        // Timer sofort starten, falls Wiedergabe bereits aktiv .
        // (wird benötigt, wenn ein zusätzliches Darstellungs während einer aktiver Wiedergabe gestartet wird)
        if (playbackHandler.isRunning())
            this.updateGuiTimer.start();
    }

    /**
     * Fügt eine SoccerPanel Instanz dem Fenster hinzu. Ist bereits eine (alte) Instanz im Fenster vorhanden, wird diese
     * zuvor entfernt und mit der neuen ausgetauscht.
     * 
     * @param soccerPanel
     *            Neue SoccerPanel-Instanz, welche ins Fenster eingefügt werden soll.
     */
    private void addSoccerPanel(SoccerPanel soccerPanel) {
        if (this.soccerPanel != null) {
            this.playbackHandler.deleteObserver(this.soccerPanel);
            this.soccerPanel.flushImageCache();
            this.remove(this.soccerPanel);
        }

        this.playbackHandler.addObserver(soccerPanel);
        this.soccerPanel = soccerPanel;
        this.getContentPane().add(soccerPanel, BorderLayout.CENTER);
    }

    /**
     * Methode von {@link IRefreshable}, welche durch einen Timer-Tick aufgerufen wird, um die Anzeige zu aktualisieren
     * und die aktuellen Daten der Wiedergabe anzuzeigen. <br>
     * Die Methode ruft wiederum die {@link IRefreshable}-Methoden der Komponenten auf, welche im Fenster enthalten
     * sind, damit diese ebenfalls aktualisiert werden.
     * 
     * {@inheritDoc}
     */
    public void refresh(TickData tickData, double playbackSpeed, int simulationTickPosition) {
        this.soccerPanel.refresh(tickData, playbackSpeed, simulationTickPosition);

        if (this.soccerGameInformationPanel != null) {
            this.soccerGameInformationPanel.refresh(tickData, playbackSpeed, simulationTickPosition);
        }

        if (this.soccerControlPanel != null) {
            this.soccerControlPanel.refresh(tickData, playbackSpeed, simulationTickPosition);
        }

        if (this.soccerMenuBar != null) {
            this.soccerMenuBar.refresh(tickData, playbackSpeed, simulationTickPosition);
        }

        // DebuggingFrame wird nur von Haupt-GUI aktualisiert.
        if (this.isMainGui) {
            this.debuggingFrame.refresh(tickData, playbackSpeed, simulationTickPosition);
        }
    }

    /**
     * Veranlasst eine Aktualisierung aller für die Anzeige relevanter GUI-Komponenten
     */
    private void refresh() {
        TickData tickData;

        if (RuntimeArguments.getRuntimeArguments().isInterpolationDeactivated())
            tickData = playbackHandler.getCurrentTickData();
        else
            tickData = playbackHandler.getInterpolatedCurrentTickData();

        if (tickData != null)
            refresh(tickData, playbackHandler.getPlaybackSpeedRate(), playbackHandler.getSimulationTickPosition());
    }

    @Override
    public void update(Observable o, Object arg) {
        switch ((SoccerUpdateEvent) arg) {
        case PLAYBACK_STARTED:
            this.updateGuiTimer.start();
            break;

        case PLAYBACK_PAUSED:
            if (this.playbackHandler.isSimulationCalculationFinished())
                this.updateGuiTimer.stop();
            this.refresh();
            break;

        case PLAYBACK_ABORTED:
            this.updateGuiTimer.stop();
            break;

        case PLAYBACK_STEP:
        case PLAYBACK_SPEED_CHANGED:
            this.refresh();
            break;

        case SIMULATION_FINISHED:
            if (!this.playbackHandler.isRunning())
                this.updateGuiTimer.stop();
            break;

        default:
            break;
        }
    }

    /**
     * Ermittelt die Wiedergabeposition in Sekunden, abhängig von TICKS_PER_SECOND (aus {@link PlaybackHandler}) und der
     * übergebenen Tickposition
     * 
     * @param tickPosition
     *            Wiedergabezeitpunkt in Ticks
     * @return Umgerechnete Wiedergabezeitpunkt als Sekundenzahl
     */
    public static int getTotalSecondsByTickposition(int tickPosition) {
        return (int) Math.round((float) tickPosition / PlaybackHandler.TICKS_PER_SECOND);
    }

    /**
     * Erstellt einen formatierten String, welcher den Wiedergabezeitpunkt im Format "Minuten:Sekunden" enthält mitts
     * der Methode getTotalSecondsByTickposition() und der übergebenen Tickposition
     * 
     * @param tickPosition
     *            Wiedergabezeitpunkt
     * @return Formatierter Wiedergabezeitpunkt im Format "Minuten:Sekunden"
     */
    public static String tickpositionToTimeString(int tickPosition) {
        int seconds = getTotalSecondsByTickposition(tickPosition);

        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    @Override
    public void dispose() {
        // Remove itself as observer & stop timers
        this.playbackHandler.deleteObserver(this);
        this.updateGuiTimer.stop();

        // If this is Main-GUI, close and quit everything (but do it only once)
        if (this.isMainGui && !this.isDisposing) {
            this.isDisposing = true;

            this.playbackHandler.abort();

            this.controller.getQuit().actionPerformed(null);
        }
        
        // Flush old image cache & observer from addiditional view frame (= not Main-GUI)
        if (!this.isMainGui) {
            this.soccerPanel.flushImageCache();
            this.playbackHandler.deleteObserver(this.soccerPanel);
        }

        super.dispose();
    }

    /**
     * Innere Klasse, welche {@link IDisplayOptionsChanged} implementiert und bei Optionsänderungen im Dialog
     * SimulationOptionsFrame aufgerufen wird. Dabei werden Werte wie die Namen der KIs, die Farben der Spieler und die
     * Darstellungsansicht aktualisiert.
     */
    private class DisplayOptionsChangedHandler implements IDisplayOptionsChanged {
        @Override
        public void changed(Color teamAColor, Color teamBColor, SoccerPanel soccerPanel, int simulationDuration,
                boolean soundsActivated) {
            addSoccerPanel(soccerPanel);

            soccerGameInformationPanel.setData(playbackHandler.getTeamAName(), playbackHandler.getTeamBName(),
                    teamAColor, teamBColor, simulationDuration);
            soccerControlPanel.setData(simulationDuration);

            if (soundHandler != null)
                soundHandler.setEnabled(soundsActivated);

            refresh();
        }
    }

}
