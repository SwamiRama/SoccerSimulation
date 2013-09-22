package SoSi.Model.GameObjects;

import sep.football.Position;
import SoSi.Model.Calculation.Vector2D;

/**
 * Beschreibt ein rundes Spielobjekt, dessen Bewegung mit einem Richtungsvektor angegeben wird.
 */
public abstract class MoveableRadialGameObject extends RadialGameObject {

	/**
	 * Setzt für das bewegliche Spielobjekt eine Bewegungsrichtung und eine Bewegungsgeschwindigkeit in Metern/Tick
	 * anhand eines Richtungsvektors. Der Richtungsvektor wird immer von der aktuellen Position des Spielobjekts aus
	 * angegeben, das bedeutet, dass der Richtungsvektor seinen Ursprung in der momentanen Position des Spielobjekts
	 * hat. Der Vektor (0/0) bedeutet dabei Stillstand. Diese Funktion kommt bei den Spieler und dem Ball zum Tragen.<br>
	 * Falls die KI einen Richtungswechsel bei einem Spieler entscheidet oder falls sie sich für einen Schuss
	 * entscheidet, wird die Variable movementDirection aus den ActionHandlern heraus angepasst. Diese Anpassung hat auf
	 * die Simulation keine unmittelbaren Folgen. Erst wenn die Physikberechnungen die movementDirection auswerten und
	 * die neue Position setzen, werden die Konsequenzen der Anpassung ersichtlich.<br>
	 * Die Werte des neuen Vektors müssen nur innerhalb der double Grenzen liegen. Die Spielphysik passt zu große Werte
	 * (im Betrag) bei der Berechnung automatisch an.
	 */
	protected Vector2D movementDirection;

	/**
	 * Erstellt ein Spielobjekt auf dem Feld und setzt seine Position.
	 * 
	 * @param position
	 *            Die Position innerhalb des Spielfeldes.
	 * @param diameter
	 *            Der Durchmesser des Spielobjekts.
	 */
	public MoveableRadialGameObject(Position position, double diameter) {
		super(position, diameter);

		this.movementDirection = new Vector2D(0, 0);
	}

	/**
	 * Gibt eine Bewegungsrichtung und -geschwindigkeit in Metern/Tick für das Spielobjekt, anhand eines
	 * Richtungsvektors, zurück.
	 * 
	 * @return Bewegungsrichtung des Spielobjekts.
	 */
	public Vector2D getMovementDirection() {
		return movementDirection;
	}

	/**
	 * Setzt die MovementDirection des Objekts.
	 * 
	 * @param movementDirection
	 *            Neue Bewegungsrichtung und -geschwindigkeit in Metern/Tick, auf die das Objekt gesetzt werden soll.
	 * @see MoveableRadialGameObject#movementDirection
	 * @throws IllegalArgumentException
	 *             Die Exception, wenn movementDirection den Wert null besitzt.
	 */
	public void setMovementDirection(Vector2D movementDirection) {
		if (movementDirection == null)
			throw new IllegalArgumentException("movementDirection is null");

		this.movementDirection = movementDirection;
	}

	/**
	 * Setzt die Position eines beweglichen Spielobjekts. Die Werte der neuen Position müssen nur innerhalb der double
	 * Grenzen liegen. Die Spielphysik passt zu große oder zu kleine Werte bei der Berechnung automatisch an.
	 * 
	 * @param position
	 *            Position des Spielobjekts.
	 * @throws IllegalArgumentException
	 *             Die Exception, wenn position den Wert null besitzt.
	 */
	public void setPosition(Position position) {
		if (position == null)
			throw new IllegalArgumentException("position is null");

		this.position = position;
	}
}
