package SoSi.View;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;

import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;

import SoSi.Model.PlaybackHandler;
import SoSi.Model.SimulationOptions;

/**
 * Dient zur Auswahl diverser Simulationseinstellungen, wie:
 * <ul>
 * <li>"Neue Simulation starten" (beim Erstellen einer neuen Simulation),
 * <li>"Simulationseinstellungen" (während einer aktuell laufenden Simulation im Hauptfenster)
 * <li>"Neues Darstellungsfenster" (beim Erstellen eines zusätzlichen Darstellungsfensters).
 */
public class SimulationOptionsFrame extends JFrame implements ActionListener {

    /**
     * Random generated.
     */
    private static final long serialVersionUID = -5219441846329826560L;

    private static final String PATH_TO_AI_FOLDER = "AIs";
    private static final String JAR_FILE_EXTENSION = ".jar";

    /**
     * Referenz auf JButton, welcher zum Bestätigen des Dialogs dient. <br>
     * Da sich der Kontext des Frames je nach Aufruf ändert, ist es notwendig, die ActionListener-Referenz des Buttons
     * bei jedem Aufruf anzupassen.
     */
    private JButton confirmButton;

    /**
     * Referenz auf ActionListener, welcher nach erfolgreichem Bestätigen des Dialogs ausgeführt werden soll.
     */
    private ActionListener confirmationAction;

    /**
     * Aktueller Anzeigetyp des Dialogs
     */
    private SimulationOptionsFrameType currentDisplayType = null;

    /**
     * Zuletzt ausgewählte und gemerkte Spielfeldansicht
     */
    private int rememberedBackgroundSelectionIndex = -1;

    /**
     * Standardparameter der Option "Farbe der Spieler des Teams A".
     */
    public static Color DEFAULT_COLOR_TEAM_A = Color.BLUE;
    /**
     * Standardparameter der Option "Farbe der Spieler des Teams B".
     */
    public static Color DEFAULT_COLOR_TEAM_B = Color.RED;

    private static int DEFAULT_SIMULATION_DURATION_IN_MINUTES = 20;

    private static final String[] COLORS_NAMES = { "Blau", "Rot", "Grün", "Gelb", "Orange", "Grau", "Schwarz", };
    private static final Color[] COLORS_VALUES = { Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.ORANGE,
            Color.GRAY, Color.BLACK };

    private ArrayList<AIData> aiDataList;

    private JPanel simulationOptions;
    private JPanel displayOptions;

    private JSpinner simulationDuration;
    private JComboBox<Integer> playersCount;
    private JComboBox<AIData> teamAAiName;
    private JComboBox<AIData> teamBAiName;
    private JCheckBox boundsActivated;
    private JCheckBox offsideActivated;

    private JComboBox<String> colorTeamA;
    private JComboBox<String> colorTeamB;
    private JCheckBox soundsActivated;

    private JComboBox<BackgroundType> backgroundSelector;

    private final BackgroundType[] BACKGROUND_TYPES;

