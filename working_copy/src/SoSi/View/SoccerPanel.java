package SoSi.View;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import sep.football.Position;

import SoSi.Model.PlaybackHandler;
import SoSi.Model.SoccerUpdateEvent;
import SoSi.Model.TickData;

/**
 * Von JPanel abgeleitete Klasse, die zur Darstellung des Spielfelds und der Simulationsobjekte dient.<br>
 * Die abstrakte Klasse beinhaltet Funktionen zum Laden des Hintergrundsbild und dessen Positionierung. Das
 * Hintergrundbild soll dabei stets im korrekten Seitenverhältnis zentriert angezeigt werden.
 */
public abstract class SoccerPanel extends JPanel implements IRefreshable, Observer {

    /**
     * Random generated.
     */
    private static final long serialVersionUID = -8466813658974699391L;

    /**
     * Breite der Bilddatei in Pixel.
     */
    protected final int imageWidth;

    /**
     * Höhe der Bilddatei in Pixel.
     */
    protected final int imageHeight;

    /**
     * Referenz auf die zu verwendende Bilddatei.
     */
    private final List<Image> backgroundImagesOriginal = new ArrayList<Image>();

    /**
     * Referenz auf das skalierte Hintergrundbild
     */
    private final List<Image> backgroundImagesScaled = new ArrayList<Image>();

    /**
     * FPS-Angabe, mit welcher Geschwindigkeit sich das Hintergrundbild (sofern entsprechende animierte
     * Hintergrundbilder-Frames vorliegen) abwechseln soll. <br>
     * Die tatsächliche Geschwindigkeit des Frameswechsel ist dabei jedoch von der aktuellen Wiedergabegeschwindigkeit
     * abhängig. Der hier angegebene Wert bezeigt sich auf 1,0 facher Wiedergabegeschwindigkeit
     * (Normalwiedergabegeschwindigkeit).
     */
    private final static int ANIMATION_FPS = 12;

    /**
     * Referenz auf das Image für den Ball
     */
    protected final Image ballImage;

    /**
     * Zuletzt aufgetretenes Event, welches gezeichnet werden soll
     */
    private SoccerUpdateEvent eventToPrint = null;

    /**
     * Zeitpunkt in Millisekunden, zu dem die Ausgabe des Events angefangen hat.
     */
    private double eventToPrintStoptime = 0;

    /**
     * Dauer (in Millisekunden), wie lang das TickEvent angezeigt werden soll.
     */
    private static final int MAX_EVENT_DISPLAYTIME = 1500;

    /**
     * Farbe, mit welcher die Spielfiguren des Teams A gezeichnet werden sollen.
     */
    protected final Color teamAColor;

    /**
     * Farbe, mit welcher die Spielfiguren des Teams A gezeichnet werden sollen.
     */
    protected final Color teamBColor;

    /**
     * Font für Spielernummerierung
     */
    private final Font playerNumbersFont;

    /**
     * Font für Event-Darstellungen wie "Halbzeit" oder "Foul"
     */
    private final Font eventFont;
    private static final int EVENT_FONT_PADDING = 84;
    private static final int EVENT_BOX_PADDING = 5;
    private static final int EVENT_TEXT_HEIGHT = 75;

    /**
     * Font für Toranzeige, falls diese aktiviert ist. (Derzeit nur bei einem neuen Darstellugnsfenster)
     */
    private final Font goalCountFont;
    private final boolean paintGoalCountActivated;
    private static final int GOAL_COUNT_PADDING = 15;
    private static final int GOAL_COUNT_HEIGHT = 30;
    private static final String GOAL_COUNT_DELIMITER = ":";
    private static final int GOAL_COUNT_Y_RECT = 3;
    private static final int GOAL_COUNT_Y_TEXT = 28;

    /**
     * Referenz auf FpsHelper zur Berechnung der angezeigten Zeichengeschwindigkeit
     */
    private final FpsHelper fps = new FpsHelper(1d);

    /**
     * Abstand (in Pixel), um welche das Hintergrundbild innerhalb des Panels auf der X-Achse verschoben gezeichnet
     * werden soll.
     */
    protected int backgroundOffsetX;

    /**
     * Abstand (in Pixel), um welche das Hintergrundbild innerhalb des Panels auf der Y-Achse verschoben gezeichnet
     * werden soll.
     */
    protected int backgroundOffsetY;

