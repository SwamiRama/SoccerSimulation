package SoSi.Model.Calculation;

import java.util.List;

import sep.football.Position;
import SoSi.Model.TickEvent;

/**
 * Interface, welches dazu dient, dem CalculationThread eine Referenz auf die innere Klasse von DataHandler, welche
 * {@link INewTickDataHandler} implementiert, zu übergeben. Diese wird verwendet, um neue Tick-Daten zur Liste der
 * berechneten Tick-Daten hinzuzufügen.
 */
public interface INewTickDataHandler {
    /**
     * Methode zum Hinzufügen neuer Tick-Daten. Dient dazu den Zugriff auf die Liste mit den berechneten Tick-Daten im
     * DataHandler auf den CalculationThread zu beschränken und zusätzlich um seine Zugriffe auf den DataHandler auf das
     * Minimum zu beschränken.
     */
    void addNewTickData(int goalsTeamA, int goalsTeamB, Position ballPosition, List<Position> playerPositionsTeamA,
            List<Position> playerPositionsTeamB, TickEvent tickEvent, String debugMessageTeamA, String debugMessageTeamB);
}
