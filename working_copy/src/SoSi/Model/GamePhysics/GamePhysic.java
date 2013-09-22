package SoSi.Model.GamePhysics;

import sep.football.Position;
import SoSi.Model.PlaybackHandler;
import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.SoSiTickInformation;
import SoSi.Model.Calculation.Vector2D;
import SoSi.Model.GameObjects.MoveableRadialGameObject;
import SoSi.Model.GameObjects.RadialGameObject;

/**
 * Die Klasse beschreibt eine Physik-Klasse, die die Methode calculate besitzt, in der die eigentliche physikalische
 * Berechnung erfolgt.<br>
 * Die Physik-Berechnungen des Spiels werden dadurch komfortabel in dazugehörige, funktional unterschiedliche Klassen
 * aufgeteilt.<br>
 * Diese werden im {@link GamePhysicsHandler} verwaltet und können ganz einfach aktiviert oder deaktiviert werden.
 */
public abstract class GamePhysic {

    /**
     * Die Klasse besitzt Informationen über die Banden-seiten eines Rechtecks, die bei einer Bandenkollision aktiv
     * sind, wenn ein Kollisionsobjekt mit dem Rechteck kollidiert.
     */
    protected static class BoundSides {
        public boolean leftSide;
        public boolean rightSide;
        public boolean topSide;
        public boolean bottomSide;

        /**
         * Erzeugt die Information über die Banden-seiten eines Rechtecks.
         * 
         * @param leftSide
         *            Gibt an, ob das Kollisionsobjekt mit der linken Seite der Bande kollidiert.
         * @param rightSide
         *            Gibt an, ob das Kollisionsobjekt mit der rechten Seite der Bande kollidiert.
         * @param topSide
         *            Gibt an, ob das Kollisionsobjekt mit der oberen Seite der Bande kollidiert.
         * @param bottomSide
         *            Gibt an, ob das Kollisionsobjekt mit der unteren Seite der Bande kollidiert.
         */
        public BoundSides(boolean leftSide, boolean rightSide, boolean topSide, boolean bottomSide) {

            this.leftSide = leftSide;
            this.rightSide = rightSide;
            this.topSide = topSide;
            this.bottomSide = bottomSide;
        }

        /**
         * Ergibt true, wenn (mindstens) eine der Banden-Seiten aktiv ist.
         * 
         * @return True, wenn mindestens eins der Banden-Seiten aktiv ist,
         */
        public boolean hasSides() {
            return (this.leftSide || this.rightSide || this.topSide || this.bottomSide);
        }
    }

    /**
     * Beinhaltet zwei Vektoren, die v.a. als Funktions-Rückgabewerte verwendet werden.
     */
    protected static class Vectors {
        public final Vector2D vector1;
        public final Vector2D vector2;

        /**
         * Erzeugt eine Sammlung von zwei Vektoren.
         * 
         * @param vector1
         *            Der erste Vektor.
         * @param vector2
         *            Der zweite Vektor.
         */
        public Vectors(Vector2D vector1, Vector2D vector2) {
            this.vector1 = vector1;
            this.vector2 = vector2;
        }
    }

    /**
     * Die Konstante, die die Überschneidungslänge angibt, mit der ein Spielobjekt mit einem anderen kollidieren kann,
     * ohne dass die Kollision erkannt wird.<br>
     * Wird vervendet, um zu verhindern, dass direkt nach einer Kollisions-korrektur durch Rundungsfehler erneut eine
     * Kollision festgestellt wird.
     */
    private static final double SAFETY_ADDITIONAL_DISTANCE = 0.1;

    /**
     * Erzeugt eine neue Instanz der jeweiligen Spielphysik der erbenden Klasse.
     */
    public GamePhysic() {

    }

    /**
     * Gibt die Strecke in Metern an, um die sich zwei kollidierende Objekte überschneiden.
     * 
     * @param object1
     *            Das Objekt1.
     * @param object2
     *            Das Objekt2.
     * @return Die Überschneidung der beiden Objekte.
     */
    protected static double getOverlap(RadialGameObject object1, RadialGameObject object2) {

        Vector2D v = new Vector2D(object1.getPosition(), object2.getPosition());
        double distance = v.getLength();

        double overlap = (object1.getDiameter() / 2d) + (object2.getDiameter() / 2d) - distance;
        if (overlap < 0)
            overlap = 0;

        return overlap;
    }

