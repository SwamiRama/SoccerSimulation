package SoSi.Model.Calculation;

import SoSi.Model.SoSiPosition;
import sep.football.Position;

/**
 * Beinhaltet die x - und y - Komponenten eines zweidimensionalen Vektors, sowie Berechnungsmethoden für
 * zweidimensionale Vektorarithmetik.<br>
 * Diese Klasse dient einerseits für die Modellierung eines zweidimensionalen Vektors, der z.B in den
 * {@link MoveableRadialGameObject}s als Bewegungsvektor gebraucht wird.<br>
 * Andererseits beinhaltet die Klasse Methoden zur Manipulation eines Vektors bzw. zur Berechnung arithmetischer
 * Funktionen, die für die physikalischen Berechnungen, wie der Kollisionsabfrage notwendig sind.
 */
public class Vector2D {

    /**
     * Die x-Komponente des Vektors.
     */
    private final double x;

    /**
     * Die y-Komponente des Vektors.
     */
    private final double y;

    /**
     * Erzeugt einen neuen Vektor durch das Angeben seiner zwei Komponenten x und y.
     * 
     * @param x
     *            Die x-Komponente des Vektors.
     * @param y
     *            Die y-Komponente des Vektors.
     */
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Erzeugt einen neuen Vektor durch das Angeben zweier Positionen, zwischen denen der Vektor aufgespannt wird.<br>
     * Der Vektor reicht dabei von fromPosition bis toPosition.
     * 
     * @param fromPosition
     *            Die Position, von der der Vektor aus aufgespannt wird.
     * @param toPosition
     *            Die Position, zu der der Vektor aufgespannt wird.
     */
    public Vector2D(Position fromPosition, Position toPosition) {
        if ((fromPosition == null) || (toPosition == null))
            throw new IllegalArgumentException("At least one parameter is null.");

        this.x = toPosition.getX() - fromPosition.getX();
        this.y = toPosition.getY() - fromPosition.getY();
    }

    /**
     * Gibt die x-Komponente des Vektors zurück.
     * 
     * @return Die x-Komponente des Vektors.
     */
    public double getX() {
        return this.x;
    }

    /**
     * Gibt die y-Komponente des Vektors zurück.
     * 
     * @return Die y-Komponente des Vektors.
     */
    public double getY() {
        return this.y;
    }

    /**
     * Gibt die neue Position zurück, die sich ergibt, wenn der Vektor an die übergebene Position angetragen wird.
     * 
     * @param position
     *            Die Position, an die der Vektor angetragen werden soll.
     * @return Die Position, die durch das Antragen des Vektors entstanden ist.
     */
    public Position getAppliedPosition(Position position) {
        if (position == null)
            throw new IllegalArgumentException("The parameter is null.");

        return new SoSiPosition(this.x + position.getX(), this.y + position.getY());
    }

    /**
     * Gibt die Länge des Vektors zurück.
     * 
     * @return Die Länge des Vektors.
     */
    public double getLength() {
        return Math.sqrt(Math.pow(this.getX(), 2) + Math.pow(this.getY(), 2));
    }

    /**
     * Gibt den Einheitsvektor eines Vektors zurück.<br>
     * Der Einheitsvektor besitzt die selbe Richtung wie der Vektors, allerdings hat seine Länge den Wert 1.<br>
     * Ein Sonderfall tritt ein, wenn der Vektor die Länge 0 besitzt. Dann wird der Nullvektor zurückgegeben.
     * 
     * @return Der Einheitsvektor des Vektors mit einer Vektorlänge > 0.
     * @throws ArithmeticException
     *             Die Exception, wenn die Vektorlänge den Wert 0 besitzt.
     */
    public Vector2D getUnitVector() {
        double vectorLength = this.getLength();

        if (vectorLength == 0)
            throw new ArithmeticException("Vector-length is 0.");

        return new Vector2D(this.getX() / vectorLength, this.getY() / vectorLength);
    }

