package SoSi.Model.Calculation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sep.football.AI;
import sep.football.GameInformation;
import sep.football.Position;
import SoSi.Model.IProgressInformation;
import SoSi.Model.SimulationOptions;
import SoSi.Model.TickData;
import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.AILoader.AiLoadingException;

/**
 * Zentrale Klasse, die eine (neue) Simulation initialisiert und deren Zustand verwaltet, insbesondere das Speichern der
 * Informationen aller bereits berechneten Simulations-Ticks (TickData), welche dem PlaybackHandler zur Verfügung
 * gestellt werden. Zusätzlich bietet sie das Speichern und Laden von Simulationen an.
 */
public class DataHandler {

    private static final int ZIP_COMPRESSION_LEVEL = 9;
    private static final int ZIP_COMPRESSION_METHOD = ZipOutputStream.DEFLATED;
    private static final String ZIP_COMMENT = "SoSi - Simulation File";

    private static final int ZIP_ENTRY_TICKDATA_COUNT = 500;
    private static final String ZIP_ENTRY = "save";
    private static final String ZIP_ENTRY_FILETYPE = ".xml";
    private static final String ZIP_ENTRY_DELIMITER = "$";
    private static final String ZIP_ENTRY_GAMEINFO = ZIP_ENTRY + ZIP_ENTRY_DELIMITER + "gameinfo" + ZIP_ENTRY_FILETYPE;
    private static final String ZIP_ENTRY_TICKDATA_PART = ZIP_ENTRY + ZIP_ENTRY_DELIMITER;

    static final String IDENTIFIER_FILE = "sosi";
    static final String IDENTIFIER_SIMULATION_INFOS = "simulation_infos";
    static final String IDENTIFIER_TEAM_A_NAME = "team_a_ai_name";
    static final String IDENTIFIER_TEAM_B_NAME = "team_b_ai_name";

    static final String IDENTIFIER_TICK_COUNT = "tick_count";
    static final String IDENTIFIER_GAME_INFO = "game_information";
    static final String IDENTIFIER_BALL_DIAMETER = "ball_diameter";
    static final String IDENTIFIER_FIELD_LENGTH = "field_length";
    static final String IDENTIFIER_FIELD_WIDTH = "field_width";
    static final String IDENTIFIER_GOAL_SIZE = "goal_size";
    static final String IDENTIFIER_PLAYER_DIAMETER = "player_diameter";
    static final String IDENTIFIER_TICK = "tick";
    static final String IDENTIFIER_TICK_ID = "id";
    static final String IDENTIFIER_BALL_POSITION = "ball";
    static final String IDENTIFIER_GOALS_TEAM_A = "goals_team_a";
    static final String IDENTIFIER_GOALS_TEAM_B = "goals_team_b";
    static final String IDENTIFIER_DEBUG_TEAM_A = "debug_team_a";
    static final String IDENTIFIER_DEBUG_TEAM_B = "debug_team_b";
    static final String IDENTIFIER_POSITION_PREFIX_TEAM_A = "players_team_a";
    static final String IDENTIFIER_POSITION_PREFIX_TEAM_B = "players_team_b";
    static final String IDENTIFIER_POSITION_SUFIX = "_position";
    static final String IDENTIFIER_POSITION_X = "x";
    static final String IDENTIFIER_POSITION_Y = "y";
    static final String IDENTIFIER_EVENT = "event";

    static SaveProgressInformation saveProgressInformation;

    /**
     * Referenz zu einem Objekt, das die aktuellen Spielparameter, die die Rahmenbedingungen der Simulation bilden,
     * enthält.
     */
    private GameInformation gameInformation;

    /**
     * Liste aller bereits berechnenten Tick-Daten. Enthält alle zur Darstellung notwendigen Daten. Diese Liste wird
     * auch zur Speicherung von Simulationen verwendet.
     */
    private List<TickData> tickDataList;

    /**
     * Der Thread, der die Berechnungen durchführen lässt und die neu berechneten Tick-Daten in die Liste
     * {@link #tickDataList} schreibt.
     */
    protected AbortableThread workerThread;

