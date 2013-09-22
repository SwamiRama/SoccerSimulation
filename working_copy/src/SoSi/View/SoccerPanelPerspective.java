package SoSi.View;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import SoSi.Model.SoSiPosition;

import sep.football.Position;

/**
 * Dient zur perspektivischen Darstellung der Wiedergabe.
 */
/**
 * Dient zur Darstellung des Spielfelds und der Simulationsobjekte. Die Darstellung erfolgt dabei perspektivisch als
 * Seitenansicht.<br>
 * Die Klasse enthält Methoden zur Berechnung der Skalierung der Simulationsobjekte sowie deren Zeichnung auf dem
 * JPanel.<br>
 * <b>Eine genauere Beschreibung, sowie die einzelnen Berechnungsschritte befinden sich in dem dazugehörigen Dokument im
 * Implementierungsbericht.</b>
 */
public class SoccerPanelPerspective extends SoccerPanel {

	/**
	 * Random generated
	 */
	private static final long serialVersionUID = 2667010779874313588L;

	/**
	 * Der Rotationswinkel > 0, der angibt, wie die Kamera auf das Spielfeld gerichtet ist.
	 */
	private static final double CAMERA_ROTATE_ANGLE = 270 + 61.075;

	/**
	 * Der Winkel, der das Blickfeld der Kamera aufspannt.
	 */
	private static final double CAMERA_ANGLE = 5;

	/**
	 * Linker oberer Punkt (in Pixel) auf dem Hintergrundbild, wo das Spielfeld beginnt.
	 */
	private final Point fieldTopLeft;

	/**
	 * rechter oberer Punkt (in Pixel) auf dem Hintergrundbild, wo das Spielfeld endet.
	 */
	private final Point fieldTopRight;

	/**
	 * Linker unterer Punkt (in Pixel) auf dem Hintergrundbild, wo das Spielfeld endet.
	 */
	private final Point fieldBottomLeft;

	/**
	 * Der Abstand (in Pixel) von dem linken Bildrand bis zur unteren, linken Trapez-Ecke.
	 */
	private int fieldOffsetX;

	/**
	 * Der Abstand (in Pixel) von dem oberen Bildrand bis zur oberen Trapezfläche.
	 */
	private int fieldOffsetY;

	/**
	 * Die Länge (in Pixel) der oberen Trapezseite.
	 */
	private int fieldPaintTopWidth;

	/**
	 * Die Länge (in Pixel) der unteren Trapezseite.
	 */
	private int fieldPaintBottomWidth;

	/**
	 * DIe Länge (in Pixel) der Trapezhöhe.
	 */
	private int fieldPaintHeight;

	/**
	 * Erzeugt ein neues Panel zur Darstellung des Spielfelds. Die Darstellung erfolgt dabei perspektivisch als
	 * Seitenansicht.
	 * 
	 * @param backgroundFileName
	 *            Dateiname des zu verwendenden Hintergrundbilds.
	 * @param teamAColor
	 *            Farbe, mit welcher die Spielfiguren des Teams A gezeichnet werden sollen.
	 * @param teamBColor
	 *            Farbe, mit welcher die Spielfiguren des Teams B gezeichnet werden soll.
	 * @param paintGoalCountActivated
	 *            Flag, ob der Torstand im SoccerPanel gezeichnet werden soll.
	 * @param fieldTopLeft
	 *            Linker oberer Punkt (in Pixel) auf dem Hintergrundbild, wo das Spielfeld beginnt.
	 * @param fieldTopRight
	 *            Rechter oberer Punkt (in Pixel) auf dem Hintergrundbild, wo das Spielfeld endet.
	 * @param fieldBottomLeft
	 *            Linker unterer Punkt (in Pixel) auf dem Hintergrundbild, wo das Spielfeld endet.
	 */
	public SoccerPanelPerspective(String backgroundFileName, Color teamAColor, Color teamBColor,
			boolean paintGoalCountActivated, Point fieldTopLeft, Point fieldTopRight, Point fieldBottomLeft) {
		super(backgroundFileName, teamAColor, teamBColor, paintGoalCountActivated);

		this.fieldTopLeft = fieldTopLeft;
		this.fieldTopRight = fieldTopRight;
		this.fieldBottomLeft = fieldBottomLeft;
	}