    /**
     * Gibt einen Vektor zurück, der durch das Multiplizieren des Vektors mit dem angegebenen Skalar entsteht.<br>
     * 
     * @param scalar
     *            Der Skalar-wert, der mit dem Vektor multipliziert wird.
     * @return Der neue Vektor, der durch die Skalarmultiplikation entstanden ist.
     */
    public Vector2D getScalarMultipliedVector(double scalar) {
        return new Vector2D(this.getX() * scalar, this.getY() * scalar);
    }

    /**
     * Gibt einen Vektor mit derselben Richtung des Vektors und der angegebenen Länge zurück.<br>
     * Ein Sonderfall tritt ein, wenn der Vektor die Länge 0 besitzt. Dann wird der Nullvektor zurückgegeben.
     * 
     * @param newLength
     *            Die neue Länge des Vektors, die gesetzt werden soll.
     * @return Der Vektor mit der gesetzten Länge und derselben Richtung wie der Vektor.
     */
    public Vector2D getNewLengthVector(double newLength) {
        return this.getUnitVector().getScalarMultipliedVector(newLength);
    }

    /**
     * Gibt den Vektor zurück, der entsteht, wenn der Vektor horizontal (an der y-Achse) gespiegelt wird.
     * 
     * @return Der Vektor, der aus der horizontalen Spiegelung entstanden ist.
     */
    public Vector2D getHorizontalMirroredVector() {
        return new Vector2D(-this.getX(), this.getY());
    }

    /**
     * Gibt den Vektor zurück, der entsteht, wenn der Vektor vertikal (an der x-Achse) gespiegelt wird.
     * 
     * @return Der Vektor, der aus der vertikalen Spiegelung entstanden ist.
     */
    public Vector2D getVerticalMirroredVector() {
        return new Vector2D(this.getX(), -this.getY());
    }

    /**
     * Gibt einen Vektor mit umgekehrter Richtung und selber Länge des Vektor zurück.
     * 
     * @return Der Vektor mit umgekehrter Richtung.
     */
    public Vector2D getInvertedVector() {
        return new Vector2D(-this.getX(), -this.getY());
    }

    /**
     * Gibt den Normalvektor des Vektors zurück, der gegenüber dem Vektor um 90° nach links gedreht ist.
     * 
     * @return Der linksgerichtete Normalvektor.
     */
    public Vector2D getLeftSideNormalVector() {
        return new Vector2D(this.getY(), -this.getX());
    }

    /**
     * Gibt den Normalvektor des Vektors zurück, der gegenüber dem Vektor um 90° nach rechts gedreht ist.
     * 
     * @return Der rechtsgerichtete Normalvektor.
     */
    public Vector2D getRightSideNormalVector() {
        return new Vector2D(-this.getY(), this.getX());
    }

    /**
     * Gibt einen Vektor zurück, der um den angegebenen Winkel in der angegebenen Richtung gedreht wurde.
     * 
     * @param angle
     *            Der Drehwinkel des Vektors.
     * @param toLeft
     *            Die Richtung, in die gedreht werden soll.
     * @return Der gedrehte Vektor mit der selben Richtung.
     */
    public Vector2D getRotatedVector(double angle, boolean toLeft) {

        if (this.getLength() == 0)
            return new Vector2D(0, 0);

        if (angle < 0)
            return this.getRotatedVector(-angle, !toLeft);

        angle = angle % 360;

        if (angle > 90)
            return this.getRotatedVector(90, toLeft).getRotatedVector(angle - 90, toLeft);

        // rechne hier nur mit 0 <= angle <= 90
        if (angle == 90) {
            if (toLeft)
                return this.getLeftSideNormalVector().getNewLengthVector(this.getLength());
            else
                return this.getRightSideNormalVector().getNewLengthVector(this.getLength());
        } else {
            double s = Math.sin(Math.PI * (angle / 180d)) * this.getLength();
            double h = Math.cos(Math.PI * (angle / 180d)) * this.getLength();

            if (toLeft)
                return Vector2D.addVectors(this.getNewLengthVector(h), this.getLeftSideNormalVector()
                        .getNewLengthVector(s));
            else
                return Vector2D.addVectors(this.getNewLengthVector(h), this.getRightSideNormalVector()
                        .getNewLengthVector(s));
        }
    }