    /**
     * Angabe (in Pixel), mit welcher Breite das Hintergrundbild gezeichnet werden soll.
     */
    protected int backgroundPaintWidth;

    /**
     * Angabe (in Pixel), mit welcher Höhe das Hintergrundbild gezeichnet werden soll.
     */
    protected int backgroundPaintHeight;

    /**
     * Radius des Balls (in Spielgrößeneinheit).
     */
    protected static final double BALL_RADIUS = SoccerGUI.BALL_DIAMETER / 2;

    /**
     * Radius eines Spielers (in Spielgrößeneinheit).
     */
    protected static final double PLAYER_RADIUS = SoccerGUI.PLAYER_DIAMETER / 2;

    /**
     * Faktor, um welchen das Hintergrundbild vergrößert bzw. verkleinert (bezogen auf die unveränderte
     * Grafikabmessungen der Bilddatei) gezeichnet werden muss, damit die Größenverhältnisse der Simulationsobjekte in
     * der Darstellung und in der Simulation übereinstimmen. <br>
     * (Dieser Skalierungswert für die Hintergrundgrafik ist jedoch noch nicht ausreichend für die Skalierung der
     * einzelnen Simulationsobjekte. Jedoch ist die Skalierung dieser Objekte von diesem Wert abhängig).
     */
    protected double backgroundSizeFactor;

    /**
     * Referenz auf aktuellen TickData, welcher gezeichnet werden soll.
     */
    protected TickData currentTickData;

    /**
     * ComponentListener, welcher auf Größenänderungen des Fensters (und somit des Zeichenbereichs) reagiert. Dabei
     * werden neue Werte für die Darstellung berechnet (siehe SoccerPanel.recalculateConstraints) und das Panel neu
     * gezeichnet.
     */
    private ComponentListener onResize = new ComponentAdapter() {
        public void componentResized(ComponentEvent e) {
            recalculateConstraints();
            repaint();
        }
    };

    /**
     * Abstrakter Konstruktor dient zum Laden des angegebenen Hintergrundbildes sowie der Festlegung von Attributen,
     * welche im Kontext dieser Klasse konstant bleiben. <br>
     * Dazu gehören sowohl die Abmessungen des Hintergrundbildes als auch die zu verwendenden Farben zur Zeichnung von
     * Spieler. (Bei einer Änderung der Darstellungsoptionen durch den Benutzer wird die aktuelle Instanz verworfen und
     * diese mit einer neuen Instanz und angepassten Parametern erstellt.)
     * 
     * @param backgroundFileName
     *            Dateiname des zu verwendenden Haupt-Hintergrundbilds. Für weitere Informationen siehe
     *            {@link #loadImages(String)}
     * @param teamAColor
     *            Farbe, mit welcher die Spielfiguren des Teams A gezeichnet werden sollen.
     * @param teamBColor
     *            Farbe, mit welcher die Spielfiguren des Teams B gezeichnet werden sollen.
     * @param paintGoalCountActivated
     *            Flag, ob der Torstand im SoccerPanel gezeichnet werden soll.
     */
    public SoccerPanel(String backgroundFileName, Color teamAColor, Color teamBColor, boolean paintGoalCountActivated) {
        this.setPreferredSize(new Dimension(1200, 600));

        this.teamAColor = teamAColor;
        this.teamBColor = teamBColor;

        this.playerNumbersFont = new Font("Arial", Font.PLAIN, 12);
        this.goalCountFont = new Font("Arial", Font.BOLD, 25);
        this.eventFont = new Font("Arial", Font.PLAIN, 75);

        this.paintGoalCountActivated = paintGoalCountActivated;

        loadImages(backgroundFileName);

        ImageIcon ballIcon = new ImageIcon(getClass().getResource("/resources/images/ball.png"));
        this.ballImage = ballIcon.getImage();

        this.imageWidth = backgroundImagesOriginal.get(0).getWidth(null);
        this.imageHeight = backgroundImagesOriginal.get(0).getHeight(null);

        this.setBackground(Color.DARK_GRAY);

        this.addComponentListener(onResize);
    }

