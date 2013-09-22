package SoSi.Model.Calculation;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import sep.football.AI;
import sep.football.FreePlayActionHandler;
import sep.football.GameInformation;
import sep.football.KickActionHandler;
import sep.football.Position;
import sep.football.TickInformation;
import SoSi.Debugging.DebugManager;
import SoSi.Debugging.DebuggingAI;
import SoSi.Debugging.SoSiDebugManager;
import SoSi.Model.SimulationOptions;
import SoSi.Model.SoSiPosition;
import SoSi.Model.TickEvent;
import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GameObjects.GoalGameObject;
import SoSi.Model.GameObjects.PlayerGameObject;
import SoSi.Model.GamePhysics.GamePhysicsHandler;
import SoSi.Model.GameRules.GameRulesHandler;

/**
 * Beinhaltet alle verwendeten Spiel-Objekte und generiert chronologisch aufeinanderfolgende Simulations-Ticks. Der Tick
 * wendet die Entscheidungen der KIs an, gibt sie an die GamePhysics zur Berechnung weiter, gleicht sie mit dem
 * Regelwerk ab und holt die neuen Entscheidungen der KIs ein. Die Berechnungen der KI-Entscheidungen werden in zwei
 * Threads durchgeführt, für jede KI einer. Dabei haben die KIs nur eine gewisse Zeit zur Verfügung, um ihre
 * Entscheidungen zu treffen. Das Ergebnis der Berechnungen liegt dem CalculationThread vor, der diese neuen Tick-Daten
 * in die Liste aller bisher berechneten Tick-Daten im DataHandler speichert.
 */
public class Tick {
    /**
     * Referenz zu einer {@link SoSiGameInformation}-Instanz, das die aktuellen Spielparameter, die die
     * Rahmenbedingungen der Simulation bilden, enthält.
     */
    protected final GameInformation gameInformation;

    /**
     * Referenz auf die Daten des Teams A, für die entsprechende KI die Entscheidungen trifft.
     */
    protected final Team teamA;

    /**
     * Referenz auf die Daten des Teams B, für die entsprechende KI die Entscheidungen trifft.
     */
    protected final Team teamB;

    /**
     * Referenz auf das Spielballobjekt.
     */
    protected final BallGameObject ball;

    /**
     * Referenz auf das Torobjekt, das sich links innerhalb des Simulationsfeldes befindet.
     */
    private GoalGameObject leftGoal;

    /**
     * Referenz auf das Torobjekt, das sich rechts innerhalb des Simulationsfeldes befindet.
     */
    private GoalGameObject rightGoal;

    /**
     * Aktuelle Tick-Position der Berechnung. Wird mit -1 initialisiert, da erster Tick erst beim erstmaligen Aufruf von
     * {@link #doNextTick()} simuliert wird.
     */
    private int currentTick = -1; // -1 = noch kein tick durchgeführt

    /**
     * Referenz auf {@link GameRulesHandler}
     * 
     * @see GameRulesHandler
     */
    private final GameRulesHandler gameRulesHandler;

    /**
     * Referenz auf {@link GamePhysicsHandler}
     * 
     * @see GamePhysicsHandler
     */
    private final GamePhysicsHandler gamePhysicsHandler;

    /**
     * Referenz auf {@link DebugManager} für das Team A
     * 
     * @see SoSiDebugManager
     * @see DebugManager
     */
    private final SoSiDebugManager debugManagerTeamA;

    /**
     * Referenz auf {@link DebugManager} für das Team B
     * 
     * @see SoSiDebugManager
     * @see DebugManager
     */
    private final SoSiDebugManager debugManagerTeamB;

    /**
     * {@link TickEvent} des aktuellen Tick-Zustands.
     */
    private TickEvent currentTickEvent = TickEvent.KICK_OFF; // Zuweisung eigentl. überflüssig.

    /**
     * {@link TickEvent} des kommenden Ticks-Zustand. Also das TickEvent, welches beim nächsten Aufruf von
     * {@link #doNextTick()} vorliegt. <br>
     * Dies ist notwendig, da aktueller Tick-Zustand z.B. "FOUL" oder "GAME_INTERRUPTED" sein könnte und definiert
     * werden muss, was im nächsten Tick geschehen soll, also z.B. "GAME_INTERRUPTED" bzw. "FREE_KICK".
     */
    private TickEvent nextTickEvent = TickEvent.KICK_OFF;

    /**
     * Referenz auf das Team, welches momentan auf der linken Seite spielt.
     */
    private Team teamOnLeftSide;

    /**
     * Anzahl an Ticks, wie lang der aktuelle Simulationszustand "GAME_INTERRUPTED" sein soll.
     */
    private int interruptedTimeLeft = 0;

    /**
     * TickEvent, welches nach einer Spielunterbrechung eintreten soll.
     */
    private TickEvent eventAfterInterrupted = null;

    /**
     * Positionsangabe, an welcher nach einem Foul ein Freistoß durchgeführt werden soll.
     */
    private Position foulPosition = null;

    /**
     * Referenz auf Team, welches das Foul verursacht hat
     */
    private Team foulCausedByTeam;

    /**
     * Referenz auf Team, welches den Anstoß auszuführen hat.
     */
    private Team kickOffTeam;

    /**
     * Liste sämtlicher Spieler beider Teams. Dient dazu, um in einer Schleife über alle Spieler beider Teams itterieren
     * zu können.
     */
    private final List<PlayerGameObject> allPlayers;

