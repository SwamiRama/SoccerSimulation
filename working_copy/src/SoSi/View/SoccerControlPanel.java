package SoSi.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ToolTipManager;

import SoSi.ControllerPkg.Controller;
import SoSi.Model.PlaybackHandler;
import SoSi.Model.SoccerUpdateEvent;
import SoSi.Model.TickData;

/**
 * Dient sowohl zur Erstellung als auch zur Darstellung der Buttons in der Steuerelemente-Leiste.
 */
public class SoccerControlPanel extends JPanel implements IRefreshable, Observer {

    /**
     * Random generated.
     */
    private static final long serialVersionUID = 5652169364574095243L;

    /**
     * Referenz auf SoccerTimeLine, welche die Zeitleiste repräsentiert.
     * 
     * @see SoccerTimeLine
     */
    private final SoccerTimeLine soccerTimeLine = new SoccerTimeLine();

    private final JButton playButton;
    private final JButton pauseButton;
    private final JButton rewindButton;
    private final JButton forwardButton;
    private final JButton stepForwardButton;
    private final JButton stepBackwardButton;
    private final JButton stopButton;
    private final JButton replayButton;
    private final JSlider speedSlider;
    private final JLabel speedLabel;
    private final JLabel speedLabelCaption = new JLabel("Geschwindigkeit:");

    private final JPanel buttonPanel = new JPanel(new GridBagLayout());

    private final GridBagConstraints buttonConstraints = new GridBagConstraints();

    private double currentSpeedRate = 1.0;

    /**
     * Erstellt eine neue Instanz von SoccerControlPanel, die sämtliche darin enthaltenen Komponenten erstellt und
     * entsprechend hinzugefügt.<br>
     * Zur Aktualisierung der aktivierten und deaktivierten Menüpunkte, registriert sich die Klasse beim PlaybackHandler
     * als Observer.<br>
     * Die zur Interaktion mit dem Benutzer notwendigen ActionListener werden mittels der Referenz auf den Controller
     * abgefragt.
     * 
     * @param playbackHandler
     *            Referenz auf {@link PlaybackHandler}.
     * @param controller
     *            Referenz auf {@link Controller}.
     */
    public SoccerControlPanel(PlaybackHandler playbackHandler, Controller controller) {
        playbackHandler.addObserver(this);

        ToolTipManager.sharedInstance().setDismissDelay(10000);
        ToolTipManager.sharedInstance().setInitialDelay(750);

        // Buttons erstellen
        this.playButton = this.createDesignedButton(controller.getPlay(),
                "<html><b><u>Play</u> (Alt+P)</b><br />Starten der Wiedergabe</html>", "play.png", "play_hover.png",
                "play_pressed.png");
        this.pauseButton = this.createDesignedButton(controller.getPause(),
                "<html><b><u>Pause</u> (Alt+P)</b><br />Pausieren der Wiedergabe</html>", "pause.png",
                "pause_hover.png", "pause_pressed.png");
        this.rewindButton = this.createDesignedButton(controller.getRewind(),
                "<html><b><u>Rückspulen</u> (Alt+B)</b><br />"
                        + "Lässt die Wiedergabe der Simulation mit erhöhter Geschwindigkeit rückwärts laufen. "
                        + "<br />Durch zusätzliches Klicken wird die Rücklaufgeschwindigeit erhöht.</html>",
                "rewind.png", "rewind_hover.png", "rewind_pressed.png");
        this.forwardButton = this.createDesignedButton(controller.getForward(),
                "<html><b><u>Vorspulen</u> (Alt+F)</b><br />"
                        + "Lässt die Wiedergabe der Simulation mit erhöhter Geschwindigkeit ablaufen. "
                        + "<br />Durch zusätzliches Klicken wird die Geschwindigkeit erhöht</html>", "forward.png",
                "forward_hover.png", "forward_pressed.png");
        this.stepForwardButton = this.createDesignedButton(controller.getSimulationStepForward(),
                "<html><b><u>Schritt Vorwärts</u> (Alt+Rechte Pfeiltaste)</b><br />Einzelne "
                        + "Simulationsschritte vorspringen.</html>", "stepForward.png", "stepForward_hover.png",
                "stepForward_pressed.png");
        this.stepBackwardButton = this.createDesignedButton(controller.getSimulationStepBack(),
                "<html><b><u>Schritt Zurück</u> (Alt+Linke Pfeiltaste)</b><br />Einzelne Simulationsschritte "
                        + "zurückspringen.</html>", "stepBackward.png", "stepBackward_hover.png",
                "stepBackward_pressed.png");
        this.stopButton = this.createDesignedButton(controller.getStop(),
                "<html><b><u>Stop</u> (Alt+.)</b><br />Pausiert und springt an den Anfang. Eine nicht "
                        + "abge-<br>schlossene Simulation kann abgebrochen werden.</html>", "stop.png",
                "stop_hover.png", "stop_pressed.png");
        this.replayButton = this.createDesignedButton(controller.getReplay(),
                "<html><b><u>Replay</u> (Alt+R)</b><br />"
                        + "Zeigt eine Wiederholung des letzten Spielgeschehens</html>", "replay.png",
                "replay_hover.png", "replay_pressed.png");

        // Hotkeys festlegen
        this.playButton.setMnemonic(KeyEvent.VK_P);
        this.pauseButton.setMnemonic(KeyEvent.VK_P);
        this.rewindButton.setMnemonic(KeyEvent.VK_B);
        this.forwardButton.setMnemonic(KeyEvent.VK_F);
        this.stepForwardButton.setMnemonic(KeyEvent.VK_RIGHT);
        this.stepBackwardButton.setMnemonic(KeyEvent.VK_LEFT);
        this.stopButton.setMnemonic(KeyEvent.VK_PERIOD);
        this.replayButton.setMnemonic(KeyEvent.VK_R);

        this.speedLabel = new JLabel();

        this.speedSlider = new JSlider(JSlider.VERTICAL, convertSpeedRateToSpeedSliderValue(0.1),
                convertSpeedRateToSpeedSliderValue(PlaybackHandler.MAX_PLAYBACK_SPEED_RATE),
                convertSpeedRateToSpeedSliderValue(1.0));
        this.speedSlider.addChangeListener(controller.getSpeedSelectorChanged());
        this.setSpeedSliderDesign(this.speedSlider);

        // Zeitleiste initialisieren (Bei Programmstart 90 Minuten)
        soccerTimeLine.setMaximum(90 * 60 * PlaybackHandler.TICKS_PER_SECOND);
        soccerTimeLine.addChangeListener(controller.getTimelineChanged());

        this.createPanelLayout();
    }

