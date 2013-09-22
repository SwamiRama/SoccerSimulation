package SoSi.Model.GameObjects;

import SoSi.Model.SoSiPosition;

import sep.football.GameInformation;

/**
 * Beinhaltet allgemeine Daten eines Tores (die zwei Pfosten), die das Tor bilden.
 */
public class GoalGameObject {

    /**
     * Linke Position des Pfostens (Sichtweise: vom Mittelpunkt auf das Tor).
     */
    private PostGameObject postLeft;

    /**
     * Rechte Position des Pfostens (Sichtweise: vom Mittelpunkt auf das Tor).
     */
    private PostGameObject postRight;

    /**
     * Konstruktor für ein Tor-Objekt. Mit Hilfe der gameInformation wird das Tor-Objekt mit der für das Spiel richtigen
     * Größe erstellt.
     * 
     * @param gameInformation
     *            Quelle für die Größe des Tores.
     * @param isLeft
     *            Boolean-Flag der angibt, auf welcher Seite sich das Tor-Objekt befindet
     */
    public GoalGameObject(GameInformation gameInformation, boolean isLeft) {
        double halfFieldWidth = gameInformation.getFieldWidth() / 2;
        double halfGoalSize = gameInformation.getGoalSize() / 2;

        if (isLeft) {
            this.postLeft = new PostGameObject(new SoSiPosition(0, halfFieldWidth + halfGoalSize));
            this.postRight = new PostGameObject(new SoSiPosition(0, halfFieldWidth - halfGoalSize));
        } else {
            this.postLeft = new PostGameObject(new SoSiPosition(gameInformation.getFieldLength(), halfFieldWidth
                    - halfGoalSize));
            this.postRight = new PostGameObject(new SoSiPosition(gameInformation.getFieldLength(), halfFieldWidth
                    + halfGoalSize));
        }
    }

    /**
     * Getter-Methode für das Pfostenobjekt dieses Torobjekts, welches sich auf der linken Seite befindet (Sichtweise:
     * vom Mittelpunkt auf das Tor blickend).
     * 
     * @return Linke Position des Pfostens. Falls der Pfosten beim Aufruf der Methode nicht vorhanden sein sollte, wird
     *         <b>null</b> zurückgegeben.
     */
    public PostGameObject getPostLeft() {
        return postLeft;
    }

    /**
     * Getter-Methode für das Pfostenobjekt dieses Torobjekts, welches sich auf der rechten Seite befindet (Sichtweise:
     * vom Mittelpunkt auf das Tor blickend).
     * 
     * @return Rechte Position des Pfostens. Falls der Pfosten beim Aufruf der Methode nicht vorhanden sein sollte, wird
     *         <b>null</b> zurückgegeben.
     */
    public PostGameObject getPostRight() {
        return postRight;
    }

}