    /**
     * Ermittelt, ob sich zwei kreisförmige Spielobjekte überschneiden.<br>
     * Achtung: verwendet den Toleranzwert {@link #SAFETY_ADDITIONAL_DISTANCE}.
     * 
     * @param circle1Position
     *            Die Position des ersten Kollisionsobjektes.
     * @param circle1Diameter
     *            Der Durchmesser des ersten Kollisionsobjektes.
     * @param circle2Position
     *            Die Position des zweiten Kollisionsobjekts.
     * @param circle2Diameter
     *            Der Durchmesser des zweiten Kollisionsobjektes.
     * @return True, wenn die beiden Objekte miteinander kollidieren, ansonsten false.
     */
    protected static boolean doCirclesCollide(Position circle1Position, double circle1Diameter,
            Position circle2Position, double circle2Diameter) {

        Vector2D v = new Vector2D(circle1Position, circle2Position);
        double distance = v.getLength();
        return (distance < ((circle1Diameter / 2d) + (circle2Diameter / 2d) - GamePhysic.SAFETY_ADDITIONAL_DISTANCE));
    }

    /**
     * Ermittelt, ob sich zwei {@link RadialGameObject}s überschneiden.
     * 
     * @param object1
     *            Das erste Kollisionsobjekt.
     * @param object2
     *            Das zweite Kollisionsobjekt.
     * @return True, wenn sich die zwei übergebenen Objekte überschneiden, ansonsten false.
     */
    protected static boolean doObjectsCollide(RadialGameObject object1, RadialGameObject object2) {
        return GamePhysic.doCirclesCollide(object1.getPosition(), object1.getDiameter(), object2.getPosition(),
                object2.getDiameter());
    }

    /**
     * Ermittelt, ob ein Kreis mit einer Box (=Rechteck) kollidiert.<br>
     * Achtung: verwendet den Toleranzwert {@link #SAFETY_ADDITIONAL_DISTANCE}.
     * 
     * @param circlePosition Die Position des Kreises.
     * @param circleDiameter Der Durchmesser des Kreises.
     * @param topLeftBoxPosition Die Position der linken, oberen Ecke der Box.
     * @param bottomRightBoxPosition Die Position der rechten, oberen Ecke der Box.
     * @return Die Information, mit welchen Seiten der Box der Kreis kollidiert.
     */
    protected static BoundSides getCircleBoxCollisionState(Position circlePosition, double circleDiameter, Position topLeftBoxPosition,
            Position bottomRightBoxPosition) {
        double radius = 0.5d * circleDiameter;
        boolean topSide = false;
        boolean bottomSide = false;
        boolean leftSide = false;
        boolean rightSide = false;

        // linke Seite
        if (circlePosition.getX() < topLeftBoxPosition.getX() + radius
                - GamePhysic.SAFETY_ADDITIONAL_DISTANCE)
            leftSide = true;

        // rechte Seite
        if (circlePosition.getX() > (bottomRightBoxPosition.getX() - radius + GamePhysic.SAFETY_ADDITIONAL_DISTANCE))
            rightSide = true;

        // obere Seite
        if (circlePosition.getY() < (topLeftBoxPosition.getY() + radius)
                - GamePhysic.SAFETY_ADDITIONAL_DISTANCE)
            topSide = true;

        // untere Seite
        if (circlePosition.getY() > (bottomRightBoxPosition.getY() - radius + GamePhysic.SAFETY_ADDITIONAL_DISTANCE))
            bottomSide = true;

        return new BoundSides(leftSide, rightSide, topSide, bottomSide);
    }
    
    
    /**
     * Ermittelt, ob ein Spielobjekt mit einer Box (=Rechteck) kollidiert.<br>
     * Achtung: verwendet den Toleranzwert {@link #SAFETY_ADDITIONAL_DISTANCE}.
     * 
     * @param object
     *            Das Spielobjekt.
     * @param topLeftBoxPosition
     *            Die Position der linken, oberen Ecke der Box.
     * @param bottomRightBoxPosition
     *            Die Position der rechten, unteren Ecke der Box.
     * @return Die Information, mit welchen Seiten der Box das Objekt kollidiert.
     */
    protected static BoundSides getObjectBoxCollisionState(RadialGameObject object, Position topLeftBoxPosition,
            Position bottomRightBoxPosition) {
    	return GamePhysic.getCircleBoxCollisionState(object.getPosition(), object.getDiameter(), topLeftBoxPosition, bottomRightBoxPosition);
    }