    /**
     * Lädt alle benötigten Bilder entsprechend des angegebenen Dateinamens. Dabei wird auf weitere Bilder für die
     * Animationwiedergabe überprüft.<br>
     * <br>
     * Kann unter dem angegebenen Dateinamen kein Bild geladen werden, so wird eine entsprechende
     * {@link NullPointerException} geworfen.<br>
     * <br>
     * Wurde das erste Bild erfolgreich geladen, so erfolgt der Versuch des Ladens weiterer Bilder für die
     * Animationsdarstellung.<br>
     * Lautet der angegebene Dateiname beispielsweise "background.jpg", so wird versucht, die Dateien
     * "background-02.jpg", "background-03.jpg" etc. zu laden.<br>
     * Das Scannen bricht ab, sobald entweder kein weiteres Bild (mit aufsteigender Nummerierung) gefunden wurde oder
     * das maximum der zu ladenden Bilder erreicht wird.<br>
     * <br>
     * Wird die maximal zu ladende Anzahl an Bilder überschritten, erfolgt eine entsprechende Warnungs-Ausgabe in der
     * Konsole.<br>
     * <br>
     * Falls {@link RuntimeArguments#isAnimationsDeactivated()} gesetzt, wird nur das Hauptbild eingelesen und die
     * Wiedergabe von Animationen sommit deaktiviert.
     * 
     * @param backgroundFileName
     *            Dateiname des zu verwendenden Haupt-Hintergrundbilds.
     */
    private void loadImages(String backgroundFileName) {
        boolean scanNextImage = true;
        int imageCounter = 1;

        // scan for multiple frames
        while (scanNextImage) {
            try {
                File f = new File(backgroundFileName);
                String filename = f.getName();
                String filenamePrefix = backgroundFileName.substring(0, filename.lastIndexOf('.'));
                String filenameSuffix = backgroundFileName.substring(filename.lastIndexOf('.'));

                // Für imageCounter > 1 entsprechendes Suffix für Bildernummerierung erzeugen. Für einstellige Zahlen
                // mit führender Null.
                String suffix = "";
                if (imageCounter != 1)
                    suffix = ((imageCounter <= 9) ? "-0" : "-") + Integer.toString(imageCounter);

                // Zu überpürfenden Dateinamen generieren.
                String checkFileName = "/resources/images/" + filenamePrefix + suffix + filenameSuffix;
                ImageIcon backroundIcon = new ImageIcon(getClass().getResource(checkFileName));
                this.backgroundImagesOriginal.add(backroundIcon.getImage());
                this.backgroundImagesScaled.add(backroundIcon.getImage());
                imageCounter++;

                // Falls keine Animationen angezeigt werden sollen, nach dem einlesen des ersten Bildes abbrechen.
                if (RuntimeArguments.getRuntimeArguments().isAnimationsDeactivated()) {
                    scanNextImage = false;
                }

                // Abbrechen, falls Maximum der einzulesenden Zahlen überschritten.
                if (imageCounter >= 32) {
                    System.err.println("The maximum count of sprite frames was exceeded!");
                    scanNextImage = false;
                }
            } catch (NullPointerException e) {
                // stop scanning
                scanNextImage = false;

                // throw exception, if not at least one image was found
                if (this.backgroundImagesOriginal.size() == 0)
                    throw e;
            }
        }
    }

