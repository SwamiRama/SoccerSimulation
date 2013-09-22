package SoSi.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import SoSi.ControllerPkg.Controller;
import SoSi.Model.PlaybackHandler;
import SoSi.Model.SoccerUpdateEvent;
import SoSi.Model.TickData;

/**
 * Enthält die Entwicklerkonsole.<br>
 * Die Klasse zeigt Informationen über den Simulationszustand, wie u.a. auch Debugging-Nachrichten, welche ggf. von KIs
 * erstellt werden können, oder informiert über aufgetretene Fehler (Exceptions) von KIs.<br>
 * Das Fenster enthält des Weiteren Steuerungselemente, um die Wiedergabe um Einzelschritte vor- oder zurückzuschalten.
 */
public class DebuggingFrame extends JFrame implements IRefreshable, Observer {

    /**
     * Random generated
     */
    private static final long serialVersionUID = -2389588892873843002L;

    /**
     * Textbereich zur Ausgabe der Debuggingnachrichten
     */
    private JTextArea consoleFirstAi;

    private JTextArea consoleSecondAi;

    private JPanel consolePanel;

    private JPanel tickEventPanel;

    private JPanel consolePanelBothAi;

    private JPanel firstAi;

    private JPanel secondAi;

    private JPanel bottomPanel;

    private JLabel tickEventInformation;

    private JLabel tickPanel;

    private String firstTeam;

    private String secondTeam;

    private JButton stepForwardButton;

    private JButton stepBackwardButton;

    private final Controller controller;

    private PlaybackHandler playbackHandler;