    /**
     * Gibt die Kollisionsrichtung(/Richtung des Zentralvektors) und die Richtung des dazugehörigen Tangentialvektors
     * zweier Spielobjekte zurück.<br>
     * Der Kollisionsrichtung ist die Richtung des Verbindungsvektors der beiden Mittelpunkte der Objekte.<br>
     * Die dazugehörige Richtung des Tangentialvektors ist ein Vektor beliebiger Länge, der auf der Kollisionsrichtung
     * senkrecht steht.<br>
     * Die Methode garantiert, das die beiden zurückgegebenen Vektoren eine Länge > 0 haben. Deshalb ist es vorteilhaft,
     * einen zusätzlichen Vektor (optionalDefaultCollisionDirection) mit anzugeben, der als alternative
     * Kollisionsrichtung dient, wenn die beiden übergebenen Positionen gleich sind.<br>
     * Wurde diese optionalDefaultCollisionDirection nicht angegeben (null-gesetzt) oder besitzt die Länge 0, so wird
     * ein Vektor verwendet, der in irgendeine Richtung zeigt, aber die Länge > 0 hat.
     * 
     * @param position1
     *            Die Position des ersten Objekts.
     * @param position2
     *            Die Position des zweiten Objekts.
     * @param optionalDefaultCollisionDirection
     *            Die optionale (kann null sein), alternative Kollisionsrichtung, die verwendet wird, wenn die beiden
     *            übergebenen Positionen gleich sind.
     * @return Die Kollisionsrichtung mit Länge > 0 (als .vector1) und die Richtung des dazugehörigen Tangentialvektors
     *         mit Länge > 0 (als .vector2).
     */
    protected static Vectors getCollisionAndTangentDirections(Position position1, Position position2,
            Vector2D optionalDefaultCollisionDirection) {

        Vector2D collisionDirection = new Vector2D(position1, position2);

        // hat der so eben bestimmte Kollisionsvektor die Länge 0, dann verwende die übergebene
        // optionalDefaultCollisionDirection, wenn diese existiert und deren Länge nicht 0 ist, ansonsten verwende einen
        // fixen Vektor mit der Länge > 0.
        if (collisionDirection.getLength() == 0) {
            if ((optionalDefaultCollisionDirection != null) && (optionalDefaultCollisionDirection.getLength() != 0))
                collisionDirection = optionalDefaultCollisionDirection;
            else
                collisionDirection = new Vector2D(0.5, 0.5);
        }

        return new Vectors(collisionDirection, collisionDirection.getLeftSideNormalVector());
    }

    /**
     * Das selbe Prinzip, wie in der Funktion {@link #getCollisionAndTangentDirections(Position, Position, Vector2D)},
     * nur dass diese Methode auf die Behandlung von kollidierenden Spielobjekten zugeschnitten ist, d.h. sie erwartet
     * keine optionale, alternative Kollisionsrichtung, sondern berechnet diesen intern durch die Verwendung der
     * movementDirections der beiden Objekte.<br>
     * Sollte dadurch keine brauchbare Kollisionsrichtung entstehen, wird der Vektor zurückgegeben, der in der Methode
     * {@link #getCollisionAndTangentDirections(Position, Position, Vector2D)} festgelegt wurde.
     * 
     * @param object1
     *            Die Position des ersten Objekts.
     * @param object2
     *            Die Position des zweiten Objekts.
     * @return Die Kollisionsrichtung mit Länge > 0 (als .vector1) und die Richtung des dazugehörigen Tangentialvektors
     *         mit Länge > 0 (als .vector2).
     */
    protected static Vectors getCollisionAndTangentDirectionsForCollisions(RadialGameObject object1,
            RadialGameObject object2) {

        // ermittle eine defaultCollisionDirection, die durch die movementDirections der übergebenen Objekte bestimmt
        // wird
        Vector2D movementDirection1 = new Vector2D(0, 0);
        Vector2D movementDirection2 = new Vector2D(0, 0);
        if (object1 instanceof MoveableRadialGameObject)
            movementDirection1 = ((MoveableRadialGameObject) object1).getMovementDirection();
        if (object2 instanceof MoveableRadialGameObject)
            movementDirection2 = ((MoveableRadialGameObject) object2).getMovementDirection();

        Position position1 = movementDirection1.getAppliedPosition(object1.getPosition());
        Position position2 = movementDirection2.getAppliedPosition(object2.getPosition());

        Vector2D defaultCollisionDirection = new Vector2D(position1, position2);

        return GamePhysic.getCollisionAndTangentDirections(object1.getPosition(), object2.getPosition(),
                defaultCollisionDirection);
    }