    /**
     * Erstellt den Frame und die darin enthaltenen Komponenten und fügt diese dem Frame entsprechend hinzu. Des
     * weiteren werden die Standardparameter festgelegt.
     */
    public SimulationOptionsFrame() {
        super();

        // Load existing AI's
        this.aiDataList = getAIDataList();

        this.BACKGROUND_TYPES = createBackgroundTypes();

        this.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.LINE_START;

        simulationOptions = this.createSimulationOptionsPanel(constraints);
        constraints.gridx = 0;
        constraints.gridy = 0;
        this.getContentPane().add(simulationOptions, constraints);

        displayOptions = this.createDisplayOptionsPanel(constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        this.getContentPane().add(displayOptions, constraints);

        JPanel backgroundSelector = this.createBackgroundSelectorPanel(constraints);
        constraints.gridx = 0;
        constraints.gridy = 2;
        this.getContentPane().add(backgroundSelector, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        this.confirmButton = new JButton("OK");
        this.confirmButton.addActionListener(this);
        this.getContentPane().add(this.confirmButton, constraints);
        this.getRootPane().setDefaultButton(this.confirmButton);

        this.setLocationByPlatform(true);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.setResizable(false);
        this.pack();
    }

    /**
     * Ermittelt eine Liste vom AIs, welche geladen werden können und gibt diese zurück.
     * 
     * @return Gefundene und ladbare AIs
     */
    private ArrayList<AIData> getAIDataList() {
        ArrayList<AIData> dataList = new ArrayList<AIData>();
        File aiFolder = new File(PATH_TO_AI_FOLDER);
        if (aiFolder.isDirectory()) {
            File[] aiCandidates = aiFolder.listFiles();
            if (aiCandidates != null) {
                for (int counter = 0; counter < aiCandidates.length; ++counter) {
                    try {
                        JarFile jarFile = new JarFile(aiCandidates[counter]);
                        String path = aiCandidates[counter].getAbsolutePath();
                        String fullName = aiCandidates[counter].getName();
                        int nameLength = fullName.length();
                        if (nameLength > JAR_FILE_EXTENSION.length()) {
                            String name = fullName.substring(0, nameLength - JAR_FILE_EXTENSION.length());
                            dataList.add(new AIData(path, name));
                        }
                        jarFile.close();
                    } catch (IOException e) {
                        // just ignore
                    }
                }
            }
        }

        // Liste alphabetisch sortieren
        Collections.sort(dataList);

        return dataList;
    }

    private JPanel createSimulationOptionsPanel(GridBagConstraints constraints) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Simulationsoptionen"));

        // Spielzeit
        constraints.gridy = 0;
        constraints.gridx = 0;
        panel.add(new JLabel("Spielzeit:"), constraints);

        constraints.gridx = 1;
        simulationDuration = new JSpinner(new SpinnerNumberModel(DEFAULT_SIMULATION_DURATION_IN_MINUTES, 1, 24 * 60, 1));

        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) simulationDuration.getEditor();
        JFormattedTextField ftf = editor.getTextField();
        JFormattedTextField.AbstractFormatter formatter = ftf.getFormatter();
        DefaultFormatter df = (DefaultFormatter) formatter;
        df.setAllowsInvalid(false);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(simulationDuration);
        timePanel.add(new JLabel("Minuten"));
        panel.add(timePanel, constraints);

        // Spieleranzahl
        constraints.gridy = 1;
        constraints.gridx = 0;
        panel.add(new JLabel("Spieleranzahl:"), constraints);

        constraints.gridx = 1;
        Integer[] playersCountValues = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
        playersCount = new JComboBox<Integer>(playersCountValues);
        playersCount.setSelectedIndex(3 - 1);
        panel.add(playersCount, constraints);

        // KI-Auswahl
        // String[] demoAiNames = { "Schlaue KI", "Ganz schlaue KI", "Nerd KI" };
        constraints.gridy = 2;
        constraints.gridx = 0;
        panel.add(new JLabel("KI Team A:"), constraints);
        constraints.gridy = 3;
        constraints.gridx = 0;
        panel.add(new JLabel("KI Team B:"), constraints);

        constraints.gridy = 2;
        constraints.gridx = 1;
        teamAAiName = new JComboBox<AIData>(this.aiDataList.toArray(new AIData[aiDataList.size()]));
        panel.add(teamAAiName, constraints);
        constraints.gridy = 3;
        constraints.gridx = 1;
        teamBAiName = new JComboBox<AIData>(this.aiDataList.toArray(new AIData[aiDataList.size()]));
        panel.add(teamBAiName, constraints);

        // Bande
        constraints.gridy = 4;
        constraints.gridx = 0;
        panel.add(new JLabel("Regeln:"), constraints);

        GridBagConstraints custom_constraints = (GridBagConstraints) constraints.clone();
        custom_constraints.insets.top = 0;
        custom_constraints.insets.bottom = 0;
        custom_constraints.gridx = 1;
        boundsActivated = new JCheckBox("Bande aktiv");
        boundsActivated.setSelected(true);
        panel.add(boundsActivated, custom_constraints);
        custom_constraints.gridy = 5;
        offsideActivated = new JCheckBox("Abseits");
        panel.add(offsideActivated, custom_constraints);

        return panel;
    }

