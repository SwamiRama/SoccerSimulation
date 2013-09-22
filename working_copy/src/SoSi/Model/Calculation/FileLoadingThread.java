package SoSi.Model.Calculation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sep.football.GameInformation;
import sep.football.Position;

import SoSi.Model.SoSiPosition;
import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.DataHandler.SimulationSaveFileException;

/**
 * Dient zum Laden von TickDatas einer Simulationsdatei. Das Laden erfolgt dabei asynchron, so das bereits geladene
 * TickDatas bereits angezeigt werden können, während verbleibende Einträge noch geladen werden.<br>
 * Aus Anwendersicht ist (bis auf den Dialog, welcher die jeweilige Aktion startet) die Berechnung und das Laden von
 * Simulationen daher nicht zu unterscheiden.
 */
public class FileLoadingThread extends AbortableThread {

    /**
     * Referenz auf die innere Klasse des DataHandler. Sie dient dazu, neue tick Daten in die Liste der bereits
     * berechneten tick Daten zu speichern.
     */
    private final INewTickDataHandler newTickDataHandler;

    /**
     * Liste von ZipEntries, welche ausgelesen werden sollen.
     * 
     * @see ZipEntry
     */
    private List<ZipEntry> tickEntryCandidates;

    /**
     * ZipFile, aus welchem die Daten gelesen werden
     * 
     * @see ZipFile
     */
    private final ZipFile zipFile;

    /**
     * DocumentBuilder für XML-Daten
     * 
     * @see DocumentBuilder
     */
    private final DocumentBuilder docBuilder;

    /**
     * Anzahl der erwarteten Ticks, welche geladen werden sollen
     */
    private final int expectedTickCount;

    /**
     * Anzahl der tatsächlich geladenen Ticks
     */
    private int tickCounter = 0;

    /**
     * Erstellt eine neue FileLoadingThread-Instanz.
     * 
     * @param newTickDataHandler
     *            Klasseninstanz, welche neue Simulationszwischenstände entgegennimmt.
     * @param gameInformation
     *            Die Parameter, die die Rahmenbedingungen für das Spiel bilden(Festlegung der Spielfeldgröße,
     *            Durchmesser der Spieler, des Balles etc.).
     * @param tickEntryCandidates
     *            Liste von ZipEntries, welche ausgelesen werden sollen.
     * 
     * @param zipFile
     *            ZipFile, aus welchem die Daten gelesen werden
     * @param docBuilder
     *            DocumentBuilder für XML-Daten
     * @see FileLoadingThread
     * @see INewTickDataHandler
     */
    public FileLoadingThread(INewTickDataHandler newTickDataHandler, GameInformation gameInformation,
            List<ZipEntry> tickEntryCandidates, ZipFile zipFile, DocumentBuilder docBuilder) {
        this.newTickDataHandler = newTickDataHandler;
        this.tickEntryCandidates = tickEntryCandidates;
        this.zipFile = zipFile;
        this.docBuilder = docBuilder;
        this.expectedTickCount = gameInformation.getMaximumTickNumber();
    }

    @Override
    public void run() {
        try {
            for (ZipEntry entry : tickEntryCandidates) {
                loadTickPart(docBuilder, zipFile.getInputStream(entry));

                if (this.getIsAborted())
                    break;
            }
            zipFile.close();

            if ((!this.getIsAborted()) && tickCounter != this.expectedTickCount) {
                throw new SimulationSaveFileException(String.format(
                        "Count of loaded ticks (%d) doesn't comply with expected count (%d)", tickCounter,
                        this.expectedTickCount));
            }

        } catch (IOException e) {
            System.err.println("An IOException occurred while loading the file!");
        } catch (SimulationSaveFileException e) {
            System.err.println(e.toString());
        }
    }

    // private static boolean isValidTickDataList(List<TickData> tickCandidates, int expectedTickCount) {
    // int counter = 0;
    // for (TickData currentTickData : tickCandidates) {
    // if (currentTickData.getTickPosition() != counter) {
    // return false;
    // }
    // ++counter;
    // }
    // return (counter == expectedTickCount);
    // }