    /**
     * Ermittelt den neuen Bewegungsvektor, der entsteht, wenn von dem übergebenen Bewegungsvektor der übergebene, fixe
     * Wert von seiner Geschwindigkeit (=Länge) abgezogen wird.
     * 
     * @param movementDirection
     *            Der Bewegungsvektor, von dem der Geschwindigkeitsbetrag abgezogen werden soll.
     * @param looseSpeedValue
     *            Der Geschwindigkeitsbetrag, der abgezogen werden soll.
     * @return Der Bewegungsvektor, mit dem verlorenen Geschwindigeitsbetrag.
     */
    protected static Vector2D looseMovementDirectionSpeedValue(Vector2D movementDirection, double looseSpeedValue) {
        double newSpeed = movementDirection.getLength() - looseSpeedValue;

        if (newSpeed <= 0)
            return new Vector2D(0, 0);
        else
            return movementDirection.getNewLengthVector(newSpeed);
    }

    /**
     * Ermittelt den neuen Bewegungsvektor, der entsteht, wenn von dem übergebenen Bewegungsvektor der übergebene,
     * prozentuale Anteil seiner Geschwindigkeit (=Länge) abgezogen wird.
     * 
     * @param movementDirection
     *            Der Bewegungsvektor, von dem der Geschwindigkeitsbetrag abgezogen werden soll.
     * @param looseSpeedPercent
     *            Der prozentuale Anteil der Bewegungsgeschwindigeit, der abgezogen werden soll.
     * @return Der Bewegungsvektor, mit dem verlorenen Geschwindigeitsbetrag.
     */
    protected static Vector2D looseMovementDirectionSpeedPercent(Vector2D movementDirection, double looseSpeedPercent) {
        double looseSpeedValue = movementDirection.getLength() * (looseSpeedPercent / 100d);

        return GamePhysic.looseMovementDirectionSpeedValue(movementDirection, looseSpeedValue);
    }

    /**
     * Konvertiert eine Geschwindigkeit in Metern pro Sekunde in die Geschwindigkeit der Einheit Meter pro Tick.
     * 
     * @param velocity
     *            Die Geschwindigkeit in Metern/Sekunde.
     * @return Die Geschwindigkeit in Metern/Tick.
     */
    public static double convertVelocity(double velocity) {
        return (velocity / PlaybackHandler.TICKS_PER_SECOND);
    }

    /**
     * Konvertiert eine Beschleunigung/Abbremsung in Metern pro Sekunde^2 in die Beschleunigung/Abbremsung der Einheit
     * Meter pro Tick^2.
     * 
     * @param accelerationOrDeceleration
     *            Die Beschleunigung/Abbremsung in Metern/Sekunde^2.
     * @return ie Beschleunigung/Abbremsung in Metern/Tick^2.
     */
    public static double convertAccelerationOrDeceleration(double accelerationOrDeceleration) {
        return (accelerationOrDeceleration / Math.pow(PlaybackHandler.TICKS_PER_SECOND, 2));
    }

    /**
     * Die Methode, die von jeder geerbten Physik-Klasse implementiert werden muss.<br>
     * Darin befinden sich die jeweiligen physikalischen Berechnungen. Die Methode wendet die Änderungen direkt auf die
     * Spielobjekte an.
     * 
     * @param simulationState
     *            Der aktuelle Simulationszustand, in welchem sich die Simulation befindet. <br>
     *            Die Physik reagiert darauf ggf. entsprechend, um z.B. einen Mindestabstand zum Ball bei einem
     *            Freistoß, nicht jedoch während des normalen Spielgeschehens, einzuhalten.
     */
    public abstract void calculate(SoSiTickInformation tickInformationOfTeamA, TickEvent simulationState);
}