    private JPanel createDisplayOptionsPanel(GridBagConstraints constraints) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Darstellungsoptionen"));

        // Farben der Teams
        constraints.gridy = 0;
        constraints.gridx = 0;
        panel.add(new JLabel("Farbe Team A:"), constraints);

        constraints.gridy = 1;
        constraints.gridx = 0;
        panel.add(new JLabel("Farbe Team B:"), constraints);

        constraints.gridy = 0;
        constraints.gridx = 1;
        colorTeamA = new JComboBox<String>(COLORS_NAMES);
        panel.add(colorTeamA, constraints);

        constraints.gridy = 1;
        colorTeamB = new JComboBox<String>(COLORS_NAMES);
        colorTeamB.setSelectedIndex(1);
        panel.add(colorTeamB, constraints);

        // Sounds
        constraints.gridy = 2;
        constraints.gridx = 0;
        panel.add(new JLabel("Sounds:"), constraints);

        constraints.gridx = 1;
        soundsActivated = new JCheckBox("Sounds aktivieren");
        soundsActivated.setSelected(true);
        panel.add(soundsActivated, constraints);

        return panel;
    }

    private JPanel createBackgroundSelectorPanel(GridBagConstraints constraints) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Fußballfeld"));

        // Spielfeld-Hintergrund
        constraints.gridy = 0;
        constraints.gridx = 0;
        backgroundSelector = new JComboBox<BackgroundType>(BACKGROUND_TYPES);
        panel.add(backgroundSelector, constraints);
        constraints.gridy = 1;
        final JLabel imagePreview = new JLabel();
        panel.add(imagePreview, constraints);

        // Vorschau der Grafik
        backgroundSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final int preferedWidth = 250;

                ImageIcon newIcon;
                int height;
                try {
                    Image img = ((BackgroundType) ((JComboBox<?>) arg0.getSource()).getSelectedItem()).getImageIcon()
                            .getImage();
                    height = (int) (Math.round((double) preferedWidth / img.getWidth(null) * img.getHeight(null)));

                    Image newImg = img.getScaledInstance(preferedWidth, height, java.awt.Image.SCALE_SMOOTH);
                    newIcon = new ImageIcon(newImg);
                } catch (NullPointerException e) {
                    newIcon = null;
                    height = 0;
                }

                imagePreview.setIcon(newIcon);
                imagePreview.setPreferredSize(new Dimension(preferedWidth, height));

                pack();
            }
        });

        backgroundSelector.setSelectedIndex(0);

        return panel;
    }

    /**
     * Erstellt ein Array mit sämtlichen Hintergrundbildern und die dafür benötigten Daten wie Dateiname und
     * Pixel-Positionen der Spielfeldecken.
     * 
     * @return Array mit sämtlichen Hintergrundbildangaben
     */
    private BackgroundType[] createBackgroundTypes() {
        BackgroundType[] backgrounds = new BackgroundType[9];

        backgrounds[0] = new BackgroundType("Stadion (top-down)", "Stadium_TopDown.jpg", new Point(63, 65), new Point(
                1470, 946));
        backgrounds[1] = new BackgroundType("Stadion (perspektivisch)", "Stadium_Perspective.jpg", new Point(316, 501),
                new Point(1216, 501), new Point(1403, 887));
        backgrounds[2] = new BackgroundType("Stadion (perspektivisch 2)", "Stadium_Perspective2.jpg", new Point(282, 594),
                new Point(1251, 594), new Point(1497, 966));
        backgrounds[3] = new BackgroundType("Strand (top-down)", "Beach_TopDown.jpg", new Point(63, 65), new Point(
                1470, 946));
        backgrounds[4] = new BackgroundType("Strand (perspektivisch)", "Beach_Perspective.jpg", new Point(316, 501),
                new Point(1216, 501), new Point(1403, 887));
        backgrounds[5] = new BackgroundType("Berge (top-down)", "Berge_TopDown.jpg", new Point(63, 65), new Point(1470,
                946));
        backgrounds[6] = new BackgroundType("Berge (perspektivisch)", "Berge_Perspective.jpg", new Point(316, 501),
                new Point(1216, 501), new Point(1403, 887));
        backgrounds[7] = new BackgroundType("Bolzplatz (top-down)", "Bolzplatz_TopDown.jpg", new Point(63, 65),
                new Point(1470, 946));
        backgrounds[8] = new BackgroundType("Bolzplatz (perspektivisch)", "Bolzplatz_Perspective.jpg", new Point(316,
                501), new Point(1216, 501), new Point(1403, 887));

        return backgrounds;
    }

    /**
     * Zeigt den Frame an. Abhängig von den angegebenen Parametern, werden die entsprechenden möglichen Auswahloptionen
     * angezeigt.
     * 
     * @param displayType
     *            Anzeigetyp, nach welchem sich die anzuzeigenden Auswahloptionen richten.
     * @param confirmationAction
     *            Referenz auf ActionListener, welcher bei Bestätigung des Formulars aufgerufen werden soll.
     */
    public void show(SimulationOptionsFrameType displayType, ActionListener confirmationAction) {
        this.confirmationAction = confirmationAction;

        if (displayType != this.currentDisplayType || !this.isVisible()) {
            this.restoreViewSelection();
        }

        this.currentDisplayType = displayType;

        switch (displayType) {
        case NEW_SIMULATION:
            this.simulationOptions.setVisible(true);
            this.displayOptions.setVisible(true);
            this.setTitle("Neue Simulation erstellen");
            break;
        case CHANGE_OPTIONS:
            this.simulationOptions.setVisible(false);
            this.displayOptions.setVisible(true);
            this.setTitle("Darstellungsoptionen");
            break;
        case NEW_VIEW_FRAME:
            this.simulationOptions.setVisible(false);
            this.displayOptions.setVisible(false);
            this.setTitle("Neues Darstellungsfenster");

            // Verhindert Fehler, dass kein zusätzliches Darstellungsfenster geöffnet werden kann, falls zweimal die
            // selbe Farbe ausgewählt wurde. Dabei wird eine Farbauswahl auf einen anderen Wert gesetzt.
            if (this.colorTeamA.getSelectedIndex() == this.colorTeamB.getSelectedIndex()) {
                this.colorTeamB.setSelectedIndex(this.colorTeamB.getSelectedIndex() == 0 ? 1 : this.colorTeamB
                        .getSelectedIndex() - 1);
            }

            break;
        default:
            break;
        }

        this.pack();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
            }
        });
    }

    /**
     * Speichert, welcher Hintergrunddarstellung derzeit angezeigt ist
     */
    private void rememberbackgroundSelectorSelection() {
        rememberedBackgroundSelectionIndex = this.backgroundSelector.getSelectedIndex();
    }

    /**
     * Setzt die Auswahl der Hintergrunddarstellung auf die gemerkte Auswahl zurück.
     */
    private void restoreViewSelection() {
        if (rememberedBackgroundSelectionIndex >= 0)
            this.backgroundSelector.setSelectedIndex(rememberedBackgroundSelectionIndex);

    }

    /**
     * Liefert die ausgewählte Farbe für die Darstellung der Spieler von Team A.
     * 
     * @return Farbe der Spiele von Team A.
     */
    public Color getSelectionTeamAColor() {
        return COLORS_VALUES[colorTeamA.getSelectedIndex()];
    }

    /**
     * Liefert die ausgewählte Farbe für die Darstellung der Spieler von Team B.
     * 
     * @return Farbe der Spiele von Team B.
     */
    public Color getSelectionTeamBColor() {
        return COLORS_VALUES[colorTeamB.getSelectedIndex()];
    }

    /**
     * Liefert den Dateinamen der ausgewählten KI von Team A.
     * 
     * @return Dateiname der KI von Team A.
     */
    public String getSelectionTeamA_Path() {
        return ((AIData) teamAAiName.getSelectedItem()).getPath();
    }

    /**
     * Liefert den Dateinamen der ausgewählten KI von Team B.
     * 
     * @return Dateiname der KI von Team B.
     */
    public String getSelectionTeamB_Path() {
        return ((AIData) teamBAiName.getSelectedItem()).getPath();
    }

    /**
     * Liefert die gewünschte Dauer der Simulation (=Anzahl an Ticks).
     * 
     * @return Gesamtanzahl der Ticks für eine neue Simulation.
     */
    public int getSelectionSimulationDuration() {
        return (Integer) this.simulationDuration.getValue() * 60 * PlaybackHandler.TICKS_PER_SECOND;
    }

    /**
     * Setzt die ausgewählte Dauer der Simulation (=Anzahl an Ticks).
     * 
     * @param duration
     *            Festzulegende Dauer der Simulation
     */
    public void setSelectionSimulationDuration(int duration) {
        this.simulationDuration.setValue((int) Math.round((double) duration / 60 / PlaybackHandler.TICKS_PER_SECOND));
    }

    /**
     * Liefert die gewünschte Anzahl der Spieler pro Team für eine neue Simulation.
     * 
     * @return Anzahl an Spieler pro Team für eine neue Simulation.
     */
    public int getSelectionPlayersPerTeam() {
        return (Integer) (playersCount.getSelectedItem());
    }

    /**
     * Setzt die gewünschte Anzahl der Spieler pro Team für eine neue Simulation.
     * 
     * @param playerCount
     *            Festzulegende Spieleranzahl
     */
    public void setSelectionPlayersPerTeam(Integer playerCount) {
        playersCount.setSelectedItem(playerCount);
    }

    /**
     * Liefert eine bereits initialisierte Instanz von SoccerPanel, welche zur Darstellung des Spielfelds dient. Die
     * Initialisierung bzw. die Instanz ist abhängig von der vom Benutzer gewünschten Darstellung des Spielfelds.
     * 
     * @param paintGoalCountActivated
     *            Flag, ob die Ausgabe des Torstands im SoccerPanel erfolgen soll
     * @return Initialisiertes SoccerPanel zur Darstellung des Spielfelds.
     */
    public SoccerPanel getSelectionSoccerPanel(boolean paintGoalCountActivated) {
        return ((BackgroundType) this.backgroundSelector.getSelectedItem()).createSoccerPanel(
                this.getSelectionTeamAColor(), this.getSelectionTeamBColor(), paintGoalCountActivated);
    }

    /**
     * Liefert ein Booleanflag, ob die Soundausgabe aktiviert oder deaktiviert werden soll.
     * 
     * @return true, falls Soundausgabe aktiviert ist bzw. werden soll.
     */
    public boolean getSelectionSoundsActivated() {
        return this.soundsActivated.isSelected();
    }

    /**
     * Liefert eine Sammlung an Simulationsoptionen, welche das Verhalten der Simulation beeinflussen.
     * 
     * @see SimulationOptions
     * 
     * @return Liste an aktivierten Simulationsoptionen.
     */
    public List<SimulationOptions> getSelectionSimulationOptions() {
        List<SimulationOptions> activatedOptions = new LinkedList<SimulationOptions>();
        if (!boundsActivated.isSelected())
            activatedOptions.add(SimulationOptions.OFF_RULE);

        if (offsideActivated.isSelected())
            activatedOptions.add(SimulationOptions.OFFSIDE_RULE);

        return activatedOptions;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (this.colorTeamA.getSelectedIndex() == this.colorTeamB.getSelectedIndex()) {
            JOptionPane.showMessageDialog(null, "Zwei Teams dürfen nicht die selbe Farbe haben",
                    "Identische Farbauswahl", JOptionPane.ERROR_MESSAGE);
        } else if (this.teamAAiName.getSelectedItem() == null || this.teamBAiName.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, "Für beide Teams muss eine KI ausgewählt sein. \n"
                    + "Bitte vergewissern Sie sich, dass die entsprechenden KIs im Verzeichnis \"AIs\" vorliegen.",
                    "Ungültige KI-Auswahl", JOptionPane.ERROR_MESSAGE);
        } else {
            this.confirmationAction.actionPerformed(e);

            if (currentDisplayType != SimulationOptionsFrameType.NEW_VIEW_FRAME) {
                this.rememberbackgroundSelectorSelection();
            }

            this.currentDisplayType = null;
        }

    }

    /**
     * Beschreibt einen Hintergrund und enthält sämtliche benötigten Daten wie Dateiname des Hintergrundbilds und die
     * Pixelangaben der Spielfeldecken.
     */
    private class BackgroundType {
        private final String name;
        private final String backgroundFileName;
        private final Point fieldTopLeft;
        private final Point fieldBottomRight;
        private final Point fieldTopRight;

        BackgroundType(String name, String backgroundFileName, Point fieldTopLeft, Point fieldBottomRight) {
            this(name, backgroundFileName, fieldTopLeft, null, fieldBottomRight);
        }

        BackgroundType(String name, String backgroundFileName, Point fieldTopLeft, Point fieldTopRight,
                Point fieldBottomRight) {
            assert (fieldTopRight == null || fieldTopLeft.y == fieldTopRight.y) : "Ungültige Spielfeldinitialisierung!";

            this.name = name;
            this.backgroundFileName = backgroundFileName;
            this.fieldTopLeft = fieldTopLeft;
            this.fieldBottomRight = fieldBottomRight;
            this.fieldTopRight = fieldTopRight;
        }

        SoccerPanel createSoccerPanel(Color teamAColor, Color teamBColor, boolean paintGoalCountActivated) {
            if (fieldTopRight == null) {
                // Create TopDown-View
                return new SoccerPanelTopDown(this.backgroundFileName, teamAColor, teamBColor, paintGoalCountActivated,
                        this.fieldTopLeft, this.fieldBottomRight);
            } else {
                // Create Perspective-View
                Point bottomLeft = new Point(fieldTopLeft.x - (fieldBottomRight.x - fieldTopRight.x),
                        fieldBottomRight.y);
                return new SoccerPanelPerspective(this.backgroundFileName, teamAColor, teamBColor,
                        paintGoalCountActivated, this.fieldTopLeft, this.fieldTopRight, bottomLeft);
            }
        }

        public String toString() {
            return this.name;
        }

        public ImageIcon getImageIcon() {
            return new ImageIcon(getClass().getResource("/resources/images/" + backgroundFileName));
        }
    }

    /**
     * Repräsentiert eine ladbare KI und stellt sowohl den Dateinamen als auch die den Pfad zur Datei zur Verfügung.<br>
     * Außerdem implementiert sie Comparable, um die Einträge alphabetisch nach Ihrem Namen sortieren zu können.
     */
    private class AIData implements Comparable<AIData> {
        private String pathToJar;
        private String name;

        public AIData(String path, String name) {
            this.pathToJar = path;
            this.name = name;
        }

        public String getPath() {
            return this.pathToJar;
        }

        public String toString() {
            return this.name;
        }

        @Override
        public int compareTo(AIData o) {
            return this.name.compareTo(o.name);
        }
    }
}