    private void loadTickPart(DocumentBuilder docBuilder, InputStream zipInputStream)
            throws SimulationSaveFileException {
        Document document = null;
        try {
            document = docBuilder.parse(zipInputStream);
        } catch (SAXException e) {
            throw new SimulationSaveFileException("Failed while parsing the tickdata list!", e);
        } catch (IOException e) {
            throw new SimulationSaveFileException("Failed while parsing the tickdata list!", e);
        }
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        readTickDataList(root);
    }

    private void readTickDataList(Element root) throws SimulationSaveFileException {
        NodeList nodeList = root.getElementsByTagName(DataHandler.IDENTIFIER_TICK);
        if (nodeList != null) {
            for (int count = 0; count < nodeList.getLength(); ++count) {
                if (nodeList.item(count).getNodeType() == Node.ELEMENT_NODE) {
                    Element currentElement = (Element) nodeList.item(count);
                    // int tickPosition = Integer.valueOf(currentElement.getAttribute(DataHandler.IDENTIFIER_TICK_ID));
                    int goalsTeamA = Integer.valueOf(DataHandler.getTagName(currentElement,
                            DataHandler.IDENTIFIER_GOALS_TEAM_A));
                    int goalsTeamB = Integer.valueOf(DataHandler.getTagName(currentElement,
                            DataHandler.IDENTIFIER_GOALS_TEAM_B));
                    String eventString = DataHandler.getTagName(currentElement, DataHandler.IDENTIFIER_EVENT);
                    TickEvent event = (eventString == null) ? null : Enum.valueOf(TickEvent.class, eventString);
                    String debugMessageA = (DataHandler.getTagName(currentElement, DataHandler.IDENTIFIER_DEBUG_TEAM_A) == null) ? ""
                            : DataHandler.getTagName(currentElement, DataHandler.IDENTIFIER_DEBUG_TEAM_A);
                    String debugMessageB = (DataHandler.getTagName(currentElement, DataHandler.IDENTIFIER_DEBUG_TEAM_B) == null) ? ""
                            : DataHandler.getTagName(currentElement, DataHandler.IDENTIFIER_DEBUG_TEAM_B);

                    ArrayList<Position> postionsTeamA = getPositions(currentElement,
                            DataHandler.IDENTIFIER_POSITION_PREFIX_TEAM_A + DataHandler.IDENTIFIER_POSITION_SUFIX);
                    ArrayList<Position> postionsTeamB = getPositions(currentElement,
                            DataHandler.IDENTIFIER_POSITION_PREFIX_TEAM_B + DataHandler.IDENTIFIER_POSITION_SUFIX);
                    SoSiPosition ballPosition = (SoSiPosition) getPositions(currentElement,
                            DataHandler.IDENTIFIER_BALL_POSITION + DataHandler.IDENTIFIER_POSITION_SUFIX).get(0);

                    tickCounter++;
                    this.newTickDataHandler.addNewTickData(goalsTeamA, goalsTeamB, ballPosition, postionsTeamA,
                            postionsTeamB, event, debugMessageA, debugMessageB);
                }
            }
        } else {
            throw new SimulationSaveFileException("XML-Node not found: " + DataHandler.IDENTIFIER_TICK);
        }
    }

    private static ArrayList<Position> getPositions(Element root, String id) {
        ArrayList<Position> positionList = new ArrayList<Position>();
        NodeList nodeListPositions = root.getElementsByTagName(id);
        if (nodeListPositions != null) {
            for (int listCounter = 0; listCounter < nodeListPositions.getLength(); ++listCounter) {
                if (nodeListPositions.item(listCounter).getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) nodeListPositions.item(listCounter);
                    double xPosition = Double.valueOf(DataHandler.getTagName(elem, DataHandler.IDENTIFIER_POSITION_X));
                    double yPosition = Double.valueOf(DataHandler.getTagName(elem, DataHandler.IDENTIFIER_POSITION_Y));
                    SoSiPosition currentPos = new SoSiPosition(xPosition, yPosition);
                    positionList.add(currentPos);
                }
            }
        }
        return positionList;
    }

}
