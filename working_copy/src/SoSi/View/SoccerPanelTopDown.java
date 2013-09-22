package SoSi.View;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import sep.football.Position;

/**
 * Dient zur Darstellung des Spielfeld und der Simulationsobjekte. Die Darstellung erfolgt dabei als Draufsicht ohne
 * perspektivischer Verzerrung. <br>
 * Die Klasse enthält Methoden zur Berechnung der Skalierung der Simulationsobjekte sowie deren Zeichnung auf dem
 * JPanel.
 */
public class SoccerPanelTopDown extends SoccerPanel {

    /**
     * Random generated
     */
    private static final long serialVersionUID = 7576010213040959773L;

    /**
     * Linker oberer Punkt (in Pixel) auf dem Hintergrundbild, wo das Spielfeld beginnt. <br>
     * (Das Hintergrundbild enthält ggf. noch Umgebung, Tore etc. Die Position gibt daher an, wo sich das linke obere
     * Eck (= Position einer Eckfahne) der Spielfeldmarkierung innerhalb des Hintergrundbilds befindet).
     */
    private final Point fieldTopLeft;
    /**
     * Rechter unterer Punkt (in Pixel) auf dem Hintergrundbild, wo das Spielfeld endet. <br>
     * (Das Hintergrundbild enthält ggf. noch Umgebung, Tore etc. Die Position gibt daher an, wo sich das rechte unere
     * Eck (= Position einer Eckfahne) der Spielfeldmarkierung innerhalb des Hintergrundbilds befindet).
     */
    private final Point fieldBottomRight;

    /**
     * Abstand (in Pixel) vom rechten Rand des Panels, ab welchem das Spielfeld (nicht das Hintergrundbild) beginnt und
     * entsprechend gezeichnet werden soll.
     */
    protected int fieldOffsetX;
    /**
     * Abstand (in Pixel) vom oberen Rand des Panels, ab welchem das Spielfeld (nicht das Hintergrundbild) beginnt und
     * entsprechend gezeichnet werden soll.
     */
    protected int fieldOffsetY;
    /**
     * Angabe (in Pixel), mit welcher Breite das Spielfeld (nicht das Hintergrundbild) gezeichnet werden soll.
     */
    protected int fieldPaintWidth;
    /**
     * Angabe (in Pixel), mit welcher Höhe das Spielfeld (nicht das Hintergrundbild) gezeichnet werden soll.
     */
    protected int fieldPaintHeight;

    /**
     * Faktor, um welchen Spielobjekte vergrößert bzw. verkleinert (bezogen auf die Größe innerhalb der Simulation)
     * gezeichnet werden müssen, damit die Größenverhältnisse der Simulationsobjekte in der Darstellung und in der
     * Simulation übereinstimmen.
     */
    protected double objectsSizeFactor;

