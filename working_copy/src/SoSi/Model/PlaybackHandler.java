package SoSi.Model;

import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import sep.football.GameInformation;

import SoSi.Model.Calculation.DataHandler;
import SoSi.Model.Calculation.DataHandler.SimulationSaveFileException;
import SoSi.Model.Calculation.SoSiGameInformation;
import SoSi.Model.Calculation.AILoader.AiLoadingException;

/**
 * Die Klasse ist primär für die einheitliche Wiedergabe aller möglichen Views zuständig. Sie versorgt die GUI, mit den
 * für sie zur Darstellung notwendigen Daten. Hierfür hat sie eine Referenz auf den {@link DataHandler}, der als Quelle
 * für die einzelnen {@link TickData}s verwendet wird. Der {@link PlaybackHandler} verwendet einen Timer, um sich vom
 * DataHandler periodisch neue Daten zu besorgen. Falls die Wiedergabe pausiert, vorgelaufen oder zurückgelaufen wird,
 * wird entweder das Timerintervall anpasst (verkürzt oder verlängert), der Timer gestoppt oder der Durchlauf durch die
 * Liste mit allen {@link TickData}s mit größeren oder kleineren Schritten durchgeführt.
 */
public class PlaybackHandler extends Observable {

    /**
     * Der Wert der Normalwiedergabegeschwindigkeit, der angibt, wieviele Ticks einer Sekunde entsprechen. Dient zur
     * Festlegung, wieviele Ticks innerhalb einer Sekunde bei einfacher Wiedergabegeschwindigkeit abgespielt werden
     * sollen.
     */
    public static final int TICKS_PER_SECOND = 20;

    /**
     * Standardwert für Anzahl der Playback-Aktualisierungen pro Sekunde während einer aktiven Wiedergabe.
     */
    protected static final int PLAYBACK_REFRESH_RATE = 40;

    /**
     * Die maximale Geschwindigkeit (das maximale Vielfache der Normalwiedergabegeschwindigkeit) mit der die Wiedergabe
     * abgespielt wird.
     */
    public static final double MAX_PLAYBACK_SPEED_RATE = 10;

    /**
     * Die Anzahl an Sekunden, die die Simulation bei einem Replay zurückspringt.
     */
    private static final double REPLAY_SECONDS_COUNT = 5;

    /**
     * Gibt die maximale, zu erwartende Anzahl an Ticks an. Entspricht dabei GameInformation.getMaximumTickNumber()
     */
    private int maximumTickNumber;

    /**
     * Flag, ob für die aktuellen DataHandler-Instanz bereits das Event "SIMULATION_FINISHED" gesendet wurde, nachdem
     * die Berechnung bzw. der Ladevorgang komplett abgeschlossen wurde.
     */
    private boolean simulationFinishedNotifySent = false;

    /**
     * Referenz zu einer Instanz eines {@link DataHandler}, in dem alle Tick-Daten einer Simulation gespeichert sind
     * (soweit sie schon berechnet sind). Wird als Quelle für die aktuellen Tick-Daten(currentTickData) verwendet,
     * welche wiederum von der GUI zur Darstellung der Wiedergabe verwendet werden.
     */
    private DataHandler dataHandler;

    /**
     * Timer, der dazu verwendet wird, um zyklisch neue Tick-Daten zu besorgen, die für die Wiedergabe notwendig sind.
     */
    private Timer updatePlaybackTimer;

    /**
     * Die aktuellen Tick-Daten, die zyklisch aktualisiert werden.
     */
    private TickData currentTickData;

    /**
     * Die Breite des Spielfeldes.
     */
    private final double fieldWidth;

    /**
     * Die Länge des Spielfeldes.
     */
    private final double fieldLength;

    /**
     * Die Länge eines Tores.
     */
    private final double goalSize;

    /**
     * Der Durchmesser eines Spielers.
     */
    private final double playerDiameter;

    /**
     * Der Durchmesser eines Balles.
     */
    private final double ballDiameter;

    /**
     * Der Multiplikator der Normalwiedergabegeschwindigkeit, der angibt, wie schnell die aktuelle
     * Wiedergabegeschwindigkeit abläuft. Ist das Vorzeichen positiv, so ist {@link #playbackSpeedRate} der Wert der
     * Vorlaufgeschwindigkeit, andernfalls der Wert der Rücklaufgeschwindigkeit. Ist der Wert 1, so ist die
     * Wiedergabegeschwindigkeit gleich der Normalwiedergabegeschwindigkeit.
     */
    private double playbackSpeedRate = 1.0;

    /**
     * Die aktuelle Position innerhalb der Liste aller berechneten TickDatas im DataHandler. Die Variable dient dazu
     * anzugeben, welche Tick-Daten gerade in currentTickData zu finden sind (genauer: an welcher Stelle sie sich in der
     * Liste befinden).
     */
    private double playbackPosition = 0;