    /**
     * Name des Teams A
     */
    private String teamAName;

    /**
     * Name des Teams B
     */
    private String teamBName;

    /**
     * Konstruktor zur Erstellung einer neuen Simulation(-sberechnung). Die Berechnung erfolgt mit Hilfe des
     * {@link CalculationThread}, der in dieser Klasser erstellt und gestartet wird.
     * 
     * @param playersPerTeam
     *            Die Anzahl der Spieler pro Team.
     * @param gameInformation
     *            Allgemeine Spielparameter, die die Rahmenbedingen für das Spiel bilden.
     * @param teamAAiPath
     *            Der Dateipfad der erstgewählten KI, die geladen werden soll.
     * @param teamBAiPath
     *            Der Dateipfad der zweitgewählten KI, die geladen werden soll.
     * @param simulationOptions
     *            Die Liste an aktivierten Simulations-Optionen, die im Spiel vewendet werden.
     * @throws IllegalArgumentException
     *             Tritt ein, wenn eine der Argumente einen ungültigen Wert enthält und die neue Simulation nicht
     *             gestartet werden kann.
     * @throws AiLoadingException
     *             Wird geworfen, wenn das Laden der KI über den angegebenen Dateipfad fehlschlägt
     * @see AiLoadingException
     */
    public DataHandler(int playersPerTeam, GameInformation gameInformation, String teamAAiPath, String teamBAiPath,
            List<SimulationOptions> simulationOptions) throws AiLoadingException {
        this(gameInformation, teamAAiPath, teamBAiPath);

        // Parameter prüfen
        if (gameInformation == null)
            throw new IllegalArgumentException("gameInformation must not be null!");
        if (simulationOptions == null)
            throw new IllegalArgumentException("simulationOptions must not be null!");

        // Simulation erstellen & Berechnung starten
        AI teamAAi = AILoader.LoadAI(teamAAiPath);
        AI teamBAi = AILoader.LoadAI(teamBAiPath);

        INewTickDataHandler newTickHandler = this.getNewTickDataHandler();

        this.workerThread = new CalculationThread(playersPerTeam, gameInformation, teamAAi, teamBAi, newTickHandler,
                simulationOptions);
        this.workerThread.start();
    }

    /**
     * Protected Konstuktor, für allgemeine Aufgaben, welche auch bei geerbten Klassen gültig ist und daher ausgeführt
     * werden soll.<br>
     * Dient im konkreten Fall zur Verwendung von eigenen AbortableThread-Instanzen, welche eigene Tick-Instanzen ohne
     * Verwendung von Zufallswerten benutzen. Dies wird z.B. für JUnit-Regressiontests benötigt. 
     * 
     * @param gameInformation
     *            Allgemeine Spielparameter, die die Rahmenbedingen für das Spiel bilden.
     * @param teamAAiPath
     *            Der Dateipfad der erstgewählten KI, die geladen werden soll.
     * @param teamBAiPath
     *            Der Dateipfad der zweitgewählten KI, die geladen werden soll.
     */
    protected DataHandler(GameInformation gameInformation, String teamAAiPath, String teamBAiPath) {
        // Parameter prüfen
        if (teamAAiPath == null || teamBAiPath == null)
            throw new IllegalArgumentException("AI-Paths must not be null!");

        // Namen der KIs auslesen
        try {
            File f = new File(teamAAiPath);
            this.teamAName = f.getName();
            this.teamAName = this.teamAName.substring(0, this.teamAName.lastIndexOf('.'));
        } catch (Exception e) {
            // do nothing here
        }

        try {
            File f = new File(teamBAiPath);
            this.teamBName = f.getName();
            this.teamBName = this.teamBName.substring(0, this.teamBName.lastIndexOf('.'));
        } catch (Exception e) {
            // do nothing here
        }

        this.tickDataList = new ArrayList<TickData>();
        this.gameInformation = gameInformation;
    }

