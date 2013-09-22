package SoSi.View;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalSliderUI;

/**
 * Eine angepasste Java Look and Feel Implementierung von SliderUI. <br>
 * Die Funktionalität von MetalSliderUI wird um die Darstellung eines Ladebalken (zur Darstellung der Fortschritt der
 * (Berechnung der) Simulation) erweitert.
 */
public class SoccerTimeLineUi extends MetalSliderUI {

    /**
     * Fortschritt der Berechnung (= Bis zu welcher Position die Zeitleiste eingefärbt werden soll)
     */
    private int calculationPosition = 0;

    /**
     * Returns a rectangle enclosing the track that will be painted.
     */
    private Rectangle getPaintTrackRect() {
        int trackLeft = 0, trackRight, trackTop = 0, trackBottom;
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            trackBottom = (trackRect.height - 1) - getThumbOverhang();
            trackTop = trackBottom - (getTrackWidth() - 1);
            trackRight = trackRect.width - 1;
        } else {

            trackLeft = (trackRect.width - getThumbOverhang()) - getTrackWidth();
            trackRight = (trackRect.width - getThumbOverhang()) - 1;

            trackBottom = trackRect.height - 1;
        }
        return new Rectangle(trackRect.x + trackLeft, trackRect.y + trackTop, trackRight - trackLeft, trackBottom
                - trackTop);
    }

    @Override
    public void paintTrack(Graphics g) {
        boolean drawInverted = drawInverted();
        Color sliderAltTrackColor = (Color) UIManager.get("Slider.altTrackColor");

        // Translate to the origin of the painting rectangle
        Rectangle paintRect = getPaintTrackRect();
        g.translate(paintRect.x, paintRect.y);

        // Width and height of the painting rectangle.
        int w = paintRect.width;
        int h = paintRect.height;

        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int middleOfThumb = thumbRect.x + thumbRect.width / 2 - paintRect.x;

            if (slider.isEnabled()) {
                int fillMinX;
                int fillMaxX;

                if (middleOfThumb > 0) {
                    g.setColor(drawInverted ? MetalLookAndFeel.getControlDarkShadow() : MetalLookAndFeel
                            .getPrimaryControlDarkShadow());

                    g.drawRect(0, 0, middleOfThumb - 1, h - 1);
                }

                if (middleOfThumb < w) {
                    g.setColor(drawInverted ? MetalLookAndFeel.getPrimaryControlDarkShadow() : MetalLookAndFeel
                            .getControlDarkShadow());

                    g.drawRect(middleOfThumb, 0, w - middleOfThumb - 1, h - 1);
                }

                g.setColor(MetalLookAndFeel.getPrimaryControlShadow());

                fillMinX = 1;
                // fillMaxX = middleOfThumb + 120;
                fillMaxX = this.xPositionForValue(this.calculationPosition) - paintRect.x - 2;
                g.drawLine(middleOfThumb, 1, w - 1, 1);

                if (h == 6) {
                    // CHANGE COLOR HERE
                    g.setColor(MetalLookAndFeel.getWhite());
                    g.drawLine(fillMinX, 1, fillMaxX, 1);
                    g.setColor(sliderAltTrackColor);
                    g.drawLine(fillMinX, 2, fillMaxX, 2);
                    g.setColor(MetalLookAndFeel.getControlShadow());
                    g.drawLine(fillMinX, 3, fillMaxX, 3);
                    g.setColor(MetalLookAndFeel.getPrimaryControlShadow());
                    g.drawLine(fillMinX, 4, fillMaxX, 4);
                }

            } else {
                // Slider disabled
                g.setColor(MetalLookAndFeel.getControlShadow());

                if (middleOfThumb > 0) {
                    if (!drawInverted && filledSlider) {
                        g.fillRect(0, 0, middleOfThumb - 1, h - 1);
                    } else {
                        g.drawRect(0, 0, middleOfThumb - 1, h - 1);
                    }
                }

                if (middleOfThumb < w) {
                    if (drawInverted && filledSlider) {
                        g.fillRect(middleOfThumb, 0, w - middleOfThumb - 1, h - 1);
                    } else {
                        g.drawRect(middleOfThumb, 0, w - middleOfThumb - 1, h - 1);
                    }
                }
            }
        }

        g.translate(-paintRect.x, -paintRect.y);
    }

    /**
     * Setzt die Fortschrittsanzeige der (Berechnung der) Simulation an eine bestimmte Stelle.
     * 
     * @param tick
     *            Die Stelle, an der die Berechnungen bereits gerade ist.
     * 
     */
    public void updateSimulationTickPosition(int tick) {
        this.calculationPosition = tick;
    }

}
