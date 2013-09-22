package SoSi.Model.GameObjects;

import SoSi.Model.Calculation.Tick;
import SoSi.Model.GamePhysics.GamePhysic;
import sep.football.Position;

/**
 * Beinhaltet allgemeine Daten eines Balles, wie die Referenz auf den ballführenden Spieler und den letzen Spieler, der
 * den Ball besessen hat ebenso wie die momentanen Koordinaten des Balles und seine Bewegungsrichtung.
 */
public class BallGameObject extends MoveableRadialGameObject {

    /**
     * Die maximale Geschwindigkeit in Metern/Sekunde, die ein Ball durch einen Schuss eines Spielers erreichen kann.
     * Diese Geschwindigkeit wird erreicht, wenn der Ball von einem Spieler mit voller Kraft geschossen wird.<br>
     * (Innerhalb der physikalischen Berechnungen kann jedoch eine höhere Geschwindigkeit erreicht werden.)
     */
    public static final double MAX_SHOOTING_SPEED = 20;

    /**
     * Die Reibung in Metern/Sekunde^2, die stets auf den Ball wirkt, unabhängig von der Geschwindigkeit.
     */
    public static final double FRICTION = 4;

    /**
     * Die Referenz auf den Spieler, der momentan den Ball besitzt.
     */
    private PlayerGameObject ballPossession;

    /**
     * Die Referenz auf den Spieler, der mit dem Ball kollidiert
     */
    private PlayerGameObject ballContact;

    /**
     * Die Referenz auf den Spieler, der den Ball zuletzt berührt hat.
     */
    private PlayerGameObject lastBallContact;

    /**
     * Die Referenz auf den Spieler, der zuletzt in Ballbesitz war.
     */
    private PlayerGameObject lastBallPossession;

    /**
     * Erstellt ein Ball-Objekt. Dieses Ball-Objekt wird von der {@link Tick} Klasse bei der Erstellung einer neuen
     * Simulation erzeugt.
     * 
     * @param position
     *            Die Position innerhalb des Spielfeldes, auf die der Mittelpunkt des Balles gesetzt werden soll.
     * @param diameter
     *            Der Durchmesser des Balles.
     */
    public BallGameObject(Position position, double diameter) {
        super(position, diameter);
    }

    /**
     * Gibt die maximale Geschwindigkeit des Balles in Metern/Tick zurück, die der Ball durch einen Schuss eines
     * Spielers erreichen kann.
     * 
     * @return Die maximale Geschwindigkeit des Balles bei einem Schuss durch einen Spieler.
     */
    public double getMaxShootingSpeed() {
        return GamePhysic.convertVelocity(BallGameObject.MAX_SHOOTING_SPEED);
    }

    /**
     * Gibt den Spieler zurück, der gerade im Ballbesitz ist.
     * 
     * @return Spieler der gerade den Ball besitzt. Falls kein Spieler gerade im Ballbesitz ist, liefert die Methode
     *         <b>null</b> zurück.
     */
    public PlayerGameObject getBallPossession() {
        return this.ballPossession;
    }

    /**
     * Gibt den Spieler zurück, der gerade den Ball berührt.
     * 
     * @return Spieler, der gerade den Ball berührt. Falls kein Spieler gerade den Ball berührt, liefert die Methode
     *         <b>null</b> zurück.
     */
    public PlayerGameObject getBallContact() {
        return this.ballContact;
    }

    /**
     * Gibt einem Spieler den Ballbesitz.
     * 
     * @param player
     *            Spieler, dem der Ballbesitz zugeschrieben wird. Falls ein ungültiger Spieler (falsche ID etc.) gewählt
     *            wird, verbleibt der Wert der Variable im BallGameObject <b>null</b>.
     */
    public void setBallPossession(PlayerGameObject player) {
        if (this.ballPossession != null)
            this.lastBallPossession = this.ballPossession;
        this.ballPossession = player;

        this.setBallContact(player);
    }

    /**
     * Setzt den Spieler, welcher aktuell (im aktuellen Tick) den Ball berührt. <br>
     * (Falls kein Spieler den Ball berührt, wird die Methode mit dem Parameter <code>null</code> aufgerufen)
     * 
     * @param player
     *            Spieler, welcher aktuell den Ball berührt
     */
    public void setBallContact(PlayerGameObject player) {
        if (player != null)
            this.lastBallContact = player;
        this.ballContact = player;
    }

    /**
     * Gibt den Spieler zurück, der zuletzt in Ballbesitz war.
     * 
     * @return Das Spielerobjekt, das zuletzt den Ball berührt hat. Falls kein Spieler vorher den Ball berührt hat (z.B.
     *         beim Anstoß), so wird <b>null</b> zurückgegeben.
     */
    public PlayerGameObject getLastBallPosession() {
        return this.lastBallPossession;
    }

    /**
     * Gibt den Spieler zurück, der zuletzt den Ball berührt hat.
     * 
     * @return Das Spielerobjekt, das zuletzt den Ball berührt hat. Falls zuvor nie ein Spieler den Ball berührt hat
     *         (z.B. beim Anstoß), so wird <b>null</b> zurückgegeben.
     */
    public PlayerGameObject getLastBallContact() {
        return this.lastBallContact;
    }

}