	/**
	 * Für die Darstellung benötigte Werte werden berechnet und gespeichert. Die Werte sind abhängig der aktuellen
	 * Fenstergröße bzw. Zeichenfläche. <br>
	 * Es wird bestimmt, wo und in welcher Breite/Höhe sich das Spielfeld (innerhalb des Panels) befindet. Abhängig von
	 * diesen Werten werden die Spielobjekte beim Zeichnen positioniert und ausgegeben.<br>
	 * Des weiteren werden notwendige Werte vorausberechnet, welche für die perspektivische Darstellung benötigt werden.
	 */
	protected void recalculateConstraints() {
		super.recalculateConstraints();

		this.fieldOffsetX = this.backgroundOffsetX + (int) Math.round(fieldBottomLeft.x * this.backgroundSizeFactor);
		this.fieldOffsetY = this.backgroundOffsetY + (int) Math.round(fieldTopLeft.y * this.backgroundSizeFactor);

		this.fieldPaintTopWidth = (int) Math.round((this.fieldTopRight.x - this.fieldTopLeft.x)
				* this.backgroundSizeFactor);

		this.fieldPaintBottomWidth = (int) Math
				.round(((this.fieldTopRight.x - this.fieldTopLeft.x) + (2d * (this.fieldTopLeft.x - this.fieldBottomLeft.x)))
						* this.backgroundSizeFactor);

		this.fieldPaintHeight = (int) Math.round((this.fieldBottomLeft.y - this.fieldTopLeft.y)
				* this.backgroundSizeFactor);
	}

	/**
	 * Rechnet die Differenz: nonPerspectiveYRatio zwischen einer Strecke zum oberen Rand des Spielfelds und der
	 * Spielfeldhöhe (in der Draufsicht) um, in die entsprechende Differenz in der perspektivischen Ansicht.<br>
	 * <b>Eine genauere Beschreibung, sowie die einzelnen Berechnungsschritte befinden sich in dem dazugehörigen
	 * Dokument im Implementierungsbericht.</b>
	 * 
	 * @param nonPerspectiveYRatio
	 *            Die Differenz zwischen einer Strecke zum oberen Rand des Spielfelds und der Spielfeldhöhe (in der
	 *            Draufsicht).
	 * @return Die Differenz zwischen einer Strecke zum oberen Rand des Spielfelds und der Spielfeldhöhe (in der
	 *         perspektivischen Ansicht).
	 */
	private double getPerspectiveYRatio(double nonPerspectiveYRatio) {

		/*
		 * Zum Einstellen des Werts der cameraAngle (Die Spielobjekte müssen dann auf einer Höhe mit dem Anstoßpunkt des
		 * Spielfelds sein):
		 * 
		 * nonPerspectiveYRatio = 0.5d;
		 */

		double whole = 100;
		double resWhole = 100;
		double part = nonPerspectiveYRatio * whole;

		double w0 = 360 - SoccerPanelPerspective.CAMERA_ROTATE_ANGLE;
		double w1 = 180 - SoccerPanelPerspective.CAMERA_ANGLE - (180 - w0);
		double w2 = 180 - SoccerPanelPerspective.CAMERA_ANGLE - w0;

		double s2 = (whole / Math.sin(Math.PI * (2 * SoccerPanelPerspective.CAMERA_ANGLE) / 180))
				* Math.sin(Math.PI * w1 / 180);
		double distance = Math.sqrt(Math.pow(s2, 2) + Math.pow(whole - part, 2)
				- (2 * s2 * (whole - part) * Math.cos(Math.PI * w2 / 180)));
		double w3 = Math.asin(((whole - part) * Math.sin(Math.PI * w2 / 180)) / distance) * (180d / Math.PI);
		double w4 = (2 * SoccerPanelPerspective.CAMERA_ANGLE) - w3;
		double w5 = 180 - 90 - SoccerPanelPerspective.CAMERA_ANGLE;
		double s3 = (0.5 * resWhole) / Math.cos(Math.PI * w5 / 180);
		double w6 = 180 - w4 - w5;
		double resPart = (s3 / Math.sin(Math.PI * w6 / 180)) * Math.sin(Math.PI * w4 / 180);

		return (resPart / resWhole);
	}

	@Override
	protected Point positionToPixelCoordinates(Position position) {
		double nonPerspectiveYRatio = position.getY() / SoccerGUI.FIELD_WIDTH;
		double perspectiveYRatio = this.getPerspectiveYRatio(nonPerspectiveYRatio);
		double y = perspectiveYRatio * this.fieldPaintHeight;
		double x = (((this.fieldPaintBottomWidth - this.fieldPaintTopWidth) / 2d) * (this.fieldPaintHeight - y))
				/ this.fieldPaintHeight;
		double positionFieldPaintWidth = this.fieldPaintBottomWidth - (2 * x);

		double dX = position.getX() * (positionFieldPaintWidth / SoccerGUI.FIELD_LENGTH);

		return new Point((int) Math.round(this.fieldOffsetX + x + dX), (int) Math.round(this.fieldOffsetY + y));
	}

