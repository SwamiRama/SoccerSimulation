package SoSi.Model.Calculation;

import java.util.List;

import SoSi.Model.SimulationOptions;
import SoSi.Model.TickData;
import sep.football.AI;
import sep.football.GameInformation;

/**
 * Dient zur Vorausberechnung von Simulationsschritten (Ticks). Die Berechnung erfolgt asynchron in einem Thread.
 * Nachdem ein neues Datenelement ({@link TickData}) berechnet wurde, wird dieses über die Schnittstelle
 * {@link INewTickDataHandler} an eine andere Klasse weitergegeben. Im konkreten Fall enthält {@link DataHandler} eine
 * innere Klasse, welche {@link INewTickDataHandler} implementiert. Diese nimmt die neu berechneten Daten entgegen und
 * fügt diese der Liste mit sämtlichen {@link TickData} hinzu.
 * 
 * @see DataHandler
 */
public class CalculationThread extends AbortableThread {

    /**
     * Referenz auf die innere Klasse des DataHandler. Sie dient dazu, neue tick Daten in die Liste der bereits
     * berechneten tick Daten zu speichern.
     */
    private INewTickDataHandler newTickDataHandler;

    /**
     * Instanz eines Tick-Objekts, welches zur Berechnung der tick Daten verwendet wird.
     */
    protected Tick currentTick;

    /**
     * Maximale Anzahl an Ticks, welche simuliert werden sollen.
     */
    private final int maximumTickNumber;

    /**
     * Erstellt eine neue {@link CalculationThread} Instanz, mit welchem asynchron (von der GUI bzw. anderen Klassen)
     * sämtliche Berechnungsschritte simuliert werden. Berechnete Zwischenstände werden mittels
     * {@link INewTickDataHandler} an eine weitere Klasse übergeben.
     * 
     * @param playersPerTeam
     *            Die Anzahl der Spieler, die jedes Team hat.
     * @param gameInformation
     *            Die Parameter, die die Rahmenbedingungen für das Spiel bilden(Festlegung der Spielfeldgröße,
     *            Durchmesser der Spieler, des Balles etc.).
     * @param TeamAAi
     *            Die KI des erstgewählten Teams.
     * @param TeamBAi
     *            Die KI des zweitgewählten Teams.
     * @param newTickDataHandler
     *            Klasseninstanz, welche neue Simulationszwischenstände entgegennimmt.
     * @param simulationOptions
     *            Die Liste an aktivierten Simulations-Optionen, die im Spiel vewendet werden.
     * @see INewTickDataHandler
     */
    public CalculationThread(int playersPerTeam, GameInformation gameInformation, AI TeamAAi, AI TeamBAi,
            INewTickDataHandler newTickDataHandler, List<SimulationOptions> simulationOptions) {
        super();

        this.newTickDataHandler = newTickDataHandler;
        this.currentTick = new Tick(playersPerTeam, gameInformation, TeamAAi, TeamBAi, simulationOptions);

        maximumTickNumber = gameInformation.getMaximumTickNumber();
    }

    /**
     * Interne Berechnung der tick Daten, die solange weiterlaufen bis die Berechnung abgeschlossen ist (der maximale
     * tick-Wert erreicht ist), das Programm beendet wird oder die Berechnung abgebrochen wird.<br>
     * Berechnete Zwischenwerte werden mittels der Schnittstelle {@link INewTickDataHandler} einer weiteren Klasse
     * übergeben.
     * 
     * @see DataHandler
     */
    @Override
    public void run() {
        boolean calculationFinished = false;

        while ((!calculationFinished) && (!this.getIsAborted())) {
            this.currentTick.doNextTick();

            SoSiTickInformation tickInfo = this.currentTick.getCurrentTickInformationOfTeamA();

            this.newTickDataHandler.addNewTickData(tickInfo.getTeamGoals(), tickInfo.getOpponentTeamGoals(),
                    tickInfo.getBallPosition(), tickInfo.getPlayerPositions(), tickInfo.getOpponentPlayerPositions(),
                    currentTick.getTickEvent(), this.currentTick.getDebugMessageTeamA(),
                    this.currentTick.getDebugMessageTeamB());

            // Abbruchkriterium prüfen
            if (tickInfo.getCurrentTickNumber() >= this.maximumTickNumber - 1)
                calculationFinished = true;
        }
    }

    /**
     * Methode, um den Thread abzubrechen.
     */
    @Override
    public void abort() {
        super.abort();
        this.currentTick.shutdown();
    }
}
