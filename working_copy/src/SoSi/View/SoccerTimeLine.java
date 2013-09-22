package SoSi.View;

import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;

import SoSi.Model.PlaybackHandler;

/**
 * Die Zeitleiste gibt mit Hilfe des Knopfes an, wo sich die Wiedergabe gerade befindet. Außerdem zeigt sie über die
 * Einfärbung des Hintergrunds an, wie weit die (Berechnung der) Simulation fortgeschritten ist.
 */
public class SoccerTimeLine extends JSlider {

    /**
     * Random generated.
     */
    private static final long serialVersionUID = 3051924345056765666L;

    /**
     * Flag, ob Zeitleistenreglerknopf von User oder von Programm verschoben wurde. Dient zum verhindern, dass eine
     * Endlosschleife beim aktualisieren der Wiedergabepostion entsteht.
     */
    private boolean isChangedByUser = true;

    /**
     * Erstellt eine neue Zeitleiste.
     */
    public SoccerTimeLine() {
        super();

        this.setUI(new SoccerTimeLineUi());
        this.setPaintTicks(true);
        this.setPaintLabels(true);
    }

    @Override
    public void setMaximum(int maxTicks) {
        super.setMaximum(maxTicks);

        int majorSpacing;

        if (maxTicks % (9 * PlaybackHandler.TICKS_PER_SECOND * 60) == 0)
            majorSpacing = maxTicks / 9;
        else if (maxTicks % (11 * PlaybackHandler.TICKS_PER_SECOND * 60) == 0)
            majorSpacing = maxTicks / 11;
        else
            majorSpacing = maxTicks / 10;

        int minorSpacing = majorSpacing / 4;

        this.setMajorTickSpacing(majorSpacing);
        this.setMinorTickSpacing(minorSpacing);

        // LabelTable generieren
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();

        // Beschriftungen für Spezialpositionen der LabelTable
        int halftimeTick = maxTicks / 2;
        labelTable.put(new Integer(0), new JLabel(" Anstoß"));
        labelTable.put(new Integer(halftimeTick), new JLabel("Halbzeit"));
        labelTable.put(new Integer(maxTicks), new JLabel("Abpfiff "));

        // Beschriftung verbleibender Positionen
        for (int i = majorSpacing; i < maxTicks; i += majorSpacing) {
            if (i != halftimeTick) {
                Double minute = (double) i / (60 * PlaybackHandler.TICKS_PER_SECOND);
                String caption;

                if (minute == (double) minute.intValue())
                    caption = Integer.toString(minute.intValue());
                else
                    caption = String.format("%.1f", minute);

                labelTable.put(i, new JLabel(caption));
            }
        }

        this.setLabelTable(labelTable);
    };

    /**
     * Setzt die Fortschrittsanzeige der (Berechnung der) Simulation an eine bestimmte Stelle.
     * 
     * @param tick
     *            Die Stelle, an der die Berechnungen bereits gerade ist.
     */
    public void updateSimulationTickPosition(int tick) {
        ((SoccerTimeLineUi) (this.ui)).updateSimulationTickPosition(tick);
        this.repaint();

    }

    /**
     * Setzt die Position auf einen neuen Wert mit dem Flag, dass diese Änderungen durch die Programmlogik und nicht
     * durch Benutzereingaben enstanden ist.
     * 
     * @param value
     *            Neue Wiedergabeposition
     */
    public void setValueWithoutNotify(int value) {
        this.isChangedByUser = false;

        this.setValue(value);

        this.isChangedByUser = true;
    }

    /**
     * Gibt zurück, ob die Änderung vom Benutzer ausgelöst wurde.
     * 
     * @return <code>true</code> falls die Änderung vom Benutzer ausgelöst wurde, <code>false</code> andernfalls.
     */
    public boolean isChangedByUser() {
        return this.isChangedByUser;
    }

}