	@Override
	protected int getPaintWidth(Position position, double simulationDiameter) {
		double nonPerspectiveYRatio = position.getY() / SoccerGUI.FIELD_WIDTH;
		double perspectiveYRatio = this.getPerspectiveYRatio(nonPerspectiveYRatio);
		double y = perspectiveYRatio * this.fieldPaintHeight;
		double x = (((this.fieldPaintBottomWidth - this.fieldPaintTopWidth) / 2d) * (this.fieldPaintHeight - y))
				/ this.fieldPaintHeight;
		double positionFieldPaintWidth = this.fieldPaintBottomWidth - (2 * x);

		double objectWidthSizeFactor = positionFieldPaintWidth / SoccerGUI.FIELD_LENGTH;

		return (int) Math.round(simulationDiameter * objectWidthSizeFactor);
	}

	@Override
	protected int getPaintHeight(Position position, double simulationDiameter) {

		double nonPerspectiveTopCircleRatio = (position.getY() - (simulationDiameter / 2d)) / SoccerGUI.FIELD_WIDTH;
		double nonPerspectiveBottomCircleRatio = (position.getY() + (simulationDiameter / 2d)) / SoccerGUI.FIELD_WIDTH;

		double perspectiveTopCircleRatio = this.getPerspectiveYRatio(nonPerspectiveTopCircleRatio);
		double perspectiveBottomCircleRatio = this.getPerspectiveYRatio(nonPerspectiveBottomCircleRatio);

		return (int) Math.round((perspectiveBottomCircleRatio * this.fieldPaintHeight)
				- (perspectiveTopCircleRatio * this.fieldPaintHeight));
	}

	@Override
	protected void paintDebugGraphics(Graphics g) {
		g.setColor(Color.RED);
		int dbgRadius = 7;

		// Spielfeldecken und Rechteck markieren
		java.util.LinkedList<Point> points = new java.util.LinkedList<Point>();
		points.add(new Point(
				this.backgroundOffsetX + (int) Math.round(this.fieldTopLeft.x * this.backgroundSizeFactor),
				this.backgroundOffsetY + (int) Math.round(this.fieldTopLeft.y * this.backgroundSizeFactor)));

		points.add(new Point(this.backgroundOffsetX
				+ (int) Math.round(this.fieldTopRight.x * this.backgroundSizeFactor), this.backgroundOffsetY
				+ (int) Math.round(this.fieldTopRight.y * this.backgroundSizeFactor)));

		int xRightDown = this.fieldTopLeft.x - this.fieldBottomLeft.x + this.fieldTopRight.x;
		points.add(new Point(this.backgroundOffsetX + (int) Math.round(xRightDown * this.backgroundSizeFactor),
				this.backgroundOffsetY + (int) Math.round(this.fieldBottomLeft.y * this.backgroundSizeFactor)));

		points.add(new Point(this.backgroundOffsetX
				+ (int) Math.round(this.fieldBottomLeft.x * this.backgroundSizeFactor), this.backgroundOffsetY
				+ (int) Math.round(this.fieldBottomLeft.y * this.backgroundSizeFactor)));

		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			g.drawOval(p.x - dbgRadius, p.y - dbgRadius, 2 * dbgRadius, 2 * dbgRadius);

			Point q = points.get((i == 3 ? 0 : i + 1));
			g.drawLine(p.x, p.y, q.x, q.y);
		}

		// Pfosten markieren
		g.setColor(Color.BLUE);
		int y1 = (int) Math.round((SoccerGUI.FIELD_WIDTH + SoccerGUI.GOAL_SIZE) / 2);
		int y2 = (int) Math.round((SoccerGUI.FIELD_WIDTH - SoccerGUI.GOAL_SIZE) / 2);

		Point leftPost1 = this.positionToPixelCoordinates(new SoSiPosition(0, y1));
		Point leftPost2 = this.positionToPixelCoordinates(new SoSiPosition(0, y2));
		Point rightPost1 = this.positionToPixelCoordinates(new SoSiPosition(SoccerGUI.FIELD_LENGTH, y1));
		Point rightPost2 = this.positionToPixelCoordinates(new SoSiPosition(SoccerGUI.FIELD_LENGTH, y2));

		dbgRadius = 4;
		g.drawOval(leftPost1.x - dbgRadius, leftPost1.y - dbgRadius, dbgRadius * 2, dbgRadius * 2);
		g.drawOval(leftPost2.x - dbgRadius, leftPost2.y - dbgRadius, dbgRadius * 2, dbgRadius * 2);
		g.drawOval(rightPost1.x - dbgRadius, rightPost1.y - dbgRadius, dbgRadius * 2, dbgRadius * 2);
		g.drawOval(rightPost2.x - dbgRadius, rightPost2.y - dbgRadius, dbgRadius * 2, dbgRadius * 2);
	}
}