    /**
     * Timeout-Zeit (in Millisekunden)
     */
    private static final int AI_DECISIONS_TIMEOUT_MILLISECONDS = 50;

    /**
     * Anzahl der Ticks, wie lang eine Spielunterbrechung dauern soll.<br>
     * Dieser Wert muss >= 10 sein (= maximale Blockingtime). <br>
     * Soll eine kürzere Unterbrechungsdauer unterstützt werden, so müssen nach einer Spielunterbrechung sämtliche
     * BlockingTimes auf 0 gesetzt werden. (Ansosnten könnte der Fall eintreten, dass der Spieler, welcher z.B. einen
     * Freistoß ausführen muss, noch geblockt ist und daher nicht schießen kann bzw. darf.
     */
    private static final int INTERRUPTED_TICK_COUNT = 50;

    /**
     * Fehlermeldung für DebugManager, wenn die Entscheidungsfindung einer KI zu lange dauerte.
     */
    private static final String DEBUG_MESSAGE_AI_TOOK_TO_LONG = "Fehler: Die Entscheidung dauerte zu lang und wurde "
            + "abgebrochen!";

    /**
     * ExecutorService für Abfrage von KI-Entscheidungen. (Für Nebenläufigkeit und max. Ausführungszeit).<br>
     * Aus Performancegründen wird ein Threadpool, welcher 2 Threads enthält, verwendet. Diese Threadinstanzen werden
     * stets wieder verwendet, es sei denn (mind.) eine KI hat die {@link #AI_DECISIONS_TIMEOUT_MILLISECONDS}
     * überschritten und der Thread musste beendet werden.
     */
    private ExecutorService aiExecutorService = null;

    /**
     * Worker-Referenz für KI-Entscheidungen des Teams A
     * 
     * @see #aiExecutorService
     */
    private AIDecisionsWorker workerTeamA;

    /**
     * Worker-Referenz für KI-Entscheidungen des Teams B
     * 
     * @see #aiExecutorService
     */
    private AIDecisionsWorker workerTeamB;

    /**
     * Flag, ob Berechnung abgeschlossen ist bzw. abgebrochen wurde.
     */
    private boolean isAborted = false;

    /**
     * CountDownLatch, um auf Fertigstellung beider KI-Entscheidungen zu warten.
     */
    private CountDownLatch aiDecisionsCountDownLatch;

    /**
     * CountDownLatch, um auf Fertigstellung beider KI-Entscheidungen zu warten.
     */
    private CountDownLatch threadsInitializedCountDownLatch;

    /**
     * Konstruktor für die Tick-Klasse, die für die Berechnung eines Ticks verantwortlich ist. Der Konstruktor erhält
     * sämtliche für die Durchführung der Simulation notwendigen Daten und speichert diese als Attribute ab.<br>
     * Des weiteren werden die Instanzen sämtlicher benötigten Klasse erstellt, sofern diese nicht als Parameter
     * übergeben wurden.<br>
     * Zur Berechnung von KI-Entscheidungen wird ein ThreadPool mit zwei Threads angelegt. Diese Resourcen werden nach
     * einer vollständigen Berechnung (entsprechende Anzahl an Aufrufen von {@link #doNextTick()}) automatisch
     * freigegeben. Sollte die Berechnung nicht vollständig durchgeführt werden, so ist ein manueller Aufruf von
     * {@link #shutdown()} notwendig.
     * 
     * @param playersPerTeam
     *            Die Anzahl der Spieler pro Team.
     * @param gameInformation
     *            Allgemeine Spielparameter, die die Rahmenbedingen für das Spiel bilden.
     * @param teamAAi
     *            Die KI, die als erstes geladen worden ist.
     * @param teamBAi
     *            Die KI, die als zweites geladen worden ist.
     * @param simulationOptions
     *            Sammlung an Spielparameter(Regeln, Spielerfarben etc.), die für diese Simulation gelten
     */
    public Tick(int playersPerTeam, GameInformation gameInformation, AI teamAAi, AI teamBAi,
            List<SimulationOptions> simulationOptions) {
        // Parameter überprüfen
        if (playersPerTeam <= 0)
            throw new IllegalArgumentException("playersPerTeam must be at least 1");
        if (gameInformation == null || teamAAi == null || teamBAi == null || simulationOptions == null)
            throw new IllegalArgumentException("Parameters must not be null");

        // Parameter speichern
        this.gameInformation = gameInformation;

        // Simulationsobjekte anlegen
        this.leftGoal = new GoalGameObject(gameInformation, true);
        this.rightGoal = new GoalGameObject(gameInformation, false);

        this.teamA = new Team(playersPerTeam, teamAAi, gameInformation.getPlayerDiameter(), new SoSiPosition(0, 0));
        this.teamB = new Team(playersPerTeam, teamBAi, gameInformation.getPlayerDiameter(), new SoSiPosition(
                this.gameInformation.getFieldLength(), 0));
        this.allPlayers = getAllPlayers();
        this.kickOffTeam = this.teamA;

        this.teamOnLeftSide = teamA;

        this.ball = new BallGameObject(new SoSiPosition(gameInformation.getFieldLength() / 2,
                gameInformation.getFieldWidth() / 2), gameInformation.getBallDiameter());

        // Berechnungs-Instanzen für Regeln und Physik anlegen
        this.gamePhysicsHandler = new GamePhysicsHandler(gameInformation, this.teamA, this.teamB, this.ball,
                this.leftGoal, this.rightGoal, simulationOptions);
        this.gameRulesHandler = new GameRulesHandler(gameInformation, this.teamA, this.teamB, this.ball,
                simulationOptions, this.leftGoal, this.rightGoal);

        // Debugging-Instanzen anlegen
        this.debugManagerTeamA = new SoSiDebugManager();
        this.debugManagerTeamB = new SoSiDebugManager();
    }

