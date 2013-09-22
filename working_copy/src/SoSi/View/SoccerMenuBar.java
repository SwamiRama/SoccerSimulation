package SoSi.View;

import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import SoSi.ControllerPkg.Controller;
import SoSi.Model.PlaybackHandler;
import SoSi.Model.SoccerUpdateEvent;
import SoSi.Model.TickData;

/**
 * Übernimmt die Darstellung und die Erstellung der Menüleiste. Enthält sämtliche für die Wiedergabe verfügbaren
 * Steuerungskomponenten sowie Komponenten zur Anzeige von Dialogen, zum Starten einer Simulation, zum Ändern von
 * Konfigurationsoptionen und zum laden bzw. speichern von Simulationen. Des weiteren wird das Anzeigen des Handbuchs
 * angeboten.
 */
public class SoccerMenuBar extends JMenuBar implements Observer, IRefreshable {

    /**
     * Random generated.
     */
    private static final long serialVersionUID = -790233127005474872L;

    /**
     * Repräsentiert den Menüpunkt "Simulation -> Neue Simulation starten".
     */
    private JMenuItem restartSimulationItem;

    /**
     * Repräsentiert den Menüpunkt "Simulation -> Simulation speichern".
     */
    private JMenuItem saveSimulationItem;

    /**
     * Repräsentiert den Menüpunkt "Optionen -> Spieleinstellungen".
     */
    private JMenuItem simulationOptionItem;

    /**
     * Repräsentiert den Menüpunkt "Steuerung -> Play".
     */
    private JMenuItem playItem;

    /**
     * Repräsentiert den Menüpunkt "Steuerung -> Pause".
     */
    private JMenuItem pauseItem;

    /**
     * Repräsentiert den Menüpunkt "Steuerung -> Stop".
     */
    private JMenuItem stopItem;

    /**
     * Repräsentiert den Menüpunkt "Steuerung -> Vorlauf".
     */
    private JMenuItem forwardItem;

    /**
     * Repräsentiert den Menüpunkt "Steuerung -> Rücklauf".
     */
    private JMenuItem rewindItem;

    /**
     * Repräsentiert den Menüpunkt "Steuerung -> Simulationsschritt vor".
     */
    private JMenuItem stepForwardItem;

    /**
     * Repräsentiert den Menüpunkt "Steuerung -> Simulationsschritt zurück".
     */
    private JMenuItem stepBackItem;

    /**
     * Repräsentiert den Menüpunkt "Steuerung -> Replay".
     */
    private JMenuItem replayItem;

    /**
     * Repräsentiert den Menüpunkt "Steuerung -> Zu Simulationszeitpunkt springen".
     */
    private JMenuItem jumpToTimeItem;

    /**
     * Untermenü für die Menüpunkte "Steuerung -> Vorlauf/Rücklauf" zur Auswahl der Geschwindigkeit.
     */
    private static double[] SPEED_SELECTIONS = { 0.25, 0.5, 0.75, 1, 1.5, 2, 5, 10 };

    /**
     * Erstellt eine neue SoccerMenuBar. Dabei werden sämtliche darin enthaltenen Menüpunkte erzeugt und dem Menü
     * hinzugefügt. <br>
     * Zur Aktualisierung der aktivierten und deaktivierten Menüpunkte registriert sich die Klasse beim PlaybackHandler
     * als Observer.<br>
     * Die zur Interaktion mit dem Benutzer notwendigen ActionListener werden mittels der Referenz auf Controller
     * abgefragt.
     * 
     * @param playbackHandler
     *            Referenz auf {@link PlaybackHandler}
     * @param controller
     *            Referenz auf {@link Controller}.
     */
    public SoccerMenuBar(PlaybackHandler playbackHandler, Controller controller) {
        playbackHandler.addObserver(this);

        JMenu simulation = new JMenu("Simulation");
        simulation.setMnemonic(KeyEvent.VK_S);
        addItemsOfSimulation(simulation, controller);

        JMenu control = new JMenu("Steuerung");
        control.setMnemonic(KeyEvent.VK_T);
        addItemsOfControl(control, controller);

        JMenu options = new JMenu("Optionen");
        options.setMnemonic(KeyEvent.VK_O);
        addItemsOfOptions(options, controller);

        JMenu help = new JMenu("Hilfe");
        help.setMnemonic(KeyEvent.VK_H);
        addItemsOfHelp(help, controller);

        this.add(simulation);
        this.add(control);
        this.add(options);
        this.add(help);
    }