    /**
     * Gibt die beiden Vektor-Komponenten x und y als Position zurück.
     * 
     * @return Die x- und y-Komponenten des Vektors als Position.
     */
    public Position convertToPosition() {
        return new SoSiPosition(this.x, this.y);
    }

    /**
     * Gibt den Vector zurück, der durch die Addition der zwei übergebenen Vektoren entsteht.
     * 
     * @param vector1
     *            Einer der zu addierenden Vektoren.
     * @param vector2
     *            Der andere zu addierende Vektor.
     * @return Der Vektor, der durch die Addition der zwei übergebenen Vektoren entsteht.
     */
    public static Vector2D addVectors(Vector2D vector1, Vector2D vector2) {
        if ((vector1 == null) || (vector2 == null))
            throw new IllegalArgumentException("At least one parameter is null.");

        return new Vector2D(vector1.getX() + vector2.getX(), vector1.getY() + vector2.getY());
    }

    /**
     * Gibt den Vektor zurück, der durch die Subtraktion von zwei Vektoren entsteht.
     * 
     * @param vector
     *            Der Vektor, von dem subtrahiert werden soll.
     * @param subtractVector
     *            Der Vektor, der subtrahiert wird.
     * @return Der Vektor, der durch die Subtraktion der zwei übergebenen Vektoren entsteht.
     */
    public static Vector2D subtractVectors(Vector2D vector, Vector2D subtractVector) {
        if ((vector == null) || (subtractVector == null))
            throw new IllegalArgumentException("At least one parameter is null.");

        return new Vector2D(vector.getX() - subtractVector.getX(), vector.getY() - subtractVector.getY());
    }

    /**
     * Gibt das Skalarprodukt der zwei übergebenen Vektoren zurück.<br>
     * <ul>
     * <li>Das Skalarprodukt ist > 0, wenn der kleinere Winkel ([0, 180]) zwischen den Vektoren ein spitzer Winkel ist.</li>
     * <li>Das Skalarprodukt ist = 0, wenn die zwei Vektoren aufeinander senkrecht stehen.</li>
     * <li>Das Skalarprodukt ist < 0, wenn kleinere Winkel ([0, 180]) zwischen den Vektoren ein stumpfer Winkel ist.</li>
     * </ul>
     * 
     * @param vector1
     *            Ein Vektor.
     * @param vector2
     *            Der andere Vektor.
     * @return Das Skalarprodukt der beiden übergebenen Vektoren.
     */
    public static double getScalarProduct(Vector2D vector1, Vector2D vector2) {
        if ((vector1 == null) || (vector2 == null))
            throw new IllegalArgumentException("At least one parameter is null.");

        return (vector1.getX() * vector2.getX()) + (vector1.getY() * vector2.getY());
    }

    /**
     * Gibt den projectVector zurück, der durch einen 90° Winkel auf den targetVector projiziert wurde.
     * 
     * @param projectVector
     *            Der zu projizierende Vektor
     * @param targetVector
     *            Der Vektor, auf den projiziert wird.
     * @return Der Vektor, der durch die Projektion des projectVectors in den targetVector entsteht.
     * @throws ArithmeticException
     *             Die Exception, wenn der targetVector die Länge 0 besitzt.
     */
    public static Vector2D rightAngleProjectionOfVectorInVector(Vector2D projectVector, Vector2D targetVector) {
        if (projectVector == null || targetVector == null)
            throw new IllegalArgumentException("At least one parameter is null.");

        Vector2D targetUnitVector = targetVector.getUnitVector();
        return targetUnitVector.getScalarMultipliedVector(Vector2D.getScalarProduct(projectVector, targetUnitVector));
    }