    /**
     * Konstruktor der Klasse DebuggingFrame.
     * 
     * @param playbackHandler
     *            Referenz auf {@link PlaybackHandler}
     * @param controller
     *            Referenz auf {@link Controller}.
     */
    public DebuggingFrame(PlaybackHandler playbackHandler, Controller controller) {
        super("Entwicklerkonsole");
        this.setSize(new Dimension(650, 400));
        this.playbackHandler = playbackHandler;
        this.controller = controller;

        consolePanel = new JPanel(new BorderLayout());
        consolePanelBothAi = new JPanel();
        consolePanelBothAi.setLayout(new BoxLayout(consolePanelBothAi, BoxLayout.X_AXIS));

        // Anzeige des TickEvent des entsprechenden Ticks
        tickEventPanel = new JPanel();
        tickEventInformation = new JLabel();
        tickEventPanel.add(tickEventInformation);

        // Textfeld erste Ki
        consoleFirstAi = buildConsoleTextfield();
        firstAi = new JPanel(new BorderLayout());
        firstAi.add(consoleFirstAi, BorderLayout.CENTER);

        // Textfeld zweite Ki
        consoleSecondAi = buildConsoleTextfield();
        secondAi = new JPanel(new BorderLayout());
        secondAi.add(consoleSecondAi, BorderLayout.CENTER);

        // Untere Anzeige und Kontrollbuttons
        buildBottomPanel();

        consolePanelBothAi.add(new JScrollPane(firstAi));
        consolePanelBothAi.add(new JScrollPane(secondAi));

        consolePanel.add(tickEventPanel, BorderLayout.NORTH);
        consolePanel.add(bottomPanel, BorderLayout.SOUTH);
        consolePanel.add(consolePanelBothAi, BorderLayout.CENTER);

        this.getContentPane().add(consolePanel);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setResizable(false);
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

    private JTextArea buildConsoleTextfield() {
        JTextArea console = new JTextArea("");
        console.setEditable(false);
        console.setLineWrap(true);
        console.setWrapStyleWord(true);
        console.setForeground(Color.GREEN);
        console.setBackground(Color.BLACK);
        console.setAutoscrolls(false);
        return console;
    }

    private void buildBottomPanel() {
        bottomPanel = new JPanel(new FlowLayout());

        // BackButton
        stepBackwardButton = createDesignedButton(controller.getSimulationStepBack(),
                "<html><b><u>Schritt Zurück</u> (Alt+Linke Pfeiltaste)</b><br />Einzelne Simulationsschritte "
                        + "zurückspringen.</html>", "stepBackward.png", "stepBackward_hover.png",
                "stepBackward_pressed.png");
        stepBackwardButton.setEnabled(false);

        // ForwardButton
        stepForwardButton = createDesignedButton(controller.getSimulationStepForward(),
                "<html><b><u>Schritt Vorwärts</u> (Alt+Rechte Pfeiltaste)</b><br />Einzelne "
                        + "Simulationsschritte vorspringen.</html>", "stepForward.png", "stepForward_hover.png",
                "stepForward_pressed.png");
        stepForwardButton.setEnabled(false);

        // Tickanzeige
        tickPanel = new JLabel("0 / 0");
        tickPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tickPanel.isEnabled())
                    controller.getJumpToTime().actionPerformed(null);
            }
        });
        tickPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Zusammenfügen
        bottomPanel.add(stepBackwardButton);
        bottomPanel.add(tickPanel);
        bottomPanel.add(stepForwardButton);

    }

    @Override
    public void refresh(TickData tickData, double playbackSpeed, int simulationTickPosition) {
        boolean stepBackEnabled = this.stepBackwardButton.isEnabled();
        boolean stepForwardEnabled = this.stepForwardButton.isEnabled();
        int tickPosition = tickData.getTickPosition();

        if ((tickPosition == 0 ^ !stepBackEnabled)) {
            this.stepBackwardButton.setEnabled(tickPosition > 0);
        }

        if (tickPosition >= simulationTickPosition ^ !stepForwardEnabled) {
            this.stepForwardButton.setEnabled(tickPosition < simulationTickPosition);
        }

        firstTeam = playbackHandler.getTeamAName();
        secondTeam = playbackHandler.getTeamBName();

        this.updateDebugMessages(tickData);

        if (tickData.getTickEvent() != null) {
            switch (tickData.getTickEvent()) {
            case FOUL_TACKLING:
                tickEventInformation.setText("Ein Foul wurde begangen");
                break;
            case FOUL_OFF:
                tickEventInformation.setText("Aus ist vorgefallen");
                break;
            case FOUL_OFFSIDE:
                tickEventInformation.setText("Abseits");
                break;
            case GAME_INTERRUPTED:
                tickEventInformation.setText("Das Spiel ist unterbrochen");
                break;
            case GOAL_SCORED:
                tickEventInformation.setText("Ein Tor wurde geschossen");
                break;
            case FREE_KICK:
                tickEventInformation.setText("Ein Freistoß wird ausgeführt");
                break;
            case KICK_OFF:
                tickEventInformation.setText("Ein Abstoß wird ausgeführt");
                break;
            case HALFTIME:
                tickEventInformation.setText("Halbzeit");
                break;
            }
        } else {
            tickEventInformation.setText("Das Spiel läuft normal");
        }

        tickPanel.setText("" + Integer.toString(tickPosition) + " / " + simulationTickPosition);
    }

    /**
     * Gekürzte DebugMessage-Ausgabe von Team A
     */
    private String shortenedLastDebugMessageOfTeamA = "";

    /**
     * Gekürzte DebugMessage-Ausgabe von Team B
     */
    private String shortenedLastDebugMessageOfTeamB = "";

    private void updateDebugMessages(TickData tickData) {
        final String NO_DEBUG_MESSAGE_TEXT = "Keine Infos für aktuellen Tick.";
        final String LAST_DEBUG_MESSAGE_TEXT = "Zuletzt angezeigte Debugnachricht:";

        StringBuilder teamAText = new StringBuilder(firstTeam);
        StringBuilder teamBText = new StringBuilder(secondTeam);

        teamAText.append("\n\n");
        teamBText.append("\n\n");

        if (tickData.getDebugMessageTeamA().isEmpty()) {
            teamAText.append(NO_DEBUG_MESSAGE_TEXT);
            if (!shortenedLastDebugMessageOfTeamA.isEmpty()) {
                teamAText.append("\n\n");
                teamAText.append(LAST_DEBUG_MESSAGE_TEXT);
                teamAText.append("\n");
                teamAText.append(shortenedLastDebugMessageOfTeamA);
            }
        } else {
            teamAText.append(tickData.getDebugMessageTeamA());
            shortenedLastDebugMessageOfTeamA = getShortenedDebugInfo(tickData.getTickPosition(),
                    tickData.getDebugMessageTeamA());
        }

        if (tickData.getDebugMessageTeamB().isEmpty()) {
            teamBText.append(NO_DEBUG_MESSAGE_TEXT);
            if (!shortenedLastDebugMessageOfTeamB.isEmpty()) {
                teamBText.append("\n\n");
                teamBText.append(LAST_DEBUG_MESSAGE_TEXT);
                teamBText.append("\n");
                teamBText.append(shortenedLastDebugMessageOfTeamB);
            }
        } else {
            teamBText.append(tickData.getDebugMessageTeamB());
            shortenedLastDebugMessageOfTeamB = getShortenedDebugInfo(tickData.getTickPosition(),
                    tickData.getDebugMessageTeamB());
        }

        consoleFirstAi.setText(teamAText.toString());
        consoleSecondAi.setText(teamBText.toString());
    }

    private String getShortenedDebugInfo(int tickPosition, String debugMessage) {
        final int MAX_SHORTENED_LENGTH = 250;
        if (debugMessage.length() > MAX_SHORTENED_LENGTH)
            debugMessage = debugMessage.substring(0, MAX_SHORTENED_LENGTH - 4) + "...";
        return String.format("Tick: %d\nNachricht: %s", tickPosition, debugMessage);
    }

    @Override
    public void update(Observable arg0, Object data) {
        if (data instanceof SoccerUpdateEvent) {
            switch ((SoccerUpdateEvent) data) {

            case PLAYBACK_ABORTED:
                stepBackwardButton.setEnabled(false);
                stepForwardButton.setEnabled(false);
                tickPanel.setEnabled(false);

                shortenedLastDebugMessageOfTeamA = "";
                shortenedLastDebugMessageOfTeamB = "";
                consoleFirstAi.setText("");
                consoleSecondAi.setText("");
                break;
            case PLAYBACK_STARTED:
                tickPanel.setEnabled(true);
            default:
                break;
            }

        }

    }

}