    /**
     * Die Elemente des Menüpunktes "Simulation" werden hier eingefügt und mit dem Controller verknüpft.
     * 
     * @param menu
     *            Die Menübar, in die die Elemente eingefügt werden sollen.
     * @param controller
     *            Referenz auf {@link Controller}.
     */
    private void addItemsOfSimulation(JMenu menu, Controller controller) {
        JMenuItem item;

        item = new JMenuItem("Neue Simulation erstellen");
        item.addActionListener(controller.getNewSimulation());
        item.setMnemonic(KeyEvent.VK_N);
        menu.add(item);

        item = new JMenuItem("Simulation erneut durchführen");
        item.addActionListener(controller.getRestartSimulation());
        item.setMnemonic(KeyEvent.VK_E);
        item.setEnabled(false);
        this.restartSimulationItem = item;
        menu.add(item);

        menu.add(new JSeparator());

        item = new JMenuItem("Simulation laden");
        item.addActionListener(controller.getLoadSimulation());
        item.setMnemonic(KeyEvent.VK_L);
        menu.add(item);

        item = new JMenuItem("Simulation speichern");
        item.addActionListener(controller.getSaveSimulation());
        item.setMnemonic(KeyEvent.VK_S);
        this.saveSimulationItem = item;
        menu.add(item);

        menu.add(new JSeparator());

        item = new JMenuItem("Beenden");
        item.addActionListener(controller.getQuit());
        item.setMnemonic(KeyEvent.VK_B);
        menu.add(item);
    }

    /**
     * Die Elemente des Menüpunktes "Optionen" werden hier eingefügt und mit dem Controller verknüpft.
     * 
     * @param menu
     *            Die Menübar, in die die Elemente eingefügt werden sollen.
     * @param controller
     *            Referenz auf {@link Controller}
     */
    private void addItemsOfOptions(JMenu menu, Controller controller) {
        JMenuItem item;

        item = new JMenuItem("Darstellungsoptionen");
        item.addActionListener(controller.getSimulationOptions());
        item.setMnemonic(KeyEvent.VK_D);
        this.simulationOptionItem = item;
        menu.add(item);

        item = new JMenuItem("Entwicklerkonsole öffnen");
        item.addActionListener(controller.getOpenDebugingFrame());
        item.setMnemonic(KeyEvent.VK_E);
        menu.add(item);

        item = new JMenuItem("Neues Darstellungsfenster");
        item.addActionListener(controller.getNewViewFrame());
        item.setMnemonic(KeyEvent.VK_N);
        menu.add(item);
    }

    /**
     * Die Elemente des Menüpunktes "Hilfe" werden hier eingefügt und mit dem Controller verknüpft.
     * 
     * @param menu
     *            Die Menübar, in die die Elemente eingefügt werden sollen.
     * @param controller
     *            Referenz auf {@link Controller}.
     */
    private void addItemsOfHelp(JMenu menu, Controller controller) {
        JMenuItem item;

        item = new JMenuItem("Benutzerhandbuch");
        item.addActionListener(controller.getOpenHelp());
        item.setMnemonic(KeyEvent.VK_H);
        menu.add(item);

        item = new JMenuItem("Über");
        item.addActionListener(controller.getAbout());
        item.setMnemonic(KeyEvent.VK_B);
        menu.add(item);
    }

    /**
     * Die Elemente des Menüpunktes "Hilfe" werden hier eingefügt und mit dem Controller verknüpft.
     * 
     * @param menu
     *            Die Menübar, in die die Elemente eingefügt werden sollen.
     * @param controller
     *            Referenz auf {@link Controller}.
     */
    private void addItemsOfControl(JMenu menu, Controller controller) {
        JMenuItem item;

        item = new JMenuItem("Play");
        item.addActionListener(controller.getPlay());
        item.setMnemonic(KeyEvent.VK_P);
        this.playItem = item;
        menu.add(item);

        item = new JMenuItem("Pause");
        item.addActionListener(controller.getPause());
        item.setMnemonic(KeyEvent.VK_P);
        this.pauseItem = item;
        menu.add(item);

        item = new JMenuItem("Stop");
        item.addActionListener(controller.getStop());
        item.setMnemonic(KeyEvent.VK_S);
        this.stopItem = item;
        menu.add(item);

        menu.add(new JSeparator());

        JMenu submenu = new JMenu("Rücklauf");
        submenu.setMnemonic(KeyEvent.VK_C);
        addSpeedsToSubmenu(submenu, controller, false);
        this.rewindItem = submenu;
        menu.add(submenu);

        submenu = new JMenu("Vorlauf");
        submenu.setMnemonic(KeyEvent.VK_V);
        addSpeedsToSubmenu(submenu, controller, true);
        this.forwardItem = submenu;
        menu.add(submenu);

        menu.add(new JSeparator());

        item = new JMenuItem("Simulationsschritt zurück");
        item.addActionListener(controller.getSimulationStepBack());
        item.setMnemonic(KeyEvent.VK_U);
        this.stepBackItem = item;
        menu.add(item);

        item = new JMenuItem("Simulationsschritt vor");
        item.addActionListener(controller.getSimulationStepForward());
        item.setMnemonic(KeyEvent.VK_O);
        this.stepForwardItem = item;
        menu.add(item);

        menu.add(new JSeparator());

        item = new JMenuItem("Replay");
        item.addActionListener(controller.getReplay());
        item.setMnemonic(KeyEvent.VK_R);
        this.replayItem = item;
        menu.add(item);

        item = new JMenuItem("Zu Zeitpunkt springen");
        item.addActionListener(controller.getJumpToTime());
        item.setMnemonic(KeyEvent.VK_Z);
        this.jumpToTimeItem = item;
        menu.add(item);

    }