    /**
     * Methode zur Berechnung von einem Simulationsschritt. Der Zustand der Klasse bzw. dessen Attribute ändern sich
     * durch einen Aufruf, da die Simulation um einen Schritt weiter berechnet wird.<br>
     * Dabei werden die Entscheidungen beider KIs abgefragt und angewandt, sämtliche physikalische Berechnungen
     * ausgeführt, das Regelwerk überprüft und ggf. entsprechend darauf reagiert.<br>
     * <br>
     * Das Abfragen der Entscheidungen der KIs erfolgt dabei parallel, wobei für die Abfrage eine maximale Dauer
     * festgelegt wird. Wird diese von der KI überschritten, so werden ausschließlich die Entscheidungen angewandt,
     * welche die KI bis zum Zeitpunkt des Abbruchs bereits getroffen hat.<br>
     * <br>
     * Nach Abschluss der Methode kann über {@link #getCurrentTickInformationOfTeamA()} und {@link #getTickEvent()} von
     * außerhalb (im konkreten Fall im {@link CalculationThread}) der Stand der Simulation abgefragt werden.
     */
    synchronized public void doNextTick() {
        // Auf gültigen Zustand prüfen
        if (isAborted)
            throw new IllegalStateException(
                    "The simulation was already terminated (by either calling shutdown() or by reaching the end)!");

        // Current Tick erhöhen, entspricht Wert für den aktuell (zu berechnendem) Tick.
        this.currentTick++;

        // TickEvent für aktuellen Tick aktualisieren
        this.currentTickEvent = this.nextTickEvent;
        TickEvent currentTickEvent = this.currentTickEvent;

        clearDebugMessages();

        boolean tacklingFoulHappened = false;

        // blockingTime aller Spieler wird um eins heruntergezählt
        decrementAllBlockingTimes();

        if (currentTickEvent == null) {
            tacklingFoulHappened = this.askAiDecisions(null);
        } else {

            switch (currentTickEvent) {
            case FREE_KICK:
                this.handleFreeKick();
                this.askAiDecisions(TickEvent.FREE_KICK);
                break;

            case KICK_OFF:
                this.handleKickOff();
                this.askAiDecisions(TickEvent.KICK_OFF);
                break;

            case HALFTIME:
                this.handleHalftime();
                break;

            case GAME_INTERRUPTED:
                --this.interruptedTimeLeft;
                break;
            case GOAL_SCORED:
                this.handleGoalScored();
                break;
            case FOUL_TACKLING:
                PlayerGameObject playerWithBall = ball.getBallPossession() != null ? ball.getBallPossession() : ball
                        .getLastBallPosession();
                assert playerWithBall != null : "Tackling-Foul konnte nicht korrekt zugeordnet werden. Tick #"
                        + this.currentTick;

                // Foul muss von Team begangen begangen worden sein, welches nicht in Ballbesitz ist
                this.handleFoul(getTeamByPlayer(playerWithBall) == this.teamA ? this.teamB : this.teamA);
                break;

            case FOUL_OFFSIDE:
                this.handleFoul(getTeamByPlayer(ball.getLastBallContact()));
                assert ball.getLastBallContact() != null : "Niemand will den ball gehabt haben, das kann nicht sein. "
                        + "Tick #" + this.currentTick;
                break;
            case FOUL_OFF:
                this.handleFoul(getTeamByPlayer(ball.getLastBallContact()));
                assert ball.getLastBallContact() != null : "Niemand will den ball gehabt haben, das kann nicht sein. "
                        + "Tick #" + this.currentTick;
                break;
            }
        }

        this.gamePhysicsHandler.calculatePhysics(this.getCurrentTickInformationOfTeamA(), currentTickEvent);

        // TickEvent für zukünftigen Tick ermitteln:
        if (this.currentTick == this.gameInformation.getMaximumTickNumber() / 2) {
            // Überprüfung auf Halbzeitwechsel
            this.nextTickEvent = TickEvent.HALFTIME;
        } else if (this.interruptedTimeLeft > 0) {
            // Falls Spiel unterbrochen, auf keine weiteren Regeln prüfen
            this.nextTickEvent = TickEvent.GAME_INTERRUPTED;
        } else if (this.nextTickEvent == TickEvent.GAME_INTERRUPTED) {
            // Falls Spiel nicht (mehr) unterbrochen und letzter Tick war unterbrochen, so entspricht der folgende Tick
            // "eventAfterInterrupted"
            this.nextTickEvent = this.eventAfterInterrupted;
        } else {
            // Spiel nicht unterbrochen und nicht gerade fortgesetzt, auf Regeln prüfen
            this.nextTickEvent = this.gameRulesHandler.checkIfActiveRulesNotComplied(this
                    .getCurrentTickInformationOfTeamA());

            // Kein Abseits unmittelbar bei Freistoß oder Abstoß
            if (this.nextTickEvent == TickEvent.FOUL_OFFSIDE
                    && (this.currentTickEvent == TickEvent.KICK_OFF || this.currentTickEvent == TickEvent.FREE_KICK))
                this.nextTickEvent = null;

            // Foul nur beachten, falls kein anderes (und somit wichtigeres Event) vorliegt
            if (this.nextTickEvent == null && tacklingFoulHappened) {
                this.nextTickEvent = TickEvent.FOUL_TACKLING;
            }

            if (this.nextTickEvent != null) {
                // Da Regelwerk-Events den aktuellen Tick betreffen (zB Torzähler), werden diese sofort angezeigt:
                this.currentTickEvent = this.nextTickEvent;
                // Anm.: Im nächsten Tick muss allerding sichergestellt werden, dass diese Events kein 2. mal anzegezigt
                // werden. Dies erfolgt derzeit in "handleFoul()" und "handleGoalScored()"
            }
        }

        // Free resources, as soon as simulation is finished
        if (this.currentTick >= this.gameInformation.getMaximumTickNumber() - 1) {
            this.shutdown();
        }

    }