    /**
     * Gibt an, ob die aktuelle Wiedergabe und Berechnung der Simulation abgebrochen wurde. Wird beim Aufruf von
     * .abort() gesetzt. Ein Aufruf von play/pause verändert den gesetzten Wert nicht.
     */
    private boolean isAborted = true;

    /**
     * Konstruktor der Klasse PlaybackHandler. Die Parameter, die unten aufgeführt sind, werden das ganze Programm über
     * verwendet. Um das Spielfeld richtig zu erstellen, von den AIs, damit sie ihre Entscheidungen abhängig davon
     * treffen können, von der Spielphysik-Klasse, um richtige Kollisions- und Abprallberechnungen durchzuführen, etc.
     * 
     * @param fieldWidth
     *            Die Weite des Spielfeldes.
     * @param fieldLength
     *            Die Länge des Spielfeldes.
     * @param goalSize
     *            Die Länge des Bereiches, der als Tor definiert wird. Diese liegen auf jeweils gegenüberliegenden
     *            Seiten des Spielfeldes.
     * @param playerDiameter
     *            Der Durchmesser eines Spielers.
     * @param ballDiameter
     *            Der Durchmesser des Balles.
     * @throws IllegalArgumentException
     *             Tritt ein, wenn eine der Argumente einen ungültigen Wert enthält und die neue Simulation nicht
     *             gestartet werden kann.
     */
    public PlaybackHandler(double fieldWidth, double fieldLength, double goalSize, double playerDiameter,
            double ballDiameter) {
        if (fieldWidth < 0 || fieldLength < 0 || goalSize < 0 || playerDiameter < 0 || ballDiameter < 0)
            throw new IllegalArgumentException("Negative values not allowed!");
        if (goalSize >= fieldWidth)
            throw new IllegalArgumentException("GoalSize must be smaller than fieldWidth!");
        if (goalSize < ballDiameter)
            throw new IllegalArgumentException("GoalSize must be taller than ballDiameter!");
        if (playerDiameter > fieldWidth || playerDiameter > fieldLength)
            throw new IllegalArgumentException("PlayerDiamater must be smaller than field!");
        if (ballDiameter > fieldWidth || ballDiameter > fieldLength)
            throw new IllegalArgumentException("BallDiameter must be smaller than field!");

        this.fieldWidth = fieldWidth;
        this.fieldLength = fieldLength;
        this.goalSize = goalSize;
        this.playerDiameter = playerDiameter;
        this.ballDiameter = ballDiameter;

        this.setChanged();
    }