    /**
     * Gibt den winkelhalbierenden Vektor mit der Länge 1 zwischen den beiden übergebenen Vektoren vector1 und vector2
     * zurück. Im Falle eines 180° Winkels, wird der winkelhalbierende Vektor genommen, der in dem, durch vector1 und
     * vector2 aufgespannten Winkel liegt, in den der defaultDirectionVector zeigt.
     * 
     * @param vector1
     *            Der Vektor 1, der zusammen mit Vektor 2 den Winkel aufspannt, zu dem der winkelhalbierende Vektor
     *            erstellt werden soll.
     * @param vector2
     *            Der Vektor 2, der zusammen mit Vektor 2 den Winkel aufspannt, zu dem der winkelhalbierende Vektor
     *            erstellt werden soll.
     * @param defaultDirectionVector
     *            Bestimmt im Falle eines 180° Winkels zwischen den Vektoren vector1 und vector2 den Winkel, zu dem der
     *            winkelhalbierende Vektor ermittelt werden soll.
     * @return Der Winkelhalbierende Vektor von vector1 und vector2.
     */
    public static Vector2D getAngleUnitBisector(Vector2D vector1, Vector2D vector2, Vector2D defaultDirectionVector) {
        if ((vector1 == null) || (vector2 == null) || (defaultDirectionVector == null))
            throw new IllegalArgumentException("At least one parameter is null.");

        Vector2D unitVector1 = vector1.getUnitVector();
        Vector2D unitVector2 = vector2.getUnitVector();
        Vector2D addedVectors = Vector2D.addVectors(unitVector1, unitVector2);

        if (addedVectors.getLength() == 0) {
            Vector2D v1 = unitVector1.getLeftSideNormalVector();
            Vector2D v2 = unitVector1.getRightSideNormalVector();

            if (Vector2D.getScalarProduct(v1, defaultDirectionVector) >= 0)
                return v1;
            else
                return v2;
        } else
            return addedVectors.getUnitVector();
    }

    /**
     * Dient zur Berechnung des Winkels zwischen zwei Vektoren.<br>
     * Dabei wird stets der kleinere Winkel ([0, 180]) zwischen den Vektoren zurückgegeben.<br>
     * Ein Sonderfall tritt ein, wenn mindestens einer der Vektoren die Länge 0 besitzt. Dann wird eine Exception
     * zurückgegeben.
     * 
     * @param vector1
     *            Der erste Vektor.
     * @param vector2
     *            Der zweite Vektor.
     * @return Gibt einen Winkel zwischen 0° und 180° zurück, der zwischen den beiden angegeben Vektoren liegt.
     * @throws ArithmeticException
     *             Die Exception, wenn einer der beiden (bzw. beide) Vektoren die Länge 0 hat (haben).
     */
    public static double getSmallestAngleBetweenVectors(Vector2D vector1, Vector2D vector2) {

        if ((vector1 == null) || (vector2 == null))
            throw new IllegalArgumentException("At least one parameter is null.");
        if ((vector1.getLength() == 0) || (vector2.getLength() == 0))
            throw new IllegalArgumentException("At least one parameter-vector-length is 0.");

        double value = Vector2D.getScalarProduct(vector1, vector2) / (vector1.getLength() * vector2.getLength());

        // durch Rundungsfehler kann value > 1 werden -> acos undefiniert!! (Bsp: (-2, 5), (-2, -5)), daher überprüfen
        if (value > 1)
            value = 1;
        if (value < -1)
            value = -1;

        return Math.acos(value) * (180d / Math.PI);
    }