    private void createPanelLayout() {
        // Constraints festlegen
        buttonConstraints.insets = new Insets(0, 5, 0, 5);
        buttonConstraints.ipadx = 30;
        buttonConstraints.gridheight = 2;
        buttonConstraints.gridy = 0;

        // Komponenten zu buttonPanel hinzufügen
        buttonConstraints.gridx = 0;
        buttonPanel.add(stepBackwardButton, buttonConstraints);

        buttonConstraints.gridx = 1;
        buttonPanel.add(stepForwardButton, buttonConstraints);

        buttonConstraints.gridx = 2;
        buttonPanel.add(rewindButton, buttonConstraints);

        buttonConstraints.gridx = 3;
        buttonPanel.add(stopButton, buttonConstraints);

        buttonConstraints.gridx = 4;
        this.buttonPanel.add(this.playButton, buttonConstraints);
        this.buttonPanel.add(this.pauseButton, buttonConstraints);
        this.exchangePlayPauseButton(true);

        buttonConstraints.gridx = 5;
        buttonPanel.add(forwardButton, buttonConstraints);

        buttonConstraints.gridx = 6;
        buttonPanel.add(replayButton, buttonConstraints);

        buttonConstraints.gridx = 7;
        buttonPanel.add(speedSlider, buttonConstraints);

        // SpeedLabel einfügen
        GridBagConstraints speedLabelConstraints = (GridBagConstraints) buttonConstraints.clone();
        speedLabelConstraints.gridheight = 1;
        speedLabelConstraints.gridx = 8;
        speedLabelConstraints.gridy = 0;
        speedLabelConstraints.insets = new Insets(25, 5, 0, 5);
        buttonPanel.add(this.speedLabelCaption, speedLabelConstraints);
        speedLabelConstraints.insets.top = 0;
        speedLabelConstraints.insets.bottom = 30;
        speedLabelConstraints.gridy = 1;
        buttonPanel.add(this.speedLabel, speedLabelConstraints);

        // Komponenten zur aktuellen SoccerControlPanel-Instanz hinzufügen
        this.setLayout(new BorderLayout());
        this.add(soccerTimeLine, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Hilfsmethode, welche zur Anpassung der Darstellung von Buttons dient. <br>
     * Dem übergebenen Button werden die angegebenen Bilder für die Stati "normal", "hervorgehoben" und "gedrückt"
     * zugewiesen. Des weiteren wird die Darstellung wie Farben und Verhaltensweisen zugewiesen.
     * 
     * @param button
     *            Referenz auf den JButton, der angepasst werden soll.
     * @param imageIconName
     *            Dateiname des Bilds, welches für "normal" verwendet werden soll.
     * @param imageRolloverIconName
     *            Dateiname des Bilds, welches für "hervorgehoben" verwendet werden soll.
     * @param imagePressedIconName
     *            Dateiname des Bilds, welches für "gedrückt" verwendet werden soll.
     * @return Gibt einen JButton-Instanz zurück, dessen Design entsprechend festgelegt wurde.
     */
    private JButton createDesignedButton(ActionListener action, String toolTip, String imageIconName,
            String imageRolloverIconName, String imagePressedIconName) {
        JButton button = new JButton();

        button.addActionListener(action);
        button.setToolTipText(toolTip);

        button.setIcon(new ImageIcon(getClass().getResource("/resources/images/" + imageIconName)));
        button.setRolloverIcon(new ImageIcon(getClass().getResource("/resources/images/" + imageRolloverIconName)));
        button.setPressedIcon(new ImageIcon(getClass().getResource("/resources/images/" + imagePressedIconName)));
        button.setBorder(null);
        button.setBackground(Color.GRAY);
        button.setForeground(Color.BLACK);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        return button;
    }

    /**
     * Dient zur Aktualisierung von simulationsrelevanter Werte. Hierbei wird die maximale Gesamtdauer der aktuellen
     * (bzw. genauer der neuen Simulation) übergeben, wodurch die Skala auf der Zeitleiste entsprechend angepasst wird.
     * 
     * @param maxDuration
     *            maximale Gesamtdauer der Simulation in Ticks.
     */
    public void setData(int maxDuration) {
        this.soccerTimeLine.setMaximum(maxDuration);
    }

    /**
     * Nimmt die neuen Darstellungsinformation entgegen und aktualisiert die Position des Reglerknopfs auf der
     * Zeitleiste<br>
     * {@inheritDoc}
     */
    @Override
    public void refresh(TickData tickData, double playbackSpeed, int simulationTickPosition) {
        boolean stepBackEnabled = this.stepBackwardButton.isEnabled();
        boolean stepForwardEnabled = this.stepForwardButton.isEnabled();
        boolean replayButtonEnabled = this.replayButton.isEnabled();
        int tickPosition = tickData.getTickPosition();

        // if ((tickPosition == 0 && rewindEnabled) || (tickPosition > 0 && !rewindEnabled)) {
        if (tickPosition == 0 ^ !stepBackEnabled) {
            this.stepBackwardButton.setEnabled(tickPosition > 0);
            this.rewindButton.setEnabled(tickPosition > 0);
        }

        if (tickPosition >= simulationTickPosition ^ !stepForwardEnabled) {
            this.stepForwardButton.setEnabled(tickPosition < simulationTickPosition);
            this.forwardButton.setEnabled(tickPosition < simulationTickPosition);
        }

        if ((tickPosition == 0 || playbackSpeed < 0) ^ !replayButtonEnabled) {
            this.replayButton.setEnabled(tickPosition > 0 && playbackSpeed > 0);
        }

        currentSpeedRate = playbackSpeed;

        this.soccerTimeLine.setValueWithoutNotify(tickPosition);
        this.soccerTimeLine.updateSimulationTickPosition(simulationTickPosition);
    }

    @Override
    public void update(Observable observable, Object data) {

        if (data instanceof SoccerUpdateEvent) {
            switch ((SoccerUpdateEvent) data) {
            case PLAYBACK_STARTED:
                this.exchangePlayPauseButton(false);
                this.setEnabledAll(true);
                break;

            case PLAYBACK_ABORTED:
                this.setEnabledAll(false);
                this.exchangePlayPauseButton(true);
                this.currentSpeedRate = 1.0;
                this.updateGuiSpeedValues();
                break;

            case PLAYBACK_SPEED_CHANGED:
                updateGuiSpeedValues();
                break;

            case PLAYBACK_PAUSED:
                this.exchangePlayPauseButton(true);
                break;

            default:
                break;
            }

        }
    }

    /**
     * Legt den Enabled-Status aller relevanter Komponenten fest
     * 
     * @param enabled
     *            Der neue Enabled-Status
     */
    private void setEnabledAll(boolean enabled) {
        playButton.setEnabled(enabled);
        pauseButton.setEnabled(enabled);
        rewindButton.setEnabled(enabled);
        forwardButton.setEnabled(enabled);
        stepForwardButton.setEnabled(enabled);
        stepBackwardButton.setEnabled(enabled);
        stopButton.setEnabled(enabled);
        replayButton.setEnabled(enabled && this.currentSpeedRate > 0);
        speedSlider.setEnabled(enabled);
        soccerTimeLine.setEnabled(enabled);
        speedLabel.setEnabled(enabled);
        speedLabelCaption.setEnabled(enabled);
    }

    /**
     * Tauscht den Wiedergabe- mit dem Pausebutton aus
     * 
     * @param playButtonVisible
     *            Flag, ob der Play-Button sichtbar sein soll
     */
    private void exchangePlayPauseButton(boolean playButtonVisible) {
        this.playButton.setVisible(playButtonVisible);
        this.pauseButton.setVisible(!playButtonVisible);

        this.buttonPanel.repaint();
    }

    private void setSpeedSliderDesign(JSlider speedSlider) {
        speedSlider.setPreferredSize(new Dimension(50, 90));

        speedSlider.setMajorTickSpacing(90);
        speedSlider.setMinorTickSpacing(30);

        Hashtable<Integer, JLabel> speedLabelTable = new Hashtable<Integer, JLabel>();

        speedLabelTable.put(convertSpeedRateToSpeedSliderValue(0.1), new JLabel(" 0.10"));
        // speedLabelTable.put(new Integer(55), new JLabel("0.55"));
        speedLabelTable.put(convertSpeedRateToSpeedSliderValue(1.0), new JLabel(" 1.00"));
        // speedLabelTable.put(new Integer(145), new JLabel("5.50"));
        speedLabelTable.put(convertSpeedRateToSpeedSliderValue(10), new JLabel(" 10.0"));

        speedSlider.setLabelTable(speedLabelTable);
        speedSlider.setPaintLabels(true);
        speedSlider.setPaintTicks(true);

    }

    private void updateGuiSpeedValues() {
        // Only update SpeedSlider if speed has changed (more than a threshold)
        if (Math.abs(convertSpeedSliderValueToSpeedRate(this.speedSlider.getValue()) - this.currentSpeedRate) > 0.005)
            this.speedSlider.setValue(convertSpeedRateToSpeedSliderValue(Math.abs(currentSpeedRate)));

        speedLabel.setText(String.format("%.2f x", this.currentSpeedRate));
    }

    /**
     * Konvertiert einen im SpeedSlider eingestellten Wert zu einem Vielfachen.
     * 
     * @param value
     *            Wert des SpeedSliders (JSlider)
     * @return Umgerechnete Geschwindigkeit als Vielfaches
     */
    public static double convertSpeedSliderValueToSpeedRate(int value) {
        if (value <= 100)
            return (double) value / 100;
        else
            return (double) value / 10 - 9;
    }

    /**
     * Konvertiert eine Geschwindigkeitsangabe als Vielfaches zu einem SpeedSlider-Wert
     * 
     * @param speed
     *            Geschwindigkeit als Vielfaches
     * @return Wert des SpeedSliders (JSlider)
     */
    public static int convertSpeedRateToSpeedSliderValue(double speed) {
        if (speed <= 1.0)
            return (int) Math.round(speed * 100);
        else
            return (int) Math.round((speed + 9) * 10);
    }

}