    /**
     * Die Elemente des Untermenüpunktes "Steuerung -> Vorlauf/Rücklauf" werden hier eingefügt und mit dem Controller
     * verknüpft.
     * 
     * @param menu
     *            Die Menübar, in die die Elemente eingefügt werden sollen.
     * @param controller
     *            Referenz auf {@link Controller}.
     * @param positiveValues
     *            Boolean-Wert, ob die Werte positiv oder negativ sind, um zu bestimmen, ob der Menüpunkt zum Vor- oder
     *            Rücklauf dient.
     */
    private void addSpeedsToSubmenu(JMenu menu, Controller controller, boolean positiveValues) {
        double multiplicator = positiveValues ? 1 : -1;

        for (double value : SPEED_SELECTIONS) {
            JMenuItem item = new JMenuItem(String.format("%.2f x", value));
            item.setActionCommand(Double.toString(value * multiplicator));
            item.addActionListener(controller.getSetSpeed());
            menu.add(item);
        }
    }

    @Override
    public void update(Observable o, Object data) {
        if (data instanceof SoccerUpdateEvent) {
            switch ((SoccerUpdateEvent) data) {
            case SIMULATION_FINISHED:
                this.saveSimulationItem.setEnabled(true);
                break;

            case PLAYBACK_ABORTED:
                this.saveSimulationItem.setEnabled(false);
                setPlaybackEnabled(false);
                break;

            case PLAYBACK_STARTED:
                setPlaybackEnabled(true);
                this.restartSimulationItem.setEnabled(true);
                this.playItem.setEnabled(false);
                this.pauseItem.setEnabled(true);
                break;
                
            case PLAYBACK_PAUSED:
                this.playItem.setEnabled(true);
                this.pauseItem.setEnabled(false);
                break;

            default:
                break;
            }
        }
    }

    private void setPlaybackEnabled(boolean enabled) {
        this.playItem.setEnabled(enabled);
        this.pauseItem.setEnabled(enabled);
        this.stopItem.setEnabled(enabled);
        this.forwardItem.setEnabled(enabled);
        this.rewindItem.setEnabled(enabled);
        this.stepBackItem.setEnabled(enabled);
        this.stepForwardItem.setEnabled(enabled);
        this.jumpToTimeItem.setEnabled(enabled);
        this.simulationOptionItem.setEnabled(enabled);
        this.replayItem.setEnabled(enabled);
    }

    @Override
    public void refresh(TickData tickData, double playbackSpeed, int simulationTickPosition) {
        
        
        boolean stepBackEnabled = this.stepBackItem.isEnabled();
        boolean stepForwardEnabled = this.stepForwardItem.isEnabled();
        boolean replayButtonEnabled = this.replayItem.isEnabled();
        int tickPosition = tickData.getTickPosition();

        // if ((tickPosition == 0 && rewindEnabled) || (tickPosition > 0 && !rewindEnabled)) {
        if (tickPosition == 0 ^ !stepBackEnabled) {
            this.stepBackItem.setEnabled(tickPosition > 0);
            this.rewindItem.setEnabled(tickPosition > 0);
        }

        if (tickPosition >= simulationTickPosition ^ !stepForwardEnabled) {
            this.stepForwardItem.setEnabled(tickPosition < simulationTickPosition);
            this.forwardItem.setEnabled(tickPosition < simulationTickPosition);
        }

        if ((tickPosition == 0 || playbackSpeed < 0) ^ !replayButtonEnabled) {
            this.replayItem.setEnabled(tickPosition > 0 && playbackSpeed > 0);
        }
    }
}