    /**
     * Überladener Konstruktor der Klasse DataHandler, der beim Laden einer bereits berechneten Simulation benutzt wird.
     * Der neu erstellte DataHandler stößt keine Berechnungen an (erstellt keinen {@link CalculationThread}), sondern
     * stellt nur die Daten für die Darstellung zur Verfügung.
     * 
     * @param simulationFilePath
     *            Der Dateipfad, in dem die gespeicherten(bereites berechneten) Simulationsdaten zu finden sind.
     * @throws SimulationSaveFileException
     *             Wird geworfen, falls Datei nicht geladen werden kann (Keine Leserechte, Datei nicht vorhanden,
     *             ungültiger Inhalt)
     */
    public DataHandler(String simulationFilePath) throws SimulationSaveFileException {
        if (simulationFilePath == null) {
            throw new SimulationSaveFileException("The file path was null", new IllegalArgumentException());
        }
        this.tickDataList = new ArrayList<TickData>();

        final File sosiCandidate = new File(simulationFilePath);

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            final ZipFile zipFile = new ZipFile(sosiCandidate);

            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            SavedGameInformation gameInfo = null;

            final List<ZipEntry> tickEntryCandidates = new ArrayList<ZipEntry>();

            while (zipEntries.hasMoreElements()) {
                ZipEntry entry = zipEntries.nextElement();
                if (entry.isDirectory()) {

                    // ignore directories
                } else if (entry.getName().equals(ZIP_ENTRY_GAMEINFO)) {
                    gameInfo = loadGameInformations(docBuilder, zipFile.getInputStream(entry));
                } else if (entry.getName().matches(
                        ZIP_ENTRY + "\\" + ZIP_ENTRY_DELIMITER + "(\\d+)" + "\\" + ZIP_ENTRY_FILETYPE)) {
                    tickEntryCandidates.add(entry);
                } else {
                    // ignore other files
                }

            }

            if (gameInfo == null) {
                zipFile.close();
                throw new SimulationSaveFileException("No GameInformation entry found!");
            }

            this.teamAName = gameInfo.getTeamAName();
            this.teamBName = gameInfo.getTeamBName();
            // final int expectedTickCount = gameInfo.getGameInformation().getMaximumTickNumber();

            this.gameInformation = gameInfo.getGameInformation();
            INewTickDataHandler newTickHandler = new NewTickDataHandler();

            this.workerThread = new FileLoadingThread(newTickHandler, this.gameInformation, tickEntryCandidates,
                    zipFile, docBuilder);
            this.workerThread.start();

        } catch (ZipException zipException) {
            throw new SimulationSaveFileException("Invalid sosi file!", zipException);
        } catch (IOException ioException) {
            throw new SimulationSaveFileException("An error occured, while loading the sosi file!", ioException);
        } catch (ParserConfigurationException parserConfigException) {
            throw new SimulationSaveFileException("An error occured, while loading the sosi file!",
                    parserConfigException);
        }
    }

    /**
     * Liest die gespeicherten Simulationsinformationen aus der XML-Speicherdatei aus
     * 
     * @param docBuilder
     * @param zipInputStream
     * @return SavedGameInformation-Instanz mit den gelesenen Simulationsinformationen
     * @throws SimulationSaveFileException
     */
    private static SavedGameInformation loadGameInformations(DocumentBuilder docBuilder, InputStream zipInputStream)
            throws SimulationSaveFileException {
        Document document = null;
        try {
            document = docBuilder.parse(zipInputStream);
        } catch (SAXException e) {
            throw new SimulationSaveFileException("Failed while parsing the gameinfo!", e);
        } catch (IOException e) {
            throw new SimulationSaveFileException("Failed while parsing the gameinfo!", e);
        }
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        String rootName = root.getNodeName();

        if (rootName.equals(IDENTIFIER_FILE)) {
            try {
                int tickCount = Integer.valueOf(getTagName(root, IDENTIFIER_TICK_COUNT));
                double ball_diameter = Double.valueOf(getTagName(root, IDENTIFIER_BALL_DIAMETER));
                double player_diameter = Double.valueOf(getTagName(root, IDENTIFIER_PLAYER_DIAMETER));
                double field_length = Double.valueOf(getTagName(root, IDENTIFIER_FIELD_LENGTH));
                double field_width = Double.valueOf(getTagName(root, IDENTIFIER_FIELD_WIDTH));
                double goal_size = Double.valueOf(getTagName(root, IDENTIFIER_GOAL_SIZE));
                SoSiGameInformation gameInfo = new SoSiGameInformation(field_width, field_length, goal_size,
                        player_diameter, ball_diameter, tickCount);
                String teamA = getTagName(root, IDENTIFIER_TEAM_A_NAME);
                String teamB = getTagName(root, IDENTIFIER_TEAM_B_NAME);
                return new SavedGameInformation(teamA, teamB, gameInfo);
            } catch (NumberFormatException nException) {
                throw new SimulationSaveFileException("Wrong number format!", nException);
            } catch (NullPointerException nException) {
                throw new SimulationSaveFileException("null is not a number!", nException);
            }
        } else {
            throw new SimulationSaveFileException("Malformed GameInfo file!");
        }
    }

    /**
     * Liefert den Inhalt eines bestimmten XML-Tags abhängig des aktuellen XML-Element-Knotens
     * 
     * @param root
     *            XML-Element-Knoten
     * @param tag
     *            Name des abzufragenden Tags
     * @return Inhalt des Tags, falls dieser vorhanden und gefunden wurde, <code>null</code> andernfalls.
     */
    static String getTagName(Element root, String tag) {
        NodeList listA = root.getElementsByTagName(tag);
        if (listA.getLength() > 0) {
            if (listA.item(0).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) listA.item(0);
                if (e.getChildNodes() != null && e.getChildNodes().getLength() > 0) {
                    return e.getChildNodes().item(0).getNodeValue();
                }
            }
        }
        return null;
    }

    /**
     * Wird verwendet, um eine bereits berechnete Simulation zu speichern. Hierzu wird ein Dateipfad angegeben, in dem
     * die berechneten Daten gespeichert werden. Dies geschieht in der Form, sodass die Klasse {@link DataHandler} diese
     * laden kann, um sie zur Darstellung zur Verfügung zu stellen. Das Speichern einer noch nicht fertig berechneten
     * Simulation ist nicht möglich, da der entsprechenden Menüpunkt "Simulation speichern", solange ausgegraut ist bis
     * die Berechnung nicht abgeschlossen ist.<br>
     * <br>
     * Falls die Berechnung der Simulation noch nicht abgeschlossen ist, wird eine Exception vom Typ
     * SimulationSaveFileException geschmissen.
     * 
     * @param path
     *            Der Dateipfad, in dem eine bereits berechnete Simulation gespeichert werden soll.
     * @throws SimulationSaveFileException
     */
    public void saveToFile(String path) throws SimulationSaveFileException {
        // Überprüfung, ob Berechnung der Simulation bereits abgeschlossen
        if (gameInformation != null && this.getSimulationTickCount() < gameInformation.getMaximumTickNumber()) {
            throw new SimulationSaveFileException("Simulationcalculation not finished yet");
        }

        final SaveProgressInformation currentSaveProgressInformation = (DataHandler.saveProgressInformation != null) ? DataHandler.saveProgressInformation
                : new SaveProgressInformation();

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(new File(path)));
            zipOutputStream.setLevel(ZIP_COMPRESSION_LEVEL);
            zipOutputStream.setMethod(ZIP_COMPRESSION_METHOD);
            zipOutputStream.setComment(ZIP_COMMENT);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            saveGameInformations(docBuilder, transformer, zipOutputStream);
            saveTicks(docBuilder, transformer, zipOutputStream, currentSaveProgressInformation);

            zipOutputStream.close();

        } catch (Exception e) {
            currentSaveProgressInformation.setProgress(100);
            throw new SimulationSaveFileException("Simulationfile could not be saved", e);
        } finally {
            currentSaveProgressInformation.setProgress(100);
        }
    }

    /**
     * Speichert die Simulationsinformationen, wie Teamnamen, Größenangaben et.c
     * 
     * @param docBuilder
     * @param transformer
     * @param zipOutputStream
     * @throws IOException
     * @throws TransformerException
     */
    private void saveGameInformations(DocumentBuilder docBuilder, Transformer transformer,
            ZipOutputStream zipOutputStream) throws IOException, TransformerException {

        // root element
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement(IDENTIFIER_FILE);
        doc.appendChild(rootElement);

        // save game specific infos
        Element gameinfo = doc.createElement(IDENTIFIER_GAME_INFO);
        // gameinfo.appendChild(doc.createTextNode("\n"));

        Element ballDiameter = doc.createElement(IDENTIFIER_BALL_DIAMETER);
        ballDiameter.appendChild(doc.createTextNode(String.valueOf(gameInformation.getBallDiameter())));
        gameinfo.appendChild(ballDiameter);
        // gameinfo.appendChild(doc.createTextNode("\n"));

        Element fieldLength = doc.createElement(IDENTIFIER_FIELD_LENGTH);
        fieldLength.appendChild(doc.createTextNode(String.valueOf(gameInformation.getFieldLength())));
        gameinfo.appendChild(fieldLength);
        // gameinfo.appendChild(doc.createTextNode("\n"));

        Element fieldWidth = doc.createElement(IDENTIFIER_FIELD_WIDTH);
        fieldWidth.appendChild(doc.createTextNode(String.valueOf(gameInformation.getFieldWidth())));
        gameinfo.appendChild(fieldWidth);
        // gameinfo.appendChild(doc.createTextNode("\n"));

        Element goalSize = doc.createElement(IDENTIFIER_GOAL_SIZE);
        goalSize.appendChild(doc.createTextNode(String.valueOf(gameInformation.getGoalSize())));
        gameinfo.appendChild(goalSize);
        // gameinfo.appendChild(doc.createTextNode("\n"));

        Element tickCount = doc.createElement(IDENTIFIER_TICK_COUNT);
        tickCount.appendChild(doc.createTextNode(String.valueOf(gameInformation.getMaximumTickNumber())));
        gameinfo.appendChild(tickCount);
        // gameinfo.appendChild(doc.createTextNode("\n"));

        Element playerDiameter = doc.createElement(IDENTIFIER_PLAYER_DIAMETER);
        playerDiameter.appendChild(doc.createTextNode(String.valueOf(gameInformation.getPlayerDiameter())));
        gameinfo.appendChild(playerDiameter);
        // gameinfo.appendChild(doc.createTextNode("\n"));

        rootElement.appendChild(gameinfo);

        // Informations for playback
        Element simulationInfos = doc.createElement(IDENTIFIER_SIMULATION_INFOS);
        rootElement.appendChild(simulationInfos);

        Element team_a_ai_name = doc.createElement(IDENTIFIER_TEAM_A_NAME);
        team_a_ai_name.appendChild(doc.createTextNode(this.getTeamAName()));
        simulationInfos.appendChild(team_a_ai_name);

        Element team_b_ai_name = doc.createElement(IDENTIFIER_TEAM_B_NAME);
        team_b_ai_name.appendChild(doc.createTextNode(this.getTeamBName()));
        simulationInfos.appendChild(team_b_ai_name);

        ZipEntry entry = new ZipEntry(ZIP_ENTRY_GAMEINFO);
        zipOutputStream.putNextEntry(entry);

        // write the content into xml file
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(zipOutputStream);

        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);
        zipOutputStream.closeEntry();
        // zipOutputStream.close();
    }

    /**
     * Speichert sämtliche TickData-Instanzen
     * 
     * @param docBuilder
     * @param transformer
     * @param zipOutputStream
     * @param saveProgressInformation
     * @throws TransformerException
     * @throws IOException
     */
    private void saveTicks(DocumentBuilder docBuilder, Transformer transformer, ZipOutputStream zipOutputStream,
            SaveProgressInformation saveProgressInformation) throws TransformerException, IOException {
        int listSize = this.tickDataList.size();
        int tickParts = (int) Math.round(Math.ceil((double) listSize / ZIP_ENTRY_TICKDATA_COUNT));
        for (int partCounter = 0, tickCounter = 0; partCounter < tickParts; ++partCounter) {
            // int newTickCounter = (partCounter < tickParts) ? tickCounter += ZIP_ENTRY_TICKDATA_COUNT : listSize - 1;
            List<TickData> partList = tickDataList.subList(tickCounter,
                    (partCounter < tickParts - 1) ? tickCounter += ZIP_ENTRY_TICKDATA_COUNT : listSize);
            saveTickPart(docBuilder, transformer, zipOutputStream, partList, partCounter);
            saveProgressInformation.setProgress((int) Math.round(((double) (partCounter + 1) / tickParts) * 100));
        }
    }

    /**
     * Speichert einen einzelnen TickData-Bereich
     * 
     * @param docBuilder
     * @param transformer
     * @param zipOutputStream
     * @param tickDataList
     * @param partCount
     * @throws TransformerException
     * @throws IOException
     */
    private void saveTickPart(DocumentBuilder docBuilder, Transformer transformer, ZipOutputStream zipOutputStream,
            List<TickData> tickDataList, int partCount) throws TransformerException, IOException {

        // root element
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement(IDENTIFIER_FILE);
        doc.appendChild(rootElement);

        // Add TickData-List
        for (TickData tick : tickDataList) {
            rootElement.appendChild(doc.createTextNode("\n"));
            rootElement.appendChild(doc.createTextNode("\n"));
            rootElement.appendChild(convertToXmlElement(doc, tick));
        }

        ZipEntry entry = new ZipEntry(ZIP_ENTRY_TICKDATA_PART + partCount + ZIP_ENTRY_FILETYPE);
        zipOutputStream.putNextEntry(entry);

        // write the content into xml file
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(zipOutputStream);

        transformer.transform(source, result);
        // zipOutputStream.closeEntry();
    }

    /**
     * Konvertiert einen TickData-Datensatz zu einem XML-Element
     * 
     * @param doc
     *            Aktuelles Document
     * @param tick
     *            Zu konvertierender Tick
     * @return In XML konvertierte {@link Element}-Instanz des Ticks
     */
    private Element convertToXmlElement(Document doc, TickData tick) {
        Element tickDataElement = doc.createElement(IDENTIFIER_TICK);

        // set attribute to staff element
        tickDataElement.setAttribute(IDENTIFIER_TICK_ID, Integer.toString(tick.getTickPosition()));

        // Ball-Position
        tickDataElement.appendChild(convertPositionToXmlElement(doc, tick.getBallPosition(), IDENTIFIER_BALL_POSITION));

        // Goals Team A
        Element goalsTeamA = doc.createElement(IDENTIFIER_GOALS_TEAM_A);
        goalsTeamA.appendChild(doc.createTextNode(Integer.toString(tick.getGoalsTeamA())));
        tickDataElement.appendChild(goalsTeamA);

        // Goals Team B
        Element goalsTeamB = doc.createElement(IDENTIFIER_GOALS_TEAM_B);
        goalsTeamB.appendChild(doc.createTextNode(Integer.toString(tick.getGoalsTeamB())));
        tickDataElement.appendChild(goalsTeamB);

        // Debug Messages
        Element debugTeamA = doc.createElement(IDENTIFIER_DEBUG_TEAM_A);
        debugTeamA.appendChild(doc.createTextNode(tick.getDebugMessageTeamA()));
        tickDataElement.appendChild(debugTeamA);

        Element debugTeamB = doc.createElement(IDENTIFIER_DEBUG_TEAM_B);
        debugTeamB.appendChild(doc.createTextNode(tick.getDebugMessageTeamB()));
        tickDataElement.appendChild(debugTeamB);

        // Player Positionen
        for (Position pos : tick.getPlayerPositionsTeamA()) {
            tickDataElement.appendChild(convertPositionToXmlElement(doc, pos, IDENTIFIER_POSITION_PREFIX_TEAM_A));
        }
        for (Position pos : tick.getPlayerPositionsTeamB()) {
            tickDataElement.appendChild(convertPositionToXmlElement(doc, pos, IDENTIFIER_POSITION_PREFIX_TEAM_B));
        }

        // TickEvent
        TickEvent event = tick.getTickEvent();
        Element eventElement = doc.createElement(IDENTIFIER_EVENT);
        if (event != null)
            eventElement.appendChild(doc.createTextNode(tick.getTickEvent().toString()));
        else
            eventElement.appendChild(doc.createTextNode(""));
        tickDataElement.appendChild(eventElement);

        return tickDataElement;
    }

    /**
     * Konvertiert eine Positionsangabe in ein XML-Dokument
     * 
     * @param doc
     *            Aktuelles Document
     * @param position
     *            Zu konvertierende Position
     * @param prefix
     *            Zu verwendender Prefix für den XML-Tag
     * @return In XML konvertierte {@link Element}-Instanz der Position
     */
    private Element convertPositionToXmlElement(Document doc, Position position, String prefix) {
        Element positionElement = doc.createElement(prefix + IDENTIFIER_POSITION_SUFIX);

        Element x = doc.createElement(IDENTIFIER_POSITION_X);
        x.appendChild(doc.createTextNode(Double.toString(position.getX())));
        positionElement.appendChild(x);

        Element y = doc.createElement(IDENTIFIER_POSITION_Y);
        y.appendChild(doc.createTextNode(Double.toString(position.getY())));
        positionElement.appendChild(y);

        return positionElement;
    }

    /**
     * Der Name der KI von Team A, die ausgewählt wurde.
     * 
     * @return Der Name der KI von Team A.
     */
    public String getTeamAName() {
        return this.teamAName;
    }

    /**
     * Der Name der KI von Team B, die ausgewählt wurde.
     * 
     * @return Der Name der KI von Team B.
     */
    public String getTeamBName() {
        return this.teamBName;
    }

    /**
     * Dient dazu die Tick-Position zu erfragen, die als letztes berechnet und der Liste {@link #tickDataList}
     * hinzugefügt worden ist.
     * 
     * @return Tick-Position, die als letztes berechneten worden ist.
     */
    public int getSimulationTickCount() {
        return this.tickDataList.size();
    }

    /**
     * Dient dazu die Tick-Daten eines bestimmten Ticks zu erfragen.
     * 
     * @param tickPosition
     *            Die Position, von der die Tick-Daten zurückgegeben werden soll.
     * @return Die Tick-Daten, an der Position "tickPosition" aus der Liste, der berechneten Simulation.
     */
    public TickData getTickPositionData(int tickPosition) {
        if (tickPosition >= this.tickDataList.size() || tickPosition < 0)
            return null;
        else
            return this.tickDataList.get(tickPosition);
    }

    /**
     * Bricht eine ggf. aktive Berechnung der Simulation ab. Dabei wird der Thread beendet und ggf. berechnete
     * Zwischenergebnisse verworfen.
     */
    public void abort() {
        if (this.workerThread != null)
            this.workerThread.abort();
    }

    /**
     * Gibt zurück, ob die Berechnung der Simulation abgeschlossen wurde.<br>
     * 
     * @return <b>True</b>, falls die Berechnung der Simulation bereits abgeschlossen wurde, <b>False</b> andernfalls.
     */
    public boolean isSimulationCalculationFinished() {
        return this.workerThread == null || !this.workerThread.isAlive();
    }

    /**
     * Liefert eine neue Instanz der Klasse {@link NewTickDataHandler} zurück. Die Methode wird benötigt, um in
     * geerbeten Klassen eine Instanz dieser privaten Klasse zu erstellen. Konkret wird dies verwendet, um in den
     * JUnit-Tests eine geänderte DataHandler-Instanz erstellen zu können, mit welcher z.B. die Simulation ohne
     * Zufallswerte durchgeführt werden kann. Dies ist für Regression-Tests notwendig, um zu Testen, ob zwei
     * Simulationen bei identischen Vorgaben nach einer Änderung weiter identisch berechnet werden.
     * 
     * @return Neue {@link NewTickDataHandler} Instanz
     */
    protected NewTickDataHandler getNewTickDataHandler() {
        return new NewTickDataHandler();
    }

    /**
     * Interne Klasse, die ein Interface zur Verfügung stellt, um neue Tick-Daten abzuspeichern. Dies soll den
     * schreibenden Zugriff auf die DataHandler Klasse aufs minimalste beschränken, weil die Konsistenz der Liste der
     * Tick-Daten im DataHandler erhalten werden soll.
     */
    private class NewTickDataHandler implements INewTickDataHandler {

        /**
         * Methode, um neue Tick-Daten in die Liste der Tick-Daten innerhalb des DataHandlers zu speichern. Der
         * CalculationThread hat als einziger eine Referenz auf diese Klasse.
         */
        @Override
        public void addNewTickData(int goalsTeamA, int goalsTeamB, Position ballPosition,
                List<Position> playerPositionsTeamA, List<Position> playerPositionsTeamB, TickEvent tickEvent,
                String debugMessageTeamA, String debugMessageTeamB) {
            int newTickPosition = tickDataList.size();
            tickDataList.add(new TickData(newTickPosition, goalsTeamA, goalsTeamB, ballPosition, playerPositionsTeamA,
                    playerPositionsTeamB, tickEvent, debugMessageTeamA, debugMessageTeamB));

            assert tickDataList.size() <= gameInformation.getMaximumTickNumber() : "Die Anzahl der maximal zu "
                    + "berechnenden Simulationsschritte wurde überschritten";
        }
    }

    /**
     * Erstellt eine neue {@link IProgressInformation}-Instanz, welche über den Fortschritt des Speicherns informiert
     * und gibt diese zurück. Der Wertebereich des Fortschritt liegt dabei in Prozent von 0 bis 100.<br>
     * Die aktuelle Rückgabewert dieser Funktion wird für alle Speichervorgänge wiederverwendet, bis diese Methode
     * erneut aufgerufen wird.
     * 
     * @return IProgressInformation-Instanz, welche über den Fortschritt des Speicherns informiert
     */
    public static IProgressInformation getNewSaveProgressInformation() {
        saveProgressInformation = new SaveProgressInformation();
        return saveProgressInformation;
    }

    /**
     * Gibt GameInformation-Instanz der aktuellen Simulation zurück
     * 
     * @return GameInformation-Instanz
     */
    public GameInformation getGameInformation() {
        return this.gameInformation;
    }

    /**
     * Beinhaltet die Daten über eine Simulation, welche aus einer Simulationsspeicherdatei geladen wurden.
     */
    private static class SavedGameInformation {
        private String teamA, teamB;
        private SoSiGameInformation gameInfo;

        public SavedGameInformation(String teamA, String teamB, SoSiGameInformation gameInfo) {
            this.teamA = teamA;
            this.teamB = teamB;
            this.gameInfo = gameInfo;
        }

        public String getTeamAName() {
            return teamA;
        }

        public String getTeamBName() {
            return teamB;
        }

        public SoSiGameInformation getGameInformation() {
            return gameInfo;
        }
    }

    /**
     * Von Exception erbende Klasse, welche geworfen wird, wenn ein Simulationsdatei nicht geladen werden konnte. Dies
     * ist u.a. der Fall, wenn für die Datei keine Leserechte vorliegen, die Datei nicht vorhanden ist oder einen
     * ungültiger Inhalt besitzt.
     */
    public static class SimulationSaveFileException extends Exception {
        /**
         * Random generated
         */
        private static final long serialVersionUID = -4517113106094304001L;

        public SimulationSaveFileException(String message) {
            super(message);
        }

        public SimulationSaveFileException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }

    /**
     * Hält den Fortschritt beim Speichern fest.
     */
    private static class SaveProgressInformation implements IProgressInformation {
        private volatile int progress = 0;

        /**
         * Setzt den Fortschritt der Speicheroperation.
         * 
         * @param progress
         *            Ein Wert zwischen 0 und 100, der angibt wie weit der Speichervorgang fortgeschritten ist.
         */
        void setProgress(int progress) {
            this.progress = progress;
        }

        @Override
        public int getProgress() {
            return this.progress;
        }

    }
}