    /**
     * Erzeugt skalierte Bilder für die Liste {@link #backgroundImagesScaled}.<br>
     * Dies dient zur besseren grafischen Darstellung, da in dieser Methode die Skalierung mit einer besseren
     * Interpoliert erfolgt. Des Weiteren dient die Methode zur Performancesteigerung, damit die Skalierung der Bilder
     * nur einmal zu erfolgen hat und anschlißened Zwischengespeichert werden kann.<br>
     * <br>
     * Die Methode überprüft bei jedem Aufruf, ob in der Liste {@link #backgroundImagesScaled} eine zu aktualiserende
     * Bildinstanz vorhanden ist. (Dies ist der Fall, wenn die Objektinstanz mit der aus
     * {@link #backgroundImagesOriginal} übereinstimmt.)<br>
     * <br>
     * Pro Aufruf der Methode wird maximal eine Bildinstanz aktualisiert. Dies erfolgt aus Performancegründen, damit bei
     * einer Sprite-Animation mit mehreren Frames nicht alle Instanzen gleichzeitig aktualisiert werden, was zu
     * GUI-Freezes führen könnte.<br>
     * <br>
     * Der Parameter doNewRescale gibt dabei an, ob die aktuellen skalierten Versionen verworfen werden sollen und alle
     * Bilder aus {@link #backgroundImagesOriginal} neu skaliert und in der Liste abgelegt werden sollen.<br>
     * Ist dieser Parameter auf <code>true</code> gesetzt, so werden sämtliche enthaltenen Objektinstanzen der Liste
     * {@link #backgroundImagesScaled} durch die enthaltenenen Objektinstanzen der Liste
     * {@link #backgroundImagesOriginal} ersetzt und es findet findet kein Skalierungsvorgang statt.<br>
     * Andernfalls wird wie entsprechend beschrieben pro Aufruf maximal eine Bildinstanz aktualisiert.
     * 
     * @param doNewRescale
     *            Gibt an, ob die aktuellen Skalierten Versionen verworfen werden sollen und ein neuer
     *            Skalierungsvorgang gestartet werden soll.
     */
    private void scaleImages(boolean doNewRescale) {
        if (doNewRescale) {
            for (int i = 0; i < this.backgroundImagesOriginal.size(); i++) {
                // Replace scaled image with original
                this.backgroundImagesScaled.add(i, this.backgroundImagesOriginal.get(i));
                if (this.backgroundImagesScaled.size() > i + 1) {
                    Image oldScaled = this.backgroundImagesScaled.remove(i + 1);
                    if (!this.backgroundImagesOriginal.contains(oldScaled)) {
                        oldScaled.flush();
                    }
                }
            }
        } else {
            // Check for new unscaled image. Rescale if found. Abort after one rescale (perfomance reasons)
            for (int i = 0; i < this.backgroundImagesOriginal.size(); i++) {
                // Check if rescale is necessary (if objects are same)
                if (this.backgroundImagesOriginal.get(i) == this.backgroundImagesScaled.get(i)
                        && backgroundPaintWidth != 0 && backgroundPaintHeight != 0) {
                    // Generate new scaled image
                    BufferedImage newScaledImage = new BufferedImage(backgroundPaintWidth, backgroundPaintHeight,
                            BufferedImage.TRANSLUCENT);
                    Graphics2D g2d = (Graphics2D) newScaledImage.getGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    AffineTransform aft = new AffineTransform();
                    aft.scale((double) backgroundPaintWidth / this.imageWidth, (double) backgroundPaintHeight
                            / this.imageHeight);
                    g2d.drawImage(this.backgroundImagesOriginal.get(i), aft, null);
                    g2d.dispose();

                    // Replace scaled Image
                    this.backgroundImagesScaled.add(i, newScaledImage);
                    if (this.backgroundImagesScaled.size() > i + 1) {
                        this.backgroundImagesScaled.remove(i + 1);
                    }

                    break;
                }
            }
        }
    }

    /**
     * Gibt das aktuell zu zeichnende Hintergrundbild zurück. Dieses ist dabei von der aktuellen Tickposition
     * (Wiedergabeposition) abhängig.<br>
     * <br>
     * Das zurückgegebene Bild entspricht dabei der skalierten Version, falls dieses bereits vorliegt. Andernfalls wird
     * die entsprechende unskalierte Bildversion zurückgegeben.
     * 
     * @return Das zum aktuellen Tick ({@link #currentTickData}) zugehörige (ggf. skalierte) Hintergrundbild.
     */
    private Image getCurrentScaledBackgroundImage() {
        int imageId = 0;

        // Null zurückgeben, falls Imagelisten leer
        if (this.backgroundImagesOriginal.size() == 0 || this.backgroundImagesOriginal.size() == 0) {
            return null;
        }

        // Spriteaktualisierung nur durchführen, wenn mehr als 1 Sprite vorhanden ist
        if (this.backgroundImagesOriginal.size() > 1) {
            TickData printTick = this.currentTickData;
            if (printTick != null)
                imageId = (int) Math.round(this.currentTickData.getTickPosition()
                        / (double) PlaybackHandler.TICKS_PER_SECOND * (double) ANIMATION_FPS
                        % this.backgroundImagesOriginal.size());
            else
                imageId = 0;

            if (imageId >= this.backgroundImagesOriginal.size())
                imageId = 0;
        }

        // Neue skalierte Bilderversion berechnen
        this.scaleImages(false);

        // Falls skalierte Version nicht verfügbar, auf Originalgrafik zurückweichen
        if (imageId >= this.backgroundImagesScaled.size()) {
            return this.backgroundImagesOriginal.get(imageId);
        } else {
            Image scaledImage = this.backgroundImagesScaled.get(imageId);

            if (scaledImage == null) {
                return this.backgroundImagesOriginal.get(imageId);
            } else {
                return scaledImage;
            }
        }
    }

