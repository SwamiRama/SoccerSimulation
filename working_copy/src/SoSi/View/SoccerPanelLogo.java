package SoSi.View;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import sep.football.Position;

/**
 * SoccerPanel Instanz, welche ausschlieﬂlich ein Logo anzeigt.
 */
public class SoccerPanelLogo extends SoccerPanel {

    /**
     * Random generated.
     */
    private static final long serialVersionUID = 8006894098283320589L;

    /**
     * Erstellt eine neue SoccerPanelLogo-Instanz. Die anzuzeigende Bilddatei ist dabei bereits festgelegt.
     */
    public SoccerPanelLogo() {
        super("SoSiLogo.png", Color.BLACK, Color.WHITE, false);
    }

    @Override
    protected Point positionToPixelCoordinates(Position position) {
        return new Point(0, 0);
    }

    @Override
    protected int getPaintWidth(Position position, double simulationDiameter) {
        return 1;
    }

    @Override
    protected int getPaintHeight(Position position, double simulationDiameter) {
        return 1;
    }

    @Override
    public void refresh(SoSi.Model.TickData tickData, double playbackSpeed, int simulationTickPosition) {
        // Nothing to draw here
    };

    @Override
    protected void paintDebugGraphics(Graphics g) {
        // Nothing to debug here
    }

}
