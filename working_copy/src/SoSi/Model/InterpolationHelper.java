package SoSi.Model;

import java.util.LinkedList;
import java.util.List;

import sep.football.Position;

public class InterpolationHelper {

    /**
     * Liefert eine interpolierte TickData-Instanz zurück.
     * 
     * @param currentTickData
     *            Die aktuell angezeigte {@link TickData}-Instanz.
     * @param nextTickData
     *            Die nächste anzuzeigende {@link TickData}-Instanz.
     * @param percent
     *            Der Fortschritt in Prozent (0.0 bis 1.0), wie weit die Wiedergabe fortgeschritten ist und daher
     *            interpoliert werden soll.
     * @return Die interpolierte {@link TickData}-Instanz
     */
    public static TickData getInterpolatedTickData(TickData currentTickData, TickData nextTickData, double percent) {
        if (currentTickData == null)
            return null;
        else if (nextTickData == null)
            return currentTickData;
        else if ((currentTickData.getTickEvent() == TickEvent.GAME_INTERRUPTED && nextTickData.getTickEvent() != TickEvent.GAME_INTERRUPTED)
                || (currentTickData.getTickEvent() != TickEvent.GAME_INTERRUPTED && nextTickData.getTickEvent() == TickEvent.GAME_INTERRUPTED)) {
            // Keine Interpolation bei Sprüngen
            return currentTickData;
        }

        Position interpolatedBallPosition = getInterpolatedPosition(currentTickData.getBallPosition(),
                nextTickData.getBallPosition(), percent);
        List<Position> interpolatedPlayerPositionsTeamA = getInterpolatedPosition(
                currentTickData.getPlayerPositionsTeamA(), nextTickData.getPlayerPositionsTeamA(), percent);
        List<Position> interpolatedPlayerPositionsTeamB = getInterpolatedPosition(
                currentTickData.getPlayerPositionsTeamB(), nextTickData.getPlayerPositionsTeamB(), percent);

        return new TickData(currentTickData.getTickPosition(), currentTickData.getGoalsTeamA(),
                currentTickData.getGoalsTeamB(), interpolatedBallPosition, interpolatedPlayerPositionsTeamA,
                interpolatedPlayerPositionsTeamB, currentTickData.getTickEvent(),
                currentTickData.getDebugMessageTeamA(), currentTickData.getDebugMessageTeamB());
    }

    /**
     * Interpoliert zwei Positionsangaben.
     * 
     * @param currentPosition
     *            Die aktuell angezeigte {@link Position}-Instanz.
     * @param nextPosition
     *            Die nächste anzuzeigende {@link Position}-Instanz.
     * @param percent
     *            Der Fortschritt in Prozent (0.0 bis 1.0), wie weit die Wiedergabe fortgeschritten ist und daher
     *            interpoliert werden soll.
     * @return Die interpolierte {@link Position}-Instanz.
     */
    private static Position getInterpolatedPosition(Position currentPosition, Position nextPosition, double percent) {
        if (percent < 0)
            throw new IllegalArgumentException("percent may not be lower than 0");
        else if (percent > 1)
            throw new IllegalArgumentException("percent may not be taller than 1");

        double x = currentPosition.getX() + (nextPosition.getX() - currentPosition.getX()) * percent;
        double y = currentPosition.getY() + (nextPosition.getY() - currentPosition.getY()) * percent;

        return new SoSiPosition(x, y);
    }

    /**
     * Interpoliert zwei Listen mit Positionsangaben.
     * 
     * @param currentPositions
     *            Die aktuell angezeigten {@link Position}-Instanzen.
     * @param nextPositions
     *            Die nächsten anzuzeigenden {@link Position}-Instanzen.
     * @param percent
     *            Der Fortschritt in Prozent (0.0 bis 1.0), wie weit die Wiedergabe fortgeschritten ist und daher
     *            interpoliert werden soll.
     * @return Eine {@link List}e mit den interpolierten {@link Position}-Instanzen.
     */
    private static List<Position> getInterpolatedPosition(List<Position> currentPositions,
            List<Position> nextPositions, double percent) {
        if (currentPositions.size() != nextPositions.size())
            throw new IllegalArgumentException("Positionlists must have same item count");

        LinkedList<Position> interpolatedPosition = new LinkedList<Position>();

        for (int i = 0; i < currentPositions.size(); i++) {
            interpolatedPosition.add(getInterpolatedPosition(currentPositions.get(i), nextPositions.get(i), percent));
        }

        return interpolatedPosition;
    }

}