    /**
     * Überprüft, ob der übergebene Vektor im kleineren Winkel ([0, 180] Grad) zwischen den beiden übergebenen Vektoren
     * liegt.
     * 
     * @param vector
     *            Der Vektor, dessen Position geprüft werden soll.
     * @param vector1
     *            Vektor 1, der zusammen mit Vektor 2 den Winkel bildet, in dem der Vektor sich befinden kann.
     * @param vector2
     *            Vektor 2, der zusammen mit Vektor 1 den Winkel bildet, in dem der Vektor sich befinden kann.
     * @return True, wenn sich der übergebene Vektor im kleineren Winkel ([0, 180] Grad) zwischen Vektor 1 und Vektor 2
     *         liegt.
     */
    public static boolean isVectorBetweenSmallestAngleOfVectors(Vector2D vector, Vector2D vector1, Vector2D vector2) {

        if ((vector == null) || (vector1 == null) || (vector2 == null))
            throw new IllegalArgumentException("At least one parameter is null.");

        double angleBetweenVector1AndVector2 = Vector2D.getSmallestAngleBetweenVectors(vector1, vector2);

        if (angleBetweenVector1AndVector2 == 180)
            return true;
        else {

            double angleToVector1 = Vector2D.getSmallestAngleBetweenVectors(vector, vector1);
            double angleToVector2 = Vector2D.getSmallestAngleBetweenVectors(vector, vector2);
            Vector2D angleUnitBisector = Vector2D.getAngleUnitBisector(vector1, vector2, vector);

            // System.out.println("angleToVector1: " + angleToVector1 + " angleToVector2: " + angleToVector2 +
            // " angleBetweenVector1AndVector2: " + angleBetweenVector1AndVector2);

            // Toleranz
            final double TOLERANCE = 0.0001d;
            return ((angleToVector1 <= angleBetweenVector1AndVector2 + TOLERANCE)
                    && (angleToVector2 <= angleBetweenVector1AndVector2 + TOLERANCE) && (Vector2D.getScalarProduct(
                    angleUnitBisector, vector) >= 0));
        }
    }

    /**
     * Prüft, ob der übergebene Vektor zwischen den beiden übergebenen Vektoren liegt.<br>
     * Der übergebene directionVector muss dabei in den, von Vektor1 und Vektor2 aufgespannten Winkel zeigen, und
     * entscheidet daher im Falle eines 180° Winkels zwischen Vektor1 und Vektor2 für welchen 180° Winkel der Vektor
     * überprüft werden soll.
     * 
     * @param vector
     *            Der Vektor, dessen Position geprüft werden soll.
     * @param vector1
     *            Vektor 1, der zusammen mit Vektor 2 den Winkel bildet, in dem der Vektor sich befinden kann.
     * @param vector2
     *            ektor 2, der zusammen mit Vektor 1 den Winkel bildet, in dem der Vektor sich befinden kann.
     * @param directionVector
     *            Der Vektor, der in den, durch Vektor1 und Vektor2 aufgespannten Winkel zeigt (Darf nicht die selbe
     *            Richtung haben wie vector1 oder vector2).
     * @return True, wenn sich der übergebene Vektor zwischen Vektor 1 und Vektor 2 liegt.
     */
    public static boolean isVectorBeweenVectors(Vector2D vector, Vector2D vector1, Vector2D vector2,
            Vector2D directionVector) {

        if ((vector == null) || (vector1 == null) || (vector2 == null) || (directionVector == null))
            throw new IllegalArgumentException("At least one parameter is null.");

        double angleBetweenVector1AndVector2 = Vector2D.getSmallestAngleBetweenVectors(vector1, vector2);

        // Statt mit 180 wird hier mit einer Toleranz gerechnet
        final double TOLERANCE = 0.0001d;
        if (angleBetweenVector1AndVector2 >= (180 - TOLERANCE))
            return ((Vector2D.isVectorBeweenVectors(vector, directionVector, vector1,
                    Vector2D.addVectors(directionVector, vector1))) || (Vector2D.isVectorBeweenVectors(vector,
                    directionVector, vector2, Vector2D.addVectors(directionVector, vector2))));
        else {
            boolean b = Vector2D.isVectorBetweenSmallestAngleOfVectors(vector, vector1, vector2);
            if (Vector2D.isVectorBetweenSmallestAngleOfVectors(directionVector, vector1, vector2))
                return b;
            else
                return (!b);
        }
    }

    @Override
    public String toString() {

        String str = "(" + this.getX() + " | " + this.getY() + ") Length: " + this.getLength();
        return str;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Vector2D) ? (((Vector2D) obj).getX() == this.getX())
                && (((Vector2D) obj).getY() == this.getY()) : false;
    }

}