    /**
     * Dient zur Initialisierung und dem Starten des Timers. Das Timerintervall wird so gewählt, dass eine bestimmte
     * Anzahl an ticks pro Sekunden vom DataHandler erfragt und in das Objekt currentTickData gespeichert werden.
     */
    private void startPlaybackTimer() {
        if (this.updatePlaybackTimer != null)
            this.updatePlaybackTimer.cancel();

        this.updatePlaybackTimer = new Timer();
        this.updatePlaybackTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                int oldPlaybackPosition = (int) playbackPosition;
                playbackPosition += ((double) TICKS_PER_SECOND / PLAYBACK_REFRESH_RATE) * playbackSpeedRate;

                // Wiedergabeposition aktualisieren
                restrictAndUpdatePlaybackPosition();

                // Updates nur durchführen, wenn sich die Wiedergabeposition tatsächlich geändert hat
                if (oldPlaybackPosition != (int) playbackPosition || playbackPosition == 0
                        || playbackPosition == maximumTickNumber - 1) {

                    // Events aus überprungenen TickDatas lesen und per notify weiterreichen (kann eintreten, falls
                    // Wiedergabe mit erhöhter Geschwindigkeit abläuft)
                    if (oldPlaybackPosition < playbackPosition) {
                        int start = oldPlaybackPosition == 0 ? 0 : oldPlaybackPosition + 1;
                        for (int i = start; i <= (int) playbackPosition; i++)
                            checkForNewTickEvent(i);
                    } else if (oldPlaybackPosition > playbackPosition) {
                        for (int i = oldPlaybackPosition - 1; i >= (int) playbackPosition; i--)
                            checkForNewTickEvent(i);
                    }

                    // Wiedergabe pausieren, falls Ende (bei Vorwärtsrichtung) oder Anfang (bei Rückrichtung) erreicht
                    if ((playbackSpeedRate > 0 && playbackPosition == maximumTickNumber - 1)
                            || (playbackSpeedRate < 0 && playbackPosition == 0)) {
                        pause();

                        // Falls Ende (und nicht Anfang) erreicht wurde, Event ausgeben:
                        if (playbackPosition == maximumTickNumber - 1) {
                            doNotify(SoccerUpdateEvent.PLAYBACK_END_REACHED);
                        }
                    }

                }
            }
        }, 0, (int) Math.round(1000d / PLAYBACK_REFRESH_RATE));

    }

    /**
     * Prüft, ob ein neues TickEvent vorliegt und benachrichtigt ggf. die Observer über das neue Event<br>
     * (Dient zum Beispiel zur Anzeige von eingetretenen Events)
     * 
     * @param tick
     *            Abzufragender Tickposition
     */
    private void checkForNewTickEvent(int tick) {
        if (dataHandler == null)
            return;

        TickEvent event = null;
        if (dataHandler.getSimulationTickCount() > 0)
            event = dataHandler.getTickPositionData(tick).getTickEvent();

        if (event != null) {
            switch (event) {
            case FOUL_OFF:
                doNotify(SoccerUpdateEvent.FOUL_OFF);
                break;
            case FOUL_OFFSIDE:
                doNotify(SoccerUpdateEvent.FOUL_OFFSIDE);
                break;
            case FOUL_TACKLING:
                doNotify(SoccerUpdateEvent.FOUL_TACKLING);
                break;
            case GOAL_SCORED:
                doNotify(SoccerUpdateEvent.GOAL_SCORED);
                break;
            case FREE_KICK:
                doNotify(SoccerUpdateEvent.FREE_KICK);
                break;
            case KICK_OFF:
                doNotify(SoccerUpdateEvent.KICK_OFF);
                break;
            case HALFTIME:
                doNotify(SoccerUpdateEvent.HALFTIME);
                break;
            case GAME_INTERRUPTED:
                // do nothing
                break;
            }
        }
    }

    /**
     * Schränkt die Tickposition auf den gültigen Wertebereich ein und aktualisiert entsprechend
     * {@link #currentTickData}
     */
    private void restrictAndUpdatePlaybackPosition() {
        if (playbackPosition >= dataHandler.getSimulationTickCount())
            playbackPosition = dataHandler.getSimulationTickCount() - 1;

        if (playbackPosition < 0)
            playbackPosition = 0;

        // TickData-Referenz aktualisiert (dies muss als letztes in der Methode erfolgen)
        if (dataHandler.getSimulationTickCount() > 0)
            currentTickData = dataHandler.getTickPositionData((int) playbackPosition); // do not round!
    }

    /**
     * Startet eine neue Simulation. Dabei werden die für die neue Simulation notwendigen Parameter und die aktivierten
     * Simulationsoptionen übergeben. Eine ggf. derzeit aktive oder bereits vollständig berechnete Simulation wird
     * verworfen.
     * 
     * @param playersPerTeam
     *            Anzahl der Spieler pro Team.
     * @param simulationDuration
     *            Die Dauer der Simulation in Ticks.
     * @param teamAAiPath
     *            Der Dateipfad der KI von Team A, die geladen werden soll.
     * @param teamBAiPath
     *            Der Dateipfad der KI von Team B, die geladen werden soll.
     * @param simulationOptions
     *            Die Liste an aktivierten Simulations-Optionen, die im Spiel vewendet werden.
     * @throws IllegalArgumentException
     *             Tritt ein, wenn eine der Argumente einen ungültigen Wert enthält und die neue Simulation nicht
     *             gestartet werden kann.
     * @throws AiLoadingException
     *             Wird geworfen, wenn das Laden der KI über den angegebenen Dateipfad fehlschlägt
     * @see AiLoadingException
     */
    public void startNewSimulation(int playersPerTeam, int simulationDuration, String teamAAiPath, String teamBAiPath,
            List<SimulationOptions> simulationOptions) throws IllegalArgumentException, AiLoadingException {
        this.playbackPosition = 0;
        this.currentTickData = null;
        this.simulationFinishedNotifySent = false;
        if (this.dataHandler != null) {
            this.abort();
        }

        this.isAborted = false;

        // Überprüfung, ob sämtliche Argumente korrekt
        if (playersPerTeam < 1) {
            throw new IllegalArgumentException("Die Anzahl der Spieler muss großer oder gleich 1 sein.");
        }
        if (simulationDuration < 1) {
            throw new IllegalArgumentException("Die Simulationsdauer muss großer oder gleich 1 sein.");
        }
        if (simulationOptions == null) {
            throw new IllegalArgumentException("Der Parameter simulationOptions darf nicht null sein.");
        }
        if (teamAAiPath == null || teamAAiPath.isEmpty() || teamBAiPath == null || teamBAiPath.isEmpty()) {
            throw new IllegalArgumentException("Die Pfadangaben zur KI dürfen nicht null oder leer sein.");
        }

        this.maximumTickNumber = simulationDuration;

        SoSiGameInformation gameInformation = new SoSiGameInformation(fieldWidth, fieldLength, goalSize,
                playerDiameter, ballDiameter, simulationDuration);

        this.dataHandler = new DataHandler(playersPerTeam, gameInformation, teamAAiPath, teamBAiPath, simulationOptions);

        this.play();
    }

    /**
     * Methode, die vom Timer der SoccerGUI aufgerufen wird, um sich Tick-Daten(currentTickData) vom PlaybackHandler zu
     * besorgen, mit denen sich dann die SoccerGUI neu zeichnen kann.
     * 
     * @return Die momentan im PlaybackHandler gespeicherten Tick-Daten im Objekt {@link currentTickData}. Falls keine
     *         Daten im Objekt {@link #currentTickData} zu finden sind, wird <b>null</b> zurückgegeben.
     */
    public TickData getCurrentTickData() {
        if (this.isAborted)
            return null;
        else
            return currentTickData;
    }

    /**
     * Entspricht primär der Funktionsweise von {@link #getCurrentTickData()}. Jedoch werden die Werte zwischen zwei
     * Ticks "interpoliert". Dies bedeutet, dass falls sich z.B. die Wiedergabeposition derzeit an Position Tick 200,75
     * befindet, der Übergang von Tick 200 auf Tick 201 interpoliert wird. Dabei werden die Werte entsprechend
     * berechnet, so dass der Wiedergabefortschritt zu 75 % bei Tick 201 angekommen ist.
     * 
     * Ist die Wiedergabe pausiert, so entspricht der Rückgabewert dem selben der Funktion {@link #getCurrentTickData()}
     * 
     * @return Die interpolierten, momentan im PlaybackHandler gespeicherten Tick-Daten im Objekt
     *         {@link currentTickData} zum nachfolgenden Tick. Falls keine Daten im Objekt {@link #currentTickData} zu
     *         finden sind, wird <b>null</b> zurückgegeben.
     */
    public TickData getInterpolatedCurrentTickData() {
        if (this.isAborted)
            return null;
        else {
            if (!isRunning()) {
                return getCurrentTickData();
            } else {
                int nextTickNumber = (playbackSpeedRate > 0) ? (int) (playbackPosition + 1)
                        : (int) (playbackPosition - 1);

                double percentage = this.playbackPosition;
                percentage -= Math.floor(percentage);
                if (playbackSpeedRate < 0)
                    percentage = 1 - percentage;

                nextTickNumber = Math.max(0, nextTickNumber);
                nextTickNumber = Math.min(dataHandler.getSimulationTickCount() - 1, nextTickNumber);

                TickData nextTickData = dataHandler.getTickPositionData(nextTickNumber);

                return InterpolationHelper.getInterpolatedTickData(this.getCurrentTickData(), nextTickData, percentage);
            }
        }
    }

    /**
     * Der Name der ersten AI, die ausgewählt wurde. Diese Methode wird von der GUI verwendet, um die Namen, der
     * gegeneinander antretenden KIs in der {@link SoccerGameInformationPanel} darzustellen.
     * 
     * @return Der <b>Name</b> der erstgewählten AI. Falls die Methode ohne ausgewählte KI aufgerufen wird, wird der
     *         Wert <b>null</b> zurückgegeben.
     */
    public String getTeamAName() {
        if (this.isAborted)
            throw new IllegalStateException("Aufruf ungültig, es wurde noch keine Simulation gestartet!");

        return this.dataHandler.getTeamAName();
    }

    /**
     * Der Name der zweiten AI, die ausgewählt wurde. Diese Methode wird von der GUI verwendet, um die Namen, der
     * gegeneinander antretenden KIs in der {@link SoccerGameInformationPanel} darzustellen.
     * 
     * @return Der <b>Name</b> der zweitgewählten AI. Falls die Methode ohne ausgewählte KI aufgerufen wird, wird der
     *         Wert <b>null</b> zurückgegeben.
     */
    public String getTeamBName() {
        if (this.isAborted)
            throw new IllegalStateException("Aufruf ungültig, es wurde noch keine Simulation gestartet!");

        return this.dataHandler.getTeamBName();
    }

    /**
     * Wird verwendet, um den aktuellen Stand der Simulationsberechnungen zu erfragen, die im Hintergrund läuft. Der
     * Rückgabe-Wert dieser Methode wird von {@link SoccerTimeLine} verwendet, um anzuzeigen, wie weit die Berechnung
     * schon fortgeschritten ist. Dies geschieht durch die Einfärbung des Hintergrunds der {@link SoccerTimeLine}.
     * 
     * @return Der letzte Tick, der berechnet worden ist. Falls keine Berechnung und somit keine {@link DataHandler}
     *         -Instanz vorliegt, wird der Wert -1 zurückgeliefert.
     */
    public int getSimulationTickPosition() {
        // Stand der Berechnung!
        if (this.dataHandler != null) {
            if ((!simulationFinishedNotifySent)
                    && (this.dataHandler.getSimulationTickCount() == this.maximumTickNumber)) {
                this.doNotify(SoccerUpdateEvent.SIMULATION_FINISHED);
            }

            return this.dataHandler.getSimulationTickCount() - 1;
        } else {
            return -1;
        }
    }

    /**
     * Wird verwendet, um eine alte Simulation zu laden. Hierzu muss ein Dateipfad angegeben werden, der Daten enthält,
     * die in den {@link DataHandler} geladen werden.
     * 
     * @param path
     *            Der Dateipfad, von dem eine bereits gespeicherte Simulationsberechnung geladen werden soll.
     * @throws SimulationSaveFileException
     */
    public void loadFromFile(String path) throws SimulationSaveFileException {
        this.playbackPosition = 0;
        this.currentTickData = null;
        this.simulationFinishedNotifySent = false;
        if (this.dataHandler != null) {
            this.abort();
        }

        this.isAborted = false;

        DataHandler newDataHandler = new DataHandler(path);
        this.dataHandler = newDataHandler;
        this.maximumTickNumber = this.dataHandler.getGameInformation().getMaximumTickNumber();
        this.play();
    }

    /**
     * Wird verwendet, um eine bereits berechnete Simulation zu speichern. Hierzu wird ein Dateipfad angegeben, unter
     * dem die berechneten Daten gespeichert werden sollen. Die Datei muss in gültiger Form vorliegen, so dass die
     * Klasse {@link DataHandler} diese laden kann, um sie zur Darstellung zur Verfügung zu stellen. Beim Versuch, eine
     * noch nicht abgeschlossene Berechnung zu speichern, bricht der Vorgang ab und es wird eine Exceptiion vom Typ
     * SimulationSaveFileException geworfen. <br>
     * Falls die Berechnung der Simulation noch nicht abgeschlossen ist, wird ebenfalls eine Exception vom Typ
     * SimulationSaveFileException geworfen.
     * 
     * @param path
     *            Der Dateipfad, unter dem die erfolgte Simulationsberechnung gespeichert werden soll.
     * @throws SimulationSaveFileException
     */
    public void saveToFile(String path) throws SimulationSaveFileException {
        if (this.isAborted)
            throw new IllegalStateException("Aufruf ungültig, es wurde noch keine Simulation gestartet!");
        else if (!dataHandler.isSimulationCalculationFinished())
            throw new IllegalStateException("Speichern nicht möglich, da Berechnung noch nicht abgeschlossen.");

        this.dataHandler.saveToFile(path);
    }

    // Die folgenden Methoden werden dazu verwendet, um das Zeitintervall, in dem der Timer sich die Daten besorgen
    // soll, zu steuern.

    /**
     * Der im PlaybackHandler enthaltene Timer wird (wieder) gestartet. Darüber werden die Observer der
     * PlaybackHandlerinstanz in Kenntnis gesetzt.
     */
    public void play() {
        if (this.isAborted)
            throw new IllegalStateException("Aufruf ungültig, es wurde noch keine Simulation gestartet!");

        // Falls Vorwärtsrichtung und Wiedergabe am Ende, Wiedergabeposition an Anfang setzen
        if (this.playbackPosition == this.maximumTickNumber - 1 && this.playbackSpeedRate > 0)
            this.playbackPosition = 0;

        // Falls Wiedergabeposition am Anfang Rückwärtsrichtung, Richtung zu Normal-Vorlaufgeschwindigkeit ändern
        if (this.playbackPosition == 0 && this.playbackSpeedRate < 0)
            this.setPlaybackSpeedRate(1);

        if (this.playbackPosition == this.dataHandler.getSimulationTickCount() && this.playbackSpeedRate > 0)
            this.playbackPosition = 0;

        this.startPlaybackTimer();
        this.doNotify(SoccerUpdateEvent.PLAYBACK_STARTED);
    }

    /**
     * Der im PlaybackHandler enthaltene Timer wird gestoppt. Darüber werden die Observer der PlaybackHandlerinstanz in
     * Kenntnis gesetzt.
     */
    public void pause() {
        if (this.isAborted)
            throw new IllegalStateException("Aufruf ungültig, es wurde noch keine Simulation gestartet!");

        // Falls Wiedergabe aktiv: Zu nächstem Tick aufrunden falls Wiedergabe vorwärts, ansonsten zu vorherigem Tick
        // springen (-1)
        if (this.isRunning()) {
            if (this.playbackSpeedRate > 0)
                this.playbackPosition = Math.ceil(this.playbackPosition);
            else
                this.playbackPosition = (int) (this.playbackPosition) - 1;
            restrictAndUpdatePlaybackPosition();
        }

        if (this.updatePlaybackTimer != null) {
            this.updatePlaybackTimer.cancel();
            this.updatePlaybackTimer = null;
        }

        this.doNotify(SoccerUpdateEvent.PLAYBACK_PAUSED);
    }

    /**
     * Setzt den Faktor, der multipliziert mit der Normalwiedergabegeschwindigkeit die aktuelle
     * Wiedergabegeschwindigkeit bestimmt. Das Vorzeichen gibt an, ob die Wiedergabe vorläuft (positives Vorzeichen)
     * oder zurückläuft (negatives Vorzeichen).
     * 
     * @param playbackSpeedRate
     *            Der Faktor, der zusammen mit der Normalwiedergabegeschwindigkeit die Wiedergabegeschwindigkeit angibt.
     *            Dieser kann maximal im Betrag nur "10" sein.
     */
    public void setPlaybackSpeedRate(double playbackSpeedRate) {
        if (playbackSpeedRate != this.playbackSpeedRate) {
            this.playbackSpeedRate = playbackSpeedRate;
            this.doNotify(SoccerUpdateEvent.PLAYBACK_SPEED_CHANGED);
        }
    }

    /**
     * Gibt den Faktor zurück, der multipliziert mit der Normalwiedergabegeschwindigkeit die aktuelle
     * Wiedergabegeschwindigkeit bestimmt. Das Vorzeichen gibt an, ob die Wiedergabe vorläuft (positives Vorzeichen)
     * oder zurückläuft (negatives Vorzeichen).
     * 
     * @return Der Faktor, der zusammen mit der Normalwiedergabegeschwindigkeit die Wiedergabegeschwindigkeit angibt.
     */
    public double getPlaybackSpeedRate() {
        return this.playbackSpeedRate;
    }

    /**
     * Bricht die aktuelle Berechnung ab und stoppt die Wiedergabe. Die Observer werden über den Abbruch informiert.
     */
    public void abort() {
        // Timer stoppen
        if (this.updatePlaybackTimer != null)
            this.updatePlaybackTimer.cancel();

        // Berechnung abbrechen
        if (this.dataHandler != null)
            this.dataHandler.abort();

        this.isAborted = true;

        this.doNotify(SoccerUpdateEvent.PLAYBACK_ABORTED);
    }

    /**
     * Bei der Ausführung der rewind-Methode wird die aktuelle Wiedergabegeschwindigkeit durch das Setzen der
     * {@link #playbackSpeedRate} entsprechend angepasst. Dabei wird zwischen den folgenden Wiedergabezuständen (vor der
     * Ausführung der Funktion) unterschieden:
     * <ul>
     * <li>Die Wiedergabe läuft beschleunigt vorwärts (d.h. {@link #playbackSpeedRate} > 1):<br>
     * Die {@link #playbackSpeedRate} wird auf den nächst-kleineren, ganzzahligen Wert >= 1 gesetzt.</li>
     * <li>Die Wiedergabe läuft verlangsamt bzw. mit Normalwiedergabegeschwindigkeit vorwärts (d.h. 0 <
     * {@link #playbackSpeedRate} <= 1):<br>
     * Die {@link #playbackSpeedRate} wird auf den Wert -1 gesetzt. Dadurch läuft die Wiedergabe mit der
     * Normalwiedergabegeschwindigkeit rückwärts.</li>
     * 
     * <li>Die Wiedergabe läuft beschleunigt bzw. mit Normalwiedergabegeschwindigkeit rückwärts (d.h.
     * {@link #playbackSpeedRate} <= -1):<br>
     * Die {@link #playbackSpeedRate} wird auf den nächst-kleineren, ganzzahligen Wert gesetzt.</li>
     * <li>Die Wiedergabe läuft verlangsamt rückwärts (d.h. -1 < {@link #playbackSpeedRate} < 0):<br>
     * Die {@link #playbackSpeedRate} wird auf den Wert -1 gesetzt. Dadurch läuft die Wiedergabe mit der
     * Normalwiedergabegeschwindigkeit rückwärts.</li>
     * </ul>
     */
    public void rewind() {
        if (this.isAborted)
            throw new IllegalStateException("Aufruf ungültig, es wurde noch keine Simulation gestartet!");

        double newSpeed = this.playbackSpeedRate;

        if (newSpeed > 1.0)
            newSpeed = Math.ceil(newSpeed) - 1;
        else if (newSpeed >= 0)
            newSpeed = -1;
        else if (playbackSpeedRate > -1)
            newSpeed = -1;
        else
            newSpeed = Math.ceil(newSpeed) - 1;

        // Auf Minimum beschränken
        if (newSpeed < -MAX_PLAYBACK_SPEED_RATE)
            newSpeed = -MAX_PLAYBACK_SPEED_RATE;

        if (!this.isRunning() && newSpeed > 0) {
            newSpeed = -1;
        }

        this.setPlaybackSpeedRate(newSpeed);

        if (!this.isRunning())
            this.play();
    }

    /**
     * Bei der Ausführung der forward-Methode wird die aktuelle Wiedergabegeschwindigkeit durch das Setzen der
     * {@link #playbackSpeedRate} entsprechend angepasst. Dabei wird zwischen den folgenden Wiedergabezuständen (vor der
     * Ausführung der Funktion) unterschieden:
     * <ul>
     * <li>Die Wiedergabe läuft beschleunigt bzw. mit Normalwiedergabegeschwindigkeit vorwärts (d.h.
     * {@link #playbackSpeedRate} >= 1):<br>
     * Die {@link #playbackSpeedRate} wird auf den nächst-größeren, ganzzahligen Wert gesetzt.</li>
     * <li>Die Wiedergabe läuft verlangsamt vorwärts (d.h. 0 < {@link #playbackSpeedRate} < 1):<br>
     * Die {@link #playbackSpeedRate} wird auf den Wert 1 gesetzt. Dadurch läuft die Wiedergabe mit der
     * Normalwiedergabegeschwindigkeit vorwärts.</li>
     * 
     * <li>Die Wiedergabe läuft beschleunigt rückwärts (d.h. {@link #playbackSpeedRate} < -1):<br>
     * Die {@link #playbackSpeedRate} wird auf den nächst-größeren, ganzzahligen Wert <= -1 gesetzt.</li>
     * <li>Die Wiedergabe läuft verlangsamt bzw. mit Normalwiedergabegeschwindigkeit rückwärts (d.h. -1 <=
     * {@link #playbackSpeedRate} < 0):<br>
     * Die {@link #playbackSpeedRate} wird auf den Wert 1 gesetzt. Dadurch läuft die Wiedergabe mit der
     * Normalwiedergabegeschwindigkeit vorwärts.</li>
     * </ul>
     */
    public void forward() {
        if (dataHandler == null)
            throw new IllegalStateException("Aufruf ungültig, es wurde noch keine Simulation gestartet!");

        double newSpeed = this.playbackSpeedRate;

        if (newSpeed >= 1.0)
            newSpeed = Math.floor(newSpeed) + 1;
        else if (newSpeed >= 0)
            newSpeed = 1;
        else if (playbackSpeedRate >= -1)
            newSpeed = 1;
        else
            newSpeed = Math.floor(newSpeed) + 1;

        // Auf Maximum beschränken
        if (newSpeed > MAX_PLAYBACK_SPEED_RATE)
            newSpeed = MAX_PLAYBACK_SPEED_RATE;

        if (!this.isRunning() && newSpeed < 0) {
            newSpeed = 1;
        }

        this.setPlaybackSpeedRate(newSpeed);

        if (!this.isRunning()) {
            this.play();
        }
    }

    /**
     * Der Durchlauf durch die Liste der Tick-Daten wird um eine feste Anzahl zurückgestellt und dort wieder
     * fortgeführt. Das Timerintervall wird dabei nicht verändert. Diese Operation ist nur möglich, wenn die Wiedergabe
     * fortwärts läuft. <br>
     * Dabei springt die Wiedergabe für den Benutzer um {@value #REPLAY_SECONDS_COUNT} Sekunden zurück. Das bedeutet,
     * wenn der User die Simulation mit 10-facher Wiedergabegeschwindigkeit betrachtet und dann den Replaybutton
     * betätigt, wird die Zeit der Simulation intern um 10 * {@value #REPLAY_SECONDS_COUNT} Sekunden zurückgesetzt wird.
     * Um die Simulationszeit, an der der Replaybutton betätigt worden ist wieder zur erreichen, vergehen für den User
     * {@value #REPLAY_SECONDS_COUNT} Sekunden (in der Simulation sind jedoch 10 * {@value #REPLAY_SECONDS_COUNT}
     * Sekunden vergangen).
     */
    public void replay() {
        if (dataHandler == null)
            throw new IllegalStateException("Aufruf ungültig, es wurde noch keine Simulation gestartet!");

        if (playbackSpeedRate > 0) {
            this.playbackPosition -= REPLAY_SECONDS_COUNT * TICKS_PER_SECOND * this.playbackSpeedRate;
            restrictAndUpdatePlaybackPosition();

            if (!this.isRunning())
                this.play();
        }
    }

    /**
     * Falls der Timer aktiv ist, wird er gestoppt, der Durchlauf der Liste damit auch und innerhalb der Liste wird um
     * eine tick Positionen zurückgesprungen. Wiederholtes Aufrufen des Befehls belässt den Timer gestoppt und
     * veranlasst den PlaybackHandler sich die Tick-Daten, die in der Liste vor den aktuellen Tick-Daten liegen, zu
     * besorgen. Das Besorgen der neuen Tick-Daten bedeutet, dass sich das Objekt currentTickData im DataHandler die
     * entsprechenden TickDatas aus der Liste aller berechneten TickDatas aus dem DataHandler holt.
     */
    public void simulationStepBack() {
        if (dataHandler == null)
            throw new IllegalStateException("Aufruf ungültig, es wurde noch keine Simulation gestartet!");

        this.pause();

        this.playbackPosition--;
        restrictAndUpdatePlaybackPosition();

        checkForNewTickEvent((int) this.playbackPosition);

        this.doNotify(SoccerUpdateEvent.PLAYBACK_STEP);
    }

    /**
     * Falls der Timer aktiv ist, wird er gestoppt, der Durchlauf der Liste damit auch und innerhalb der Liste wird um
     * eine tick Positionen vorgesprungen. Wiederholtes Aufrufen des Befehls belässt den Timer gestoppt und veranlasst
     * den PlaybackHandler sich die Tick-Daten, die in der Liste nach den aktuellen Tick-Daten liegen, zu besorgen. Das
     * Besorgen der neuen Tick-Daten bedeutet, dass sich das Objekt currentTickData im DataHandler die entsprechenden
     * TickDatas aus der Liste aller berechneten TickDatas aus dem DataHandler holt.
     */
    public void simulationStepForward() {
        if (dataHandler == null)
            throw new IllegalStateException("Aufruf ungültig, es wurde noch keine Simulation gestartet!");

        this.pause();

        this.playbackPosition++;
        restrictAndUpdatePlaybackPosition();

        checkForNewTickEvent((int) this.playbackPosition);

        this.doNotify(SoccerUpdateEvent.PLAYBACK_STEP);
    }

    /**
     * Das currentTickData Objekt wird mit den Werten aktualisiert, die sich in der TickData Liste im DataHandler an der
     * Position tickPosition befinden.
     * 
     * @param tickPosition
     *            Die Tick-Daten, die an dieser Stelle in der TickData-Liste im DataHandler zu finden sind, werden in
     *            currentTickData aktualisiert.
     */
    public void jumpToTime(int tickPosition) {
        if (dataHandler == null)
            throw new IllegalStateException("Aufruf ungültig, es wurde noch keine Simulation gestartet!");

        this.playbackPosition = tickPosition;
        restrictAndUpdatePlaybackPosition();

        this.doNotify(SoccerUpdateEvent.PLAYBACK_STEP);
    }

    /**
     * Methode, um zu überprüfen, ob der {@link updatePlaybackTimer} gerade aktiv ist.
     * 
     * @return <b> True</b>, falls der Timer aktiv ist und <b> False</b>, falls der Timer inaktiv oder nicht vorhanden
     *         ist.
     */
    public boolean isRunning() {
        return (this.updatePlaybackTimer != null);
    }

    /**
     * Gibt zurück, ob die Berechnung der Simulation abgeschlossen wurde.<br>
     * Dies wird konkret dazu verwendet, um im Stop-ActionListener zu unterscheiden, ob die Berechnung der Simulation
     * ggf. abgebrochen werden soll, oder ob nur die Wiedergabe pausiert ist und zum Anfang gesprungen werden soll.
     * 
     * @return <b>True</b>, falls die Berechnung der Simulation bereits abgeschlossen wurde, <b>False</b> andernfalls.
     */
    public boolean isSimulationCalculationFinished() {
        if (dataHandler == null)
            throw new IllegalStateException("Aufruf ungültig, es wurde noch keine Simulation gestartet!");

        return dataHandler.isSimulationCalculationFinished();
    }

    /**
     * Ruft setChanged auf und benachrichtigt die Observer über das neue Event
     * 
     * @param arg
     *            Das neue Event, über welches die Observer benachrichtigt werden sollen.
     */
    private void doNotify(Object arg) {
        this.setChanged();
        this.notifyObservers(arg);

        if (arg == SoccerUpdateEvent.SIMULATION_FINISHED) {
            this.simulationFinishedNotifySent = true;
        }
    }

    /**
     * Benachrichtigt alle Observer über den aktuellen Stand der Wiedergabe.
     */
    public void notifyCurrentState() {
        if (this.isAborted)
            this.doNotify(SoccerUpdateEvent.PLAYBACK_ABORTED);
        else if (this.isRunning())
            this.doNotify(SoccerUpdateEvent.PLAYBACK_STARTED);
        else
            this.doNotify(SoccerUpdateEvent.PLAYBACK_PAUSED);
    }

    /**
     * Gibt GameInformation-Instanz der aktuellen Simulation zurück.<br>
     * Rückgabe ist <code>null</code>, falls keine Simulationsberechnung gestartet und keine Simulation geladen wurde.
     * 
     * @return GameInformation-Instanz. <code>null</code>, falls keine Simulation vorliegt.
     */
    public GameInformation getGameInformation() {
        if (this.dataHandler != null)
            return this.dataHandler.getGameInformation();
        else
            return null;
    }
}