    /**
     * Nimmt die neuen Darstellungsinformation entgegen und veranlasst das Neuzeichnen des Panels. <br>
     * {@inheritDoc}
     */
    public void refresh(TickData tickData, double playbackSpeed, int simulationTickPosition) {
        this.currentTickData = tickData;
        this.repaint();
    }

    /**
     * Für die Darstellung benötigte Werte werden berechnet und gespeichert. Die Werte sind abhängig von der aktuellen
     * Fenstergröße bzw. Zeichenfläche. <br>
     * Es wird bestimmt, wo und in welcher Breite/Höhe das Hintergrundbild gezeichnet werden soll (zentriert im Panel).
     * Zusäzlich wird der Größenfaktor für das Zeichnen von Simulationsobjekten festgelegt.
     */
    protected void recalculateConstraints() {
        int width = getWidth();
        int height = getHeight();

        if ((float) width / height > (float) imageWidth / imageHeight) {
            // set height, calculate width
            this.backgroundPaintHeight = height;
            this.backgroundPaintWidth = (int) Math.round((float) height / imageHeight * imageWidth);

            this.backgroundOffsetX = (width - backgroundPaintWidth) / 2;
            this.backgroundOffsetY = 0;
        } else {
            // set width, calculate height
            this.backgroundPaintWidth = width;
            this.backgroundPaintHeight = (int) Math.round((float) width / imageWidth * imageHeight);

            this.backgroundOffsetX = 0;
            this.backgroundOffsetY = (height - backgroundPaintHeight) / 2;
        }

        this.backgroundSizeFactor = (double) this.backgroundPaintWidth / this.imageWidth;

        this.scaleImages(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));

        // Hintergrund festlegen.
        Image backgroundToDraw = this.getCurrentScaledBackgroundImage();

        // Spielfeld zeichnen
        if (backgroundToDraw != null) {
            g2d.drawImage(backgroundToDraw, backgroundOffsetX, backgroundOffsetY, backgroundPaintWidth,
                    backgroundPaintHeight, null);
        }