    // Für Debugzwecke: Alternative askAiDecisions() ohne asynchrone Thread-Aufrufe für KI-Entscheidungen

    // protected boolean askAiDecisions(TickEvent tickEvent) {
    // boolean foulHappened = false;
    //
    // TickInformation tickInformationOfTeamA = this.getTeamTickInformation(true);
    // TickInformation tickInformationOfTeamB = this.getTeamTickInformation(false);
    //
    // if (tickEvent == null) {
    // SoSiFreePlayActionHandler teamAAction = new SoSiFreePlayActionHandler(this.teamA.getPlayers(),
    // this.ball, this.debugManagerTeamA);
    // SoSiFreePlayActionHandler teamBAction = new SoSiFreePlayActionHandler(this.teamB.getPlayers(),
    // this.ball, this.debugManagerTeamB);
    //
    // this.teamA.getAI().freePlay(this.gameInformation, tickInformationOfTeamA, teamAAction);
    // this.teamB.getAI().freePlay(this.gameInformation, tickInformationOfTeamB, teamBAction);
    //
    // foulHappened = teamAAction.hasFoulHappend() || teamBAction.hasFoulHappend();
    // } else {
    // SoSiKickActionHandler teamAAction = new SoSiKickActionHandler(this.teamA.getPlayers(), this.ball,
    // this.debugManagerTeamA);
    // SoSiKickActionHandler teamBAction = new SoSiKickActionHandler(this.teamB.getPlayers(), this.ball,
    // this.debugManagerTeamB);
    //
    // if (tickEvent == TickEvent.KICK_OFF) {
    // this.teamA.getAI().kickOff(this.gameInformation, tickInformationOfTeamA, teamAAction);
    // this.teamB.getAI().kickOff(this.gameInformation, tickInformationOfTeamB, teamBAction);
    // } else if (tickEvent == TickEvent.FREE_KICK) {
    // this.teamA.getAI().freeKick(this.gameInformation, tickInformationOfTeamA, teamAAction);
    // this.teamB.getAI().freeKick(this.gameInformation, tickInformationOfTeamB, teamBAction);
    // } else {
    // throw new IllegalArgumentException("AiDecisions not valid for " + tickEvent.toString());
    // }
    // }
    //
    // return foulHappened;
    // }

    /**
     * Führt die Abfrage von KI-Entscheidungen durch.
     * 
     * @param tickEvent
     *            Das aktuelle TickEvent. Mithilfe dessen wird bestimmt, welche KI-Methode aufgerufen werden soll.
     * @return <code>true</code>, falls ein Foul eingetreten ist, <code>false</code> andernfalls.
     */
    protected synchronized boolean askAiDecisions(TickEvent tickEvent) {
        // Exit if calculation was aborted
        if (this.isAborted)
            return false;

        this.aiDecisionsCountDownLatch = new CountDownLatch(2);
        this.threadsInitializedCountDownLatch = new CountDownLatch(2);

        workerTeamA = new AIDecisionsWorker(this.teamA, tickEvent);
        workerTeamB = new AIDecisionsWorker(this.teamB, tickEvent);

        try {
            // Start new aiExecutorService, if no active one is available
            if (aiExecutorService == null) {
                // this.aiExecutorService = Executors.newFixedThreadPool(2);
                this.aiExecutorService = Executors.newFixedThreadPool(2);
//                new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
//                        Executors.defaultThreadFactory());
                
                new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                        new TickThreadFactory());

            }

            aiExecutorService.submit(workerTeamA);
            aiExecutorService.submit(workerTeamB);

            // Warten, bis KI-Entscheidungen abgeschlossen oder Zeitlimit überschritten wurde
            boolean timeOutOccurred = !aiDecisionsCountDownLatch.await(AI_DECISIONS_TIMEOUT_MILLISECONDS,
                    TimeUnit.MILLISECONDS);

            // Falls KI-Entscheidungen nicht abgeschlossen, ExecutorService bzw. ThreadPool stoppen und neuen anlegen.
            if (timeOutOccurred) {
                // Im DebugManager darüber informieren
                if (workerTeamA.getWorkerThread() != null && workerTeamA.getWorkerThread().isAlive()) {
                    this.debugManagerTeamA.print(DEBUG_MESSAGE_AI_TOOK_TO_LONG);
                }

                if (workerTeamB.getWorkerThread() != null && workerTeamB.getWorkerThread().isAlive()) {
                    this.debugManagerTeamB.print(DEBUG_MESSAGE_AI_TOOK_TO_LONG);
                }

                // Threads killen
                this.killAiThread();

                // In Konsole über Abbruch benachrichtigen
                System.err.println(String.format("Warning: At least one AI-Calculation took too long. (Tick #%d)",
                        this.currentTick));
            }
        } catch (InterruptedException e) {
            System.err.println("Waiting for AI Decisions was interrupted.");
        }