    /**
     * Erzeugt ein neues Panel zur Darstellung des Spielfelds. Die Darstellung erfolgt als Draufsicht ohne
     * perspektivischer Verzerrung.
     * 
     * @param backgroundFileName
     *            Dateiname des zu verwendenden Hintergrundbilds.
     * @param teamAColor
     *            Farbe, mit welcher die Spielfiguren des Teams A gezeichnet werden sollen.
     * @param teamBColor
     *            Farbe, mit welcher die Spielfiguren des Teams B gezeichnet werden sollen.
     * @param paintGoalCountActivated
     *            Flag, ob der Torstand im SoccerPanel gezeichnet werden soll
     * @param fieldTopLeft
     *            Linker oberer Punkt (in Pixel) auf dem Hintergrundbild, wo das Spielfeld beginnt.
     * @param fieldBottomRight
     *            Rechter unterer Punkt (in Pixel) auf dem Hintergrundbild, wo das Spielfeld endet.
     */
    public SoccerPanelTopDown(String backgroundFileName, Color teamAColor, Color teamBColor,
            boolean paintGoalCountActivated, Point fieldTopLeft, Point fieldBottomRight) {
        super(backgroundFileName, teamAColor, teamBColor, paintGoalCountActivated);

        this.fieldTopLeft = fieldTopLeft.getLocation(); // clone positions
        this.fieldBottomRight = fieldBottomRight.getLocation();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Point positionToPixelCoordinates(Position position) {
        int x = (int) Math.round(position.getX() / SoccerGUI.FIELD_LENGTH * this.fieldPaintWidth) + fieldOffsetX;
        int y = (int) Math.round(position.getY() / SoccerGUI.FIELD_WIDTH * this.fieldPaintHeight) + fieldOffsetY;

        return new Point(x, y);
    }

    /**
     * Für die Darstellung benötigte Werte werden berechnet und gespeichert. Die Werte sind abhängig von der aktuellen
     * Fenstergröße bzw. Zeichenfläche. <br>
     * Es wird bestimmt, wo und in welcher Breite/Höhe sich das Spielfeld (innerhalb des Panels) befindet. Abhängig von
     * diesen Werten werden die Spielobjekte beim Zeichnen positioniert und ausgegeben.
     */
    protected void recalculateConstraints() {
        super.recalculateConstraints();

        this.fieldOffsetX = this.backgroundOffsetX + (int) Math.round(fieldTopLeft.x * this.backgroundSizeFactor);
        this.fieldOffsetY = this.backgroundOffsetY + (int) Math.round(fieldTopLeft.y * this.backgroundSizeFactor);

        this.fieldPaintWidth = (int) Math.round((this.fieldBottomRight.x - this.fieldTopLeft.x)
                * this.backgroundSizeFactor);

        this.fieldPaintHeight = (int) Math.round((this.fieldBottomRight.y - this.fieldTopLeft.y)
                * this.backgroundSizeFactor);

        this.objectsSizeFactor = fieldPaintWidth / SoccerGUI.FIELD_LENGTH;
    }

    @Override
    protected int getPaintWidth(Position position, double simulationDiameter) {
        return (int) Math.round(simulationDiameter * this.objectsSizeFactor);
    }

    @Override
    protected int getPaintHeight(Position position, double simulationDiameter) {
        return getPaintWidth(position, simulationDiameter);
    }

    @Override
    protected void paintDebugGraphics(Graphics g) {
        /*
         * // Pseudo-Spielfiguren zeichnen List<Position> positions = new LinkedList<Position>();
         * 
         * positions.add(new SoSiPosition(0, 0)); positions.add(new SoSiPosition(SoccerGUI.FIELD_LENGTH,
         * SoccerGUI.FIELD_WIDTH)); positions.add(new SoSiPosition(SoccerGUI.FIELD_LENGTH / 2, SoccerGUI.FIELD_WIDTH));
         * positions.add(new SoSiPosition(SoccerGUI.FIELD_LENGTH, SoccerGUI.FIELD_WIDTH / 2)); positions.add(new
         * SoSiPosition(SoccerGUI.FIELD_LENGTH / 2, SoccerGUI.FIELD_WIDTH / 2)); paintPlayers(g, Color.ORANGE,
         * positions);
         */

        g.setColor(Color.RED);
        int dbgRadius = 7;

        // Rechteck ums Spielfeld
        g.drawRect(fieldOffsetX, fieldOffsetY, fieldPaintWidth, fieldPaintHeight);

        // Spielfeldecken markieren
        g.drawOval(fieldOffsetX - dbgRadius, fieldOffsetY - dbgRadius, dbgRadius * 2, dbgRadius * 2);
        g.drawOval(fieldOffsetX - dbgRadius + fieldPaintWidth, fieldOffsetY - dbgRadius, dbgRadius * 2, dbgRadius * 2);
        g.drawOval(fieldOffsetX - dbgRadius, fieldOffsetY + fieldPaintHeight - dbgRadius, dbgRadius * 2, dbgRadius * 2);
        g.drawOval(fieldOffsetX - dbgRadius + fieldPaintWidth, fieldOffsetY + fieldPaintHeight - dbgRadius,
                dbgRadius * 2, dbgRadius * 2);

        // Pfosten markieren
        g.setColor(Color.BLUE);
        int y1 = (int) Math.round((SoccerGUI.FIELD_WIDTH + SoccerGUI.GOAL_SIZE) / 2);
        int y2 = (int) Math.round((SoccerGUI.FIELD_WIDTH - SoccerGUI.GOAL_SIZE) / 2);

        Point leftPost1 = this.positionToPixelCoordinates(new SoSi.Model.SoSiPosition(0, y1));
        Point leftPost2 = this.positionToPixelCoordinates(new SoSi.Model.SoSiPosition(0, y2));
        Point rightPost1 = this.positionToPixelCoordinates(new SoSi.Model.SoSiPosition(SoccerGUI.FIELD_LENGTH, y1));
        Point rightPost2 = this.positionToPixelCoordinates(new SoSi.Model.SoSiPosition(SoccerGUI.FIELD_LENGTH, y2));

        dbgRadius = 4;
        g.drawOval(leftPost1.x - dbgRadius, leftPost1.y - dbgRadius, dbgRadius * 2, dbgRadius * 2);
        g.drawOval(leftPost2.x - dbgRadius, leftPost2.y - dbgRadius, dbgRadius * 2, dbgRadius * 2);
        g.drawOval(rightPost1.x - dbgRadius, rightPost1.y - dbgRadius, dbgRadius * 2, dbgRadius * 2);
        g.drawOval(rightPost2.x - dbgRadius, rightPost2.y - dbgRadius, dbgRadius * 2, dbgRadius * 2);

        // Anstoßkreis zeichnen
        // Graphics2D g2D = (Graphics2D) g;
        // g2D.setStroke(new BasicStroke(2F));
        //
        // int kickOfRadius = (int) Math.round(125 * this.backgroundSizeFactor);
        //
        // g.drawOval(fieldOffsetX + fieldPaintWidth / 2 - kickOfRadius, fieldOffsetY + fieldPaintHeight / 2
        // - kickOfRadius, kickOfRadius * 2, kickOfRadius * 2);
    }
}