        // Simulationsobjekte zeichnen
        if (currentTickData != null) {
            // Ballabmessungen bestimmen
            int ballWidth = getPaintWidth(this.currentTickData.getBallPosition(), SoccerGUI.BALL_DIAMETER);
            int ballHeight = getPaintHeight(this.currentTickData.getBallPosition(), SoccerGUI.BALL_DIAMETER);

            // Überprüfung, ob BallWidth > 0. Falls nicht, so wurde Background-Image noch nicht vollständig geladen
            if (ballWidth == 0) {
                return; // Image not loaded yet, don't draw anyhing more
            }

            // Spieler-Pixel-Daten berechnen
            List<ScreenPositioningData> postionsData = this.calculatePlayerScreenPositionData(
                    this.currentTickData.getPlayerPositionsTeamA(), this.teamAColor);
            postionsData.addAll(this.calculatePlayerScreenPositionData(this.currentTickData.getPlayerPositionsTeamB(),
                    this.teamBColor));

            // Alle Spieler zeichnen
            paintPlayers(g2d, postionsData);

            // Ball-Image zeichnen
            Point ballPos = positionToPixelCoordinates(this.currentTickData.getBallPosition());
            g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR));
            g2d.drawImage(this.ballImage, (int) Math.round(ballPos.getX() - ballWidth / 2f),
                    (int) Math.round(ballPos.getY() - ballHeight / 2f), ballWidth, ballHeight, null);

            // Spielstand zeichnen, falls aktiviert
            if (paintGoalCountActivated) {
                this.paintGoalCount(g2d);
            }

            this.printEvent(g2d);
        }

        // Debug-Grafiken zeichnen
        if (RuntimeArguments.getRuntimeArguments().isDebugGraphicsActivated()) {
            this.paintDebugGraphics(g);
        }

        // FPS ausgeben, falls per Kommandzeile aktiviert
        if (RuntimeArguments.getRuntimeArguments().isFpsDisplayActivated()) {
            fps.nextFrame();
            g2d.setFont(this.goalCountFont);
            g2d.setColor(Color.WHITE);
            g2d.drawString("FPS: " + Integer.toString(fps.getFps()), 20, 20);
        }
    }

    /**
     * Zeichnet das Torverhältniss
     * 
     * @param g2d
     */
    private void paintGoalCount(Graphics2D g2d) {
        int paintCenterX = backgroundOffsetX + backgroundPaintWidth / 2;

        g2d.setFont(this.goalCountFont);
        FontMetrics metrics = g2d.getFontMetrics();

        String teamACount = Integer.toString(this.currentTickData.getGoalsTeamA());
        String teamBCount = Integer.toString(this.currentTickData.getGoalsTeamB());

        int delimiterWidth = metrics.stringWidth(GOAL_COUNT_DELIMITER);
        int teamACountWidth = metrics.stringWidth(teamACount);
        int teamBCountWidth = metrics.stringWidth(teamBCount);
        int teamCountWidthMax = Math.max(teamACountWidth, teamBCountWidth);

        // Halbtransparentes Rechteck zeichnen
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.fillRect(paintCenterX - teamCountWidthMax - 2 * GOAL_COUNT_PADDING - delimiterWidth / 2, GOAL_COUNT_Y_RECT,
                (teamCountWidthMax + GOAL_COUNT_PADDING * 2) * 2 + delimiterWidth, GOAL_COUNT_HEIGHT);

        // Anzahl der Tore zeichnen
        g2d.setColor(this.teamAColor);
        g2d.drawString(teamACount, paintCenterX - delimiterWidth / 2 - GOAL_COUNT_PADDING
                - (teamCountWidthMax + teamACountWidth) / 2, GOAL_COUNT_Y_TEXT);

        g2d.setColor(this.teamBColor);
        g2d.drawString(teamBCount, paintCenterX + delimiterWidth / 2 + GOAL_COUNT_PADDING
                + (teamCountWidthMax - teamBCountWidth) / 2, GOAL_COUNT_Y_TEXT);

        // Delimiter zeichnen
        g2d.setColor(Color.WHITE);
        g2d.drawString(GOAL_COUNT_DELIMITER, paintCenterX - delimiterWidth / 2, GOAL_COUNT_Y_TEXT);
    }

    /**
     * Zeichnet ein Event, falls eins vorhanden und innerhalb des Anzeige-Zeitraum-Limit ist, an
     * 
     * @param g2d
     */
    private void printEvent(Graphics2D g2d) {
        if (this.eventToPrint != null && (System.currentTimeMillis() < this.eventToPrintStoptime)) {
            String eventText = "";
            Color textColor = null;

            final Color FOUL_COLOR = new Color(238, 118, 36, 180);

            switch (this.eventToPrint) {
            case GOAL_SCORED:
                eventText = "Tor!";
                textColor = new Color(11, 255, 108, 150);
                break;
            case FOUL_OFF:
                eventText = "Aus!";
                textColor = FOUL_COLOR;
                break;
            case FOUL_OFFSIDE:
                eventText = "Abseits!";
                textColor = FOUL_COLOR;
                break;
            case FOUL_TACKLING:
                eventText = "Foul!";
                textColor = FOUL_COLOR;
                break;
            case FREE_KICK:
                break;
            case HALFTIME:
                eventText = "Halbzeit";
                textColor = new Color(0, 0, 0, 180);
                break;
            case KICK_OFF:
                break;
            case PLAYBACK_ABORTED:
                break;
            case PLAYBACK_END_REACHED:
                eventText = "Spielende";
                textColor = new Color(0, 0, 0, 180);
                break;
            case PLAYBACK_PAUSED:
            case PLAYBACK_SPEED_CHANGED:
            case PLAYBACK_STARTED:
            case PLAYBACK_STEP:
            case SIMULATION_FINISHED:
                break;
            }

            int paintPadding = (int) Math.round(EVENT_FONT_PADDING * this.backgroundSizeFactor);
            g2d.setFont(this.eventFont);

            g2d.setColor(new Color(255, 255, 255, 55));
            FontMetrics metrics = g2d.getFontMetrics();

            g2d.fillRect(this.backgroundOffsetX + paintPadding - EVENT_BOX_PADDING, this.backgroundOffsetY
                    + this.backgroundPaintHeight - paintPadding - EVENT_TEXT_HEIGHT - EVENT_BOX_PADDING + 15,
                    metrics.stringWidth(eventText) + 2 * EVENT_BOX_PADDING, EVENT_TEXT_HEIGHT + 2 * EVENT_BOX_PADDING);

            g2d.setColor(textColor);
            g2d.drawString(eventText, this.backgroundOffsetX + paintPadding, this.backgroundOffsetY
                    + this.backgroundPaintHeight - paintPadding);
        } else {
            this.eventToPrint = null;
        }
    }

    /**
     * Konvertiert eine Positionsangabe, die sich auf die Fläche der Simulation bezieht, in Pixel-Koordinaten. Die
     * berechneten Pixel-Koordinaten entsprechen der Stelle, an welcher das jeweilige Objekt innerhalb des Panels seinen
     * Mittelpunkt hat.
     * 
     * @param position
     *            Positionsangabe (bezogen auf die Simulation).
     * @return Pixel-Koordinaten, an denen das Objekt innerhalb des Panels zu zeichnen ist.
     */
    protected abstract Point positionToPixelCoordinates(Position position);

    /**
     * Ermittelt die zu zeichnende Breite eines Simulationsobjekts. Dies erfolgt abhängig von der Position und der Größe
     * innerhalb des Simulationsspielfelds.
     * 
     * @param position
     *            Position des Objektes in der Simulation.
     * @param simulationDiameter
     *            Durchmesser des Objekts.
     * @return Zu zeichnende Breite des Objekts.
     */
    protected abstract int getPaintWidth(Position position, double simulationDiameter);

    /**
     * Ermittelt die zu zeichnende Höhe eines Simulationsobjekts. Dies erfolgt abhängig von der Position und der Größe
     * innerhalb des Simulationsspielfelds.
     * 
     * @param position
     *            Position des Objektes in der Simulation.
     * @param simulationDiameter
     *            Durchmesser des Objekts.
     * @return Zu zeichnende Höhe des Objekts.
     */
    protected abstract int getPaintHeight(Position position, double simulationDiameter);

    private List<ScreenPositioningData> calculatePlayerScreenPositionData(List<Position> positions, Color color) {
        List<ScreenPositioningData> postionsData = new LinkedList<ScreenPositioningData>();

        // Alle benötigten Pixelangaben der Spielerpositionen für Bildschirmausgabe berechnen
        for (Position playerPosition : positions) {
            int playerWidth = getPaintWidth(playerPosition, SoccerGUI.PLAYER_DIAMETER);
            int playerHeight = getPaintHeight(playerPosition, SoccerGUI.PLAYER_DIAMETER);

            Point screenCenterPosition = positionToPixelCoordinates(playerPosition);
            Point screenOvalPosition = new Point((int) Math.round(screenCenterPosition.getX() - playerWidth / 2f),
                    (int) Math.round(screenCenterPosition.getY() - playerHeight / 2f));

            postionsData.add(new ScreenPositioningData(screenCenterPosition, screenOvalPosition, playerWidth,
                    playerHeight, color));
        }

        return postionsData;
    }

    /**
     * Zeichnet einen Spieler, je nach Darstellung, als Kreis oder als Ellipse auf dem Panel. Dabei werden die
     * Koordinaten entsprechend umgewandelt und die Größe der Darstellung abgefragt.
     * 
     * @param g2d
     *            Graphics-Objekt des Panels, mit welchem das Zeichnen erfolgt.
     * @param color
     *            Farbe, mit welcher die Spieler gezeichnet werden sollen.
     * @param positions
     *            Positionen der zu zeichnenden Spieler.
     */
    private void paintPlayers(Graphics2D g2d, List<ScreenPositioningData> postionsData) {
        // Schatten zeichnen
        for (ScreenPositioningData screenPosition : postionsData) {
            Paint shaddowPaint = new GradientPaint(screenPosition.positionOval.x, screenPosition.positionOval.y,
                    new Color(0.0f, 0.0f, 0.0f, 0.0f), screenPosition.positionOval.x, screenPosition.positionOval.y
                            + screenPosition.height, new Color(0.0f, 0.0f, 0.0f, 0.4f));

            g2d.setPaint(shaddowPaint);
            g2d.fillOval(screenPosition.positionOval.x,
                    screenPosition.positionOval.y + (int) Math.round(screenPosition.height * 0.1),
                    (int) (Math.round(screenPosition.width * 1.2)), screenPosition.height);
        }

        // Spieler (als Kugeln) zeichnen
        for (ScreenPositioningData screenPosition : postionsData) {
            int playerWidth = screenPosition.width;
            int playerHeight = screenPosition.height;

            int ovalPositionX = screenPosition.positionOval.x;
            int ovalPositionY = screenPosition.positionOval.y;

            // Farben des Farbverlaufs generieren
            double colorFactor = 180;
            Color colorCenter = new Color(Math.min((int) (screenPosition.color.getRed() + colorFactor), 255), Math.min(
                    (int) (screenPosition.color.getGreen() + colorFactor), 255), Math.min(
                    (int) (screenPosition.color.getBlue() + colorFactor), 255), screenPosition.color.getAlpha());
            Color colorSides = screenPosition.color;

            Paint spherePaint = new RadialGradientPaint(new Point2D.Double(screenPosition.positionCenter.getX()
                    - playerWidth / 6d, screenPosition.positionCenter.getY() - playerHeight / 6d), playerWidth / 2f,
                    new float[] { 0.0f, 1.0f }, new Color[] { colorCenter, colorSides, });

            g2d.setPaint(spherePaint);
            g2d.fillOval(ovalPositionX, ovalPositionY, playerWidth, playerHeight);
        }

        // Spieler-Nummern zeichnen
        Color firstColor = null; // Für Fallunterscheidung, welches Team nummeriert wird
        int playerIdTeamA = 0;
        int playerIdTeamB = 0;
        g2d.setFont(this.playerNumbersFont);
        g2d.setColor(Color.WHITE);
        for (ScreenPositioningData screenPosition : postionsData) {
            if (firstColor == null)
                firstColor = screenPosition.color;

            // Use postfix! (id should start at 0)
            int playerId = (screenPosition.color == firstColor) ? playerIdTeamA++ : playerIdTeamB++;

            int y = (int) Math.round(screenPosition.height / 2d) + 2;

            String playerText = Integer.toString(playerId);
            int stringWidth = g2d.getFontMetrics().stringWidth(playerText);
            g2d.drawString(playerText, (int) screenPosition.positionCenter.getX() - stringWidth / 2,
                    (int) (screenPosition.positionCenter.getY() - y));
            playerId++;
        }
    }

    /**
     * Setzt ein Event, welches ausgegeben werden soll. Legt dabei den Zeitpunkt, wann die Anzeige wieder ausgeblendet
     * werden soll, fest.
     * 
     * @param updateEvent
     *            Das zu zeichnende Event
     */
    private void setEventToPrint(SoccerUpdateEvent updateEvent) {
        this.eventToPrint = updateEvent;
        this.eventToPrintStoptime = System.currentTimeMillis() + MAX_EVENT_DISPLAYTIME;
    }

    /**
     * Gibt die Image-Ressourcen frei. Diese Methode sollte nur aufgerufen werden, wenn die JPanel-Instanz nicht mehr
     * verwendet wird. Nach Aufruf der Funktion werden keine Hintergrundbilder mehr gezeichnet.
     */
    public void flushImageCache() {
        // Flush image cache
        for (Image imageScaled : this.backgroundImagesScaled) {
            imageScaled.flush();
        }

        // remove instances from lists (so GC can free memory)
        this.backgroundImagesOriginal.clear();
        this.backgroundImagesScaled.clear();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof SoccerUpdateEvent) {
            switch ((SoccerUpdateEvent) arg) {
            case FOUL_OFF:
            case FOUL_OFFSIDE:
            case FOUL_TACKLING:
            case GOAL_SCORED:
            case PLAYBACK_END_REACHED:
            case HALFTIME:
                setEventToPrint((SoccerUpdateEvent) arg);
                this.repaint();
                break;
            default:
                break;
            }
        }
    }

    /**
     * Beinhaltet für die Bildschirmausgabe benötigte Daten (insb. Positions- und Größenangaben in Pixel)<br>
     * Dadurch erfolgt die Berechnung der Daten nur einmal für sämtliche Spieler und muss nicht für jeden
     * Teil-Zeichenvorgang erneut durchgeführt werden. <br>
     * (zB erfolgt zuerst das Zeichnen des Schattens aller Spieler, dann der Kugeln und zuletzt der Spielernummern)
     */
    private class ScreenPositioningData {
        public final Point positionCenter;
        public final Point positionOval;
        public final int width;
        public final int height;
        public final Color color;

        public ScreenPositioningData(Point positionCenter, Point positionOval, int width, int height, Color color) {
            this.positionCenter = positionCenter;
            this.positionOval = positionOval;
            this.width = width;
            this.height = height;
            this.color = color;
        }
    }

    /**
     * Zeichnet Debug-Grafiken. Dies kann je nach erbender Klasse unterschiedlich sein. IdR werden die
     * Spielfeld-Begrenzungslinien gezeichnet, um die Positionierung der Zeichnung und des Hintergrundbilds zu
     * überprüfen.
     * 
     * @param g
     */
    abstract protected void paintDebugGraphics(Graphics g);
}