        return workerTeamA.completeAndCheckForFoul() || workerTeamB.completeAndCheckForFoul();
    }

    /**
     * Bricht die KI-Threads (bei einer Zeitüberschreitung) ab.
     */
    @SuppressWarnings("deprecation")
    synchronized private void killAiThread() {
        // AI-Threads didn't terminate, kill alive ones and create new Thread pool.
        if (this.aiExecutorService != null) {
            // Wait until both Threads have at least finished initialization (prevents getWorkerThread is null)
            if (this.threadsInitializedCountDownLatch != null) {
                try {
                    this.threadsInitializedCountDownLatch.await();
                } catch (InterruptedException e) {
                    // just ignore it
                }
            }

            // Kill both threads forcefully
            if (workerTeamA != null) {
                Thread threadToKill = workerTeamA.getWorkerThread();

                if (threadToKill != null && (threadToKill.isAlive()))
                    threadToKill.stop();
            }

            if (workerTeamB != null) {
                Thread threadToKill = workerTeamB.getWorkerThread();

                if (threadToKill != null && (threadToKill.isAlive()))
                    threadToKill.stop();
            }

            // Shutdown ExecutorService (release resources)
            this.aiExecutorService.shutdown();
            this.aiExecutorService = null;
        }
    }

    /**
     * Bricht die Berechnung der Simulation ab und gibt Ressourcen (Threads) frei.
     */
    synchronized public void shutdown() {
        if (!this.isAborted) {
            this.killAiThread();
            this.isAborted = true;
        }
    }

    /**
     * Handhabt die Halbzeit
     */
    private void handleHalftime() {
        // Zu Halbzeitwechsel alle Spieler zu Stillstand abbremsen
        this.stopAllPlayers();

        // Seiten tauschen
        this.teamOnLeftSide = (this.teamOnLeftSide == this.teamA) ? this.teamB : this.teamA;
        this.kickOffTeam = (this.kickOffTeam == this.teamA) ? this.teamB : this.teamA;

        // Spiel pausieren
        this.startInterruptedTime(TickEvent.KICK_OFF);
    }

    /**
     * Handhabt das Erzielen eines Tors
     */
    private void handleGoalScored() {
        // Fouls in Event-Liste nicht doppelt anzeigen lassen
        this.currentTickEvent = TickEvent.GAME_INTERRUPTED;

        // Nach Tor alle Spieler zu Stillstand abbremsen
        this.stopAllPlayers();

        // Ballbewegung stoppen (kann weiterlaufen, falls Player damit kollidiert. Dies ist erwünscht.)
        this.ball.setMovementDirection(new Vector2D(0, 0));

        if (this.ball.getPosition().getX() > this.gameInformation.getFieldLength() / 2) {
            // Ball ging in rechtes Tor
            this.kickOffTeam = (this.teamOnLeftSide == this.teamA) ? this.teamB : this.teamA;
        } else {
            // Ball ging in linkes Tor
            this.kickOffTeam = (this.teamOnLeftSide == this.teamA) ? this.teamA : this.teamB;
        }

        // Spiel pausieren
        this.startInterruptedTime(TickEvent.KICK_OFF);
    }

    /**
     * Handhabt einen Abstoß
     */
    private void handleKickOff() {
        // Ball erneut auf Mittelpunkt setzen
        this.ball.setPosition(new SoSiPosition(this.gameInformation.getFieldLength() / 2, this.gameInformation
                .getFieldWidth() / 2));
        this.ball.setMovementDirection(new Vector2D(0, 0));

        PlayerGameObject playerWithBall = kickOffTeam.getPlayers().get(0);
        this.ball.setBallPossession(playerWithBall);

        // Alle Spieler wegbewegen; Ein Spieler des Teams, welches das Gegentor bekommen hat, darf Abstoß ausführen und
        // wird auf die selbe Position wie die des Balles platziert
        this.movePlayersWithoutBallAwayFromBall();

        playerWithBall.setPosition(this.ball.getPosition());
    }

    /**
     * Gibt das zu einem Spieler zugehörige Team zurück.<br>
     * Die Methode muss mit einer {@link PlayerGameObject}-Instanz aufgerufen werden, welche in einem der beiden Teams
     * ist. Sollte ein "unbekannte" {@link PlayerGameObject}-Instanz oder <code>null</code> übergeben werden, so ist die
     * Rückgabe der Methode falsch, liefert jedoch keine Fehlermeldung.
     * 
     * @param player
     *            Spieler, zu welchem das zugehörige Team ermittelt werden soll.
     * @return Das zum Spieler zugehörige Team
     */
    private Team getTeamByPlayer(PlayerGameObject player) {
        // Argument darf (und sollte) niemals null sein. Assert zum Sichergehen während Implementierung/Verifikation.
        // Falls dies doch eintritt, was höchstens bei einem Foul in Kombination mit Regel-/Physikfehlern verursacht
        // werden können sollte, soll dies nicht zum Simulationsabbruch führen.
        // (Im Falle eines Falles einfach als Schiedrichterfehlentscheidung ansehen ;-) )
        assert player != null : "Player must not be null!";

        return (this.teamA.getPlayers().contains(player)) ? this.teamA : this.teamB;
    }

    /**
     * Handhabt ein Foul
     * 
     * @param foulCausedByTeam
     *            Team, welches das Foul verursacht hat.
     */
    private void handleFoul(Team foulCausedByTeam) {
        // Team, welches Foul verursacht hat, festhalten
        this.foulCausedByTeam = foulCausedByTeam;

        // Fouls in Event-Liste nicht doppelt anzeigen lassen
        this.currentTickEvent = TickEvent.GAME_INTERRUPTED;

        // Nach Foul alle Spieler zu Stillstand abbremsen
        this.stopAllPlayers();

        // Foulposition entspricht zunächst Ballposition bzw. specialFoulPosition und wird anschließend näher geprüft
        // und ggf. korrigiert
        this.foulPosition = this.ball.getPosition();

        // Prüfe auf Seitenaus -> Eckball oder Abstoß
        handleFoulCornerOrFreeKick(foulCausedByTeam);

        // Position ins Feld setzen (falls nicht bereits geschehen)
        double x = Math.min(this.gameInformation.getFieldLength(), Math.max(0, this.foulPosition.getX()));
        double y = Math.min(this.gameInformation.getFieldWidth(), Math.max(0, this.foulPosition.getY()));
        this.foulPosition = new SoSiPosition(x, y);

        // Spiel pausieren
        this.startInterruptedTime(TickEvent.FREE_KICK);
    }

    /**
     * Handhabt bei einem Foul spezielle Foul-Positionen. So wird entweder eine Ecke oder ein Abstoß gegeben, falls der
     * Ball ins Seitenaus ist.
     * 
     * @param foulCausedByTeam
     *            Team, welches das Foul verursacht hat.
     */
    private void handleFoulCornerOrFreeKick(Team foulCausedByTeam) {
        double cornerY = (foulPosition.getY() > this.gameInformation.getFieldWidth() / 2) ? this.gameInformation
                .getFieldWidth() : 0;
        if (foulPosition.getX() < 0) {
            // Linke Seite Aus
            if (foulCausedByTeam == this.teamOnLeftSide) {
                // Ecke
                this.foulPosition = new SoSiPosition(0, cornerY);
            } else {
                // Abstoß
                this.foulPosition = new SoSiPosition(gameInformation.getFieldLength() * 0.1,
                        gameInformation.getFieldWidth() / 2d);
            }
        } else if (foulPosition.getX() > this.gameInformation.getFieldLength()) {
            // Rechte Seite Aus
            if (foulCausedByTeam != this.teamOnLeftSide) {
                // Ecke
                this.foulPosition = new SoSiPosition(this.gameInformation.getFieldLength(), cornerY);
            } else {
                // Abstoß
                this.foulPosition = new SoSiPosition(gameInformation.getFieldLength() * 0.9,
                        gameInformation.getFieldWidth() / 2d);
            }
        }
    }

    /**
     * Handhabt einen Freistoß
     */
    private void handleFreeKick() {
        // Ball auf Foul-Position setzen
        this.ball.setPosition(this.foulPosition);

        Team freeKickTeam = (foulCausedByTeam == this.teamA) ? this.teamB : this.teamA;

        PlayerGameObject playerWithBall = freeKickTeam.getPlayers().get(0);
        this.ball.setBallPossession(playerWithBall);

        // Alle Spieler wegbewegen; Ein Spieler des Teams, welches den Freistoß durchführen darf, wird an die Stelle des
        // Balles platziert
        this.movePlayersWithoutBallAwayFromBall();

        playerWithBall.setPosition(this.ball.getPosition());
    }

    /**
     * Bewegt alle Spieler (außer den, der in Ballbesitz ist) vom Ball weg, falls diese die selbe Position wie der Ball
     * haben.<br>
     * Ansonsten köntne es für die KI nicht mehr eindeutig sein, welcher Spieler in Ballbesitz ist.
     */
    private void movePlayersWithoutBallAwayFromBall() {
        Position middlePoint = new SoSiPosition(this.gameInformation.getFieldLength() / 2,
                this.gameInformation.getFieldWidth() / 2);

        for (PlayerGameObject player : this.allPlayers) {
            Position playerPosition = player.getPosition();

            if (player != this.ball.getBallPossession() && playerPosition.getX() == this.ball.getPosition().getX()
                    && playerPosition.getY() == this.ball.getPosition().getY()) {

                // Spieler von Ball weg Richtung Mittelpunkt bewegen
                Vector2D moveAwayVector = new Vector2D(player.getPosition(), middlePoint);
                if (moveAwayVector.getLength() > 0) {
                    moveAwayVector = moveAwayVector.getNewLengthVector(this.gameInformation.getBallDiameter());
                } else {
                    // Falls direkt auf Mittelpunkt, Spieler "beliebig" verschieben
                    moveAwayVector = new Vector2D(this.gameInformation.getBallDiameter(), 0);
                }

                Vector2D newPosition = new Vector2D(playerPosition.getX(), playerPosition.getY());
                newPosition = Vector2D.addVectors(newPosition, moveAwayVector);

                player.setPosition(newPosition.convertToPosition());
            }
        }
    }

    /**
     * Bremst alle Spieler zum Stillstand ab
     */
    private void stopAllPlayers() {
        Vector2D stopMovementVector = new Vector2D(0, 0);

        for (PlayerGameObject player : this.allPlayers) {
            player.setTargetDirection(stopMovementVector);
        }
    }

    /**
     * Startet eine Spielunterbrechung. In dieser Zeit werden keine KI-Entscheidungen abgefragt und keine Überprüfung
     * der Spielregeln (Tor, Foul etc.) durchgeführt. <br>
     * Als Dauer der Spielunterbrechung (Anzahl an Ticks) wird der Wert {@value #INTERRUPTED_TICK_COUNT} von
     * {@link #INTERRUPTED_TICK_COUNT} verwendet.
     * 
     * @param eventAfterInterrupted
     *            Event, welches nach der Spielunterbrechung eintreten soll. (z.B Abstoß nach Spielunterbrechung durch
     *            Tor, Freistoß nach Spielunterbrechung durch ein Foul)
     */
    private void startInterruptedTime(TickEvent eventAfterInterrupted) {
        startInterruptedTime(eventAfterInterrupted, INTERRUPTED_TICK_COUNT);
    }

    /**
     * Startet eine Spielunterbrechung. In dieser Zeit werden keine KI-Entscheidungen abgefragt und keine Überprüfung
     * der Spielregeln (Tor, Foul etc.) durchgeführt.
     * 
     * @param eventAfterInterrupted
     *            Event, welches nach der Spielunterbrechung eintreten soll. (z.B Abstoß nach Spielunterbrechung durch
     *            Tor, Freistoß nach Spielunterbrechung durch ein Foul)
     * @param tickCount
     *            Anzahl an Ticks, wie lange die Spielunterbrechung dauern soll.
     */
    private void startInterruptedTime(TickEvent eventAfterInterrupted, int tickCount) {
        this.interruptedTimeLeft = tickCount;
        this.eventAfterInterrupted = eventAfterInterrupted;
    }

    /**
     * Zu jedem Tick muss zu Anfang die blockingTime runtergezählt werden. Dazu werden Spieler aller Teams
     * zusammengefasst und falls deren blockingTime ungleich 0 ist, wird sie um 1 verringert.
     */
    private void decrementAllBlockingTimes() {
        // Runterzählen der blockingTime
        for (PlayerGameObject player : this.allPlayers) {
            player.decrementBlockTimeRemaining();
        }
    }

    /**
     * Sämtliche {@link PlayerGameObject} der beiden Teams auf dem Feld werden zu einer Liste zusammengefasst.
     * 
     * @return Liste aller Spieler, die in diesem Spiel existieren.
     */
    private List<PlayerGameObject> getAllPlayers() {
        List<PlayerGameObject> allPlayers = new ArrayList<PlayerGameObject>();
        allPlayers.addAll(teamA.getPlayers());
        allPlayers.addAll(teamB.getPlayers());

        return allPlayers;
    }

    /**
     * Setzt die Debugnachrichten beider DebugManager der beiden Teams zurück.
     */
    private void clearDebugMessages() {
        this.debugManagerTeamA.clear();
        this.debugManagerTeamB.clear();
    }

    /**
     * Methode, um teamspezifische tick Informationen zu erstellen und zurückzugeben. Diese werden den AIs zur Verfügung
     * gestellt, damit diese ihre Entscheidungen berechnen können.
     * 
     * @param useTeamA
     *            Boolean-Wert, ob die Information für das erste oder das zweite Team erstellt werden soll.
     * @return Teamspezifische tick Informationen. Falls das Objekt nicht vorliegt oder fehlerhaft ist, wird <b>null</b>
     *         zurückgegeben.
     */
    protected SoSiTickInformation getTeamTickInformation(boolean useTeamA) {
        Team ownTeam = (useTeamA) ? this.teamA : this.teamB;
        Team oppTeam = (useTeamA) ? this.teamB : this.teamA;

        return new SoSiTickInformation(this.ball.getPosition(), ownTeam, oppTeam, ownTeam == teamOnLeftSide,
                currentTick, ball.getBallPossession());
    }

    /**
     * Getter-Methode für ein Objekt der SoSiTickInformation, die für die erstgewählte KI benutzt wird, um sie mit
     * Information zu versorgen, die sie zur Entscheidungsfindung benötigt.
     * 
     * @return Die tick Informationen des erstgewählten Teams. Falls das Objekt nicht existiert/nicht erstellt werden
     *         kann, wird <b>null</b> zurückgegeben.
     */
    public SoSiTickInformation getCurrentTickInformationOfTeamA() {
        return getTeamTickInformation(true);
    }

    /**
     * Getter für eventuell aufgetretene tick Events.
     * 
     * @return <b>Null</b>, falls kein Event eingetreten ist oder ein ENUM des Typs tickEvent.
     */
    public TickEvent getTickEvent() {
        return currentTickEvent;
    }

    /**
     * Getter-Methode für die debugging Nachricht des Teams A.
     * 
     * @return Die debug-Nachricht des Team A.
     */
    public String getDebugMessageTeamA() {
        return this.debugManagerTeamA.getDebugMessage();
    }

    /**
     * Getter-Methode für die debugging Nachricht des Teams B.
     * 
     * @return Die debug-Nachricht des Team B.
     */
    public String getDebugMessageTeamB() {
        return this.debugManagerTeamB.getDebugMessage();
    }

    /**
     * Worker-Klasse für die Abfrage von KI-Entscheidungen. Die Worker Klasse wird dabei einem Thread übergeben, damit
     * die Abfrage asynchron und zeitbegrenzt ausgeführt werden kann.
     */
    private class AIDecisionsWorker implements Runnable {
        private final Team team;
        private final TickEvent tickEvent;
        private final TickInformation tickInformation;
        private final SoSiActionHandler actionHandler;
        private final DebugManager currentDebugManager;
        private final CountDownLatch currentAiDecisionsCountDownLatch;
        private final CountDownLatch currentThreadsInitializedCountDownLatch;
        private volatile Thread ownThread = null;

        /**
         * Erstellt eine neue AIDecisionsWorker Instanz
         * 
         * @param team
         *            Referenz auf das Team, für welche die Abfrage durchgeführt werden soll.
         * @param tickEvent
         *            Das aktuelle TickEvent. Mithilfe dessen wird bestimmt, welche KI-Methode aufgerufen werden soll.
         */
        public AIDecisionsWorker(Team team, TickEvent tickEvent) {
            this.team = team;
            this.tickEvent = tickEvent;
            tickInformation = getTeamTickInformation(team == teamA);
            this.currentAiDecisionsCountDownLatch = aiDecisionsCountDownLatch;
            this.currentThreadsInitializedCountDownLatch = threadsInitializedCountDownLatch;

            this.currentDebugManager = (team == teamA) ? debugManagerTeamA : debugManagerTeamB;

            if (tickEvent == null) {
                this.actionHandler = new SoSiFreePlayActionHandler(team.getPlayers(), ball, this.currentDebugManager);
            } else {
                this.actionHandler = new SoSiKickActionHandler(team.getPlayers(), ball, this.currentDebugManager);
            }
        }

        @Override
        public void run() {
            this.ownThread = Thread.currentThread();

            // Set uncaughtException for currentThread, so that ThreadDeaths aren't printed to console by any other
            // UncaughtExceptionHandlers (ThreadPool or default)
            Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread arg0, Throwable arg1) {
                    if (arg1 instanceof ThreadDeath) {
                        // just ignore ThreadDeath
                    }
                }
            });

            // Decrement threadsInitializedCountDownLatch, so Tick waits for hard termination (Thread.stop) of
            // Worker/Thread, until at least the initialization instructions have finished
            this.currentThreadsInitializedCountDownLatch.countDown();

            if (this.actionHandler instanceof SoSiFreePlayActionHandler) {
                // FreePlay
                try {
                    this.team.getAI().freePlay(gameInformation, this.tickInformation,
                            (FreePlayActionHandler) this.actionHandler);
                } catch (Exception e) {
                    this.handleAiException(e);
                } catch (Error e) {
                    this.handleAiException(e);
                }

            } else if (this.actionHandler instanceof SoSiKickActionHandler) {
                if (tickEvent == TickEvent.KICK_OFF) {
                    try {
                        this.team.getAI().kickOff(gameInformation, this.tickInformation,
                                (KickActionHandler) this.actionHandler);
                    } catch (Exception e) {
                        this.handleAiException(e);
                    }
                } else if (tickEvent == TickEvent.FREE_KICK) {
                    try {
                        this.team.getAI().freeKick(gameInformation, this.tickInformation,
                                (KickActionHandler) this.actionHandler);
                    } catch (Exception e) {
                        this.handleAiException(e);
                    }
                } else {
                    throw new IllegalStateException("Unknown TickEvent-Typ!");
                }

            } else {
                throw new IllegalStateException("Unknown ActionHandler-Typ!");
            }

            // KI auf Debugmessages abfragen, falls diese {@link DebuggingAI} implementiert.
            if (this.team.getAI() instanceof DebuggingAI) {
                try {
                    ((DebuggingAI) this.team.getAI()).debug(this.currentDebugManager);
                } catch (Exception e) {
                    this.handleAiException(e);
                }
            }

            // Decrement countDownLatch, so Tick gets notified when both Workers have terminated
            this.currentAiDecisionsCountDownLatch.countDown();
        }

        /**
         * Gibt die Thread-Instanz des aktuellen Workers zurück
         * 
         * @return Thread-Instanz des aktuellen Workers
         */
        public Thread getWorkerThread() {
            return this.ownThread;
        }

        /**
         * ActionHandler beenden und auf eingetretenes Foul prüfen
         * 
         * @return <code>true</code>, falls ein Foul eingetreten ist, <code>false</code> andernfalls.
         */
        public boolean completeAndCheckForFoul() {
            this.actionHandler.completeAction();

            return (this.actionHandler instanceof SoSiFreePlayActionHandler)
                    && ((SoSiFreePlayActionHandler) this.actionHandler).hasFoulHappend();
        }

        /**
         * Handhabt ggf. aufgetretene Exceptions während einer KI-Ausführung.
         * 
         * @param e
         *            Aufgetretene Exception bzw. Throwable-Instanz
         */
        private void handleAiException(Throwable e) {
            // ignore ThreadDeaths
            if (!(e instanceof ThreadDeath)) {
                String tickEventName = "unknown";
                if (this.tickEvent == null)
                    tickEventName = "freePlay()";
                else if (this.tickEvent == TickEvent.FREE_KICK)
                    tickEventName = "freeKick()";
                else if (this.tickEvent == TickEvent.KICK_OFF)
                    tickEventName = "kickOff()";

                System.err.println(String.format("Exception in AI-Decision (Tick #%d, %s): %s",
                        this.tickInformation.getCurrentTickNumber(), tickEventName, e.toString()));

                this.currentDebugManager.print("Exception: " + e.getClass().getName());
                this.currentDebugManager.print("Message: " + e.getMessage());
                this.currentDebugManager.print("\nStacktrace:");

                for (StackTraceElement line : e.getStackTrace()) {
                    this.currentDebugManager.print("  " + line.toString());
                }
            }
        }
    }
    
    
    private static class TickThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        private static final ThreadGroup group = new ThreadGroup("sosi-ai-group");
 
        TickThreadFactory() {
            namePrefix = "sosi-ai-thread - pool-" + poolNumber.getAndIncrement() + "-thread-";
        }
        
        
 
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
    
}
