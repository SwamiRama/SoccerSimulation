package SoSi.View;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import SoSi.Model.TickData;
import SoSi.Model.Calculation.DataHandler;

/**
 * Dient sowohl zur Erstellung als auch zur Darstellung von aktuellen Wiedergabeinformationen (Tore, Dauer, Namen der
 * antretenden KIs).
 */
public class SoccerGameInformationPanel extends JPanel implements IRefreshable {

    /**
     * Random generated.
     */
    private static final long serialVersionUID = -5796018477394612715L;

    /**
     * Referenz auf JLabel, welches den aktuellen Namen von Team A enthält.
     * 
     * @see DataHandler
     */
    private final JLabel teamALabel;
    /**
     * Referenz auf JLabel, welches den aktuellen Namen von Team B enthält.
     * 
     * @see DataHandler
     */
    private final JLabel teamBLabel;

    /**
     * Referenz auf JLabel, welches den aktuellen Torestand (beider Teams) enthält.
     */
    private final JLabel scoreLabel;
    /**
     * Referenz auf JLabel, welches die aktuelle Wiedergabeposition und Gesamtdauer enthält.
     */
    private final JLabel timeLabelLeft;
    private final JLabel timeLabelRight;

    /**
     * Angabe der Dauer der Simulation.
     */
    private int maxSimulationDuration = 0;

    /**
     * Erstellt ein neues SoccerGameInformationPanel. Dabei werden sämtliche darin enthaltenen Komponenten erstellt und
     * entsprechend hinzugefügt.
     */
    public SoccerGameInformationPanel() {
        this.teamALabel = new JLabel();
        this.teamBLabel = new JLabel();
        this.scoreLabel = new JLabel();
        this.timeLabelLeft = new JLabel();
        this.timeLabelRight = new JLabel();

        this.initGuiComponents();

        this.positionGuiComponents();

        this.setData("Team SoSi", "Team SoSi", SimulationOptionsFrame.DEFAULT_COLOR_TEAM_A,
                SimulationOptionsFrame.DEFAULT_COLOR_TEAM_B, 0);
        this.setPlaybackPosition(0);
    }

    /**
     * Passt die Werte der im Panel enthaltenen Komponenten an.
     */
    private void initGuiComponents() {
        Border bo = new LineBorder(Color.BLACK);

        this.updateScores(0, 0);

        // Standard Darstellungsoptionen festlegen
        this.teamALabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        this.teamBLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        this.scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        this.timeLabelLeft.setFont(new Font("SansSerif", Font.BOLD, 20));
        this.timeLabelRight.setFont(new Font("SansSerif", Font.BOLD, 20));
        this.timeLabelLeft.setForeground(new Color(0, 0, 0, 0));

        this.teamALabel.setForeground(Color.WHITE);
        this.teamBLabel.setForeground(Color.WHITE);

        this.teamBLabel.setOpaque(true);
        this.teamALabel.setOpaque(true);

        this.teamALabel.setBorder(bo);
        this.teamBLabel.setBorder(bo);
    }

    /**
     * Positioniert die (bereits erstellten) GUI Komponenten.
     */
    private void positionGuiComponents() {
        this.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        constraints.gridx = 0;
        constraints.weightx = 0;
        this.add(this.timeLabelLeft, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        this.add(new JPanel(), constraints);

        constraints.gridx = 2;
        this.add(this.teamALabel);

        constraints.gridx = 3;
        constraints.weightx = 0;
        this.add(this.scoreLabel);

        constraints.gridx = 4;
        this.add(this.teamBLabel);

        constraints.gridx = 5;
        constraints.weightx = 1;
        this.add(new JPanel(), constraints);
        constraints.weightx = 0;

        constraints.gridx = 6;
        constraints.weightx = 0;
        this.add(this.timeLabelRight, constraints);
    }

    /**
     * Dient zur Aktualisierung von Werten, welche zur Darstellung des Panels notwendig sind. Dabei werden von der
     * Aktualisierung der Wiedergabe unabhängige Daten gesetzt, wie Farben der Spieler oder die Namen der Teams.
     * 
     * @param teamAName
     *            Name von Team A
     * @param teamBName
     *            Name von Team B
     * @param teamAColor
     *            Farbe der Spieler von Team A
     * @param teamBColor
     *            Farbe der Spieler von Team B
     * @param maxDuration
     *            Maximale Wiedergabedauer der Simulation
     */
    public void setData(String teamAName, String teamBName, Color teamAColor, Color teamBColor, int maxDuration) {
        teamALabel.setText(" " + teamAName + " ");
        teamBLabel.setText(" " + teamBName + " ");

        teamALabel.setBackground(teamAColor);
        teamBLabel.setBackground(teamBColor);

        this.maxSimulationDuration = maxDuration;
    }

    /**
     * Aktualisiert den im timeLabel angezeigten Wert. Dabei wird die aktuelle Wiedergabepostion und die Gesamtdauer in
     * lesbarer Form (in Minuten und Sekunden) umgerechnet und angezeigt.
     * 
     * @param position
     *            Aktuelle Wiedergabeposition (in Ticks)
     */
    private void setPlaybackPosition(int position) {
        String text = String.format("%s / %s", SoccerGUI.tickpositionToTimeString(position),
                SoccerGUI.tickpositionToTimeString(this.maxSimulationDuration));
        this.timeLabelLeft.setText(text);
        this.timeLabelRight.setText(text);
    }

    private void updateScores(int goalsTeamA, int goalsTeamB) {
        this.scoreLabel.setText(String.format(" %d - %d ", goalsTeamA, goalsTeamB));
    }

    @Override
    public void refresh(TickData tickData, double playbackSpeed, int simulationTickPosition) {
        this.setPlaybackPosition(tickData.getTickPosition());

        this.updateScores(tickData.getGoalsTeamA(), tickData.getGoalsTeamB());
    }
}
