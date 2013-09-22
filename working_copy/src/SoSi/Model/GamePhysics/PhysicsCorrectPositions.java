package SoSi.Model.GamePhysics;

import java.util.LinkedList;
import java.util.List;
import sep.football.Position;
import SoSi.Model.SoSiPosition;
import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.SoSiTickInformation;
import SoSi.Model.Calculation.Team;
import SoSi.Model.Calculation.Vector2D;
import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GameObjects.GoalGameObject;
import SoSi.Model.GameObjects.MoveableRadialGameObject;
import SoSi.Model.GameObjects.PlayerGameObject;
import SoSi.Model.GameObjects.RadialGameObject;

/**
 * Diese Klasse ist zuständig für das Korrigieren der Positionen der Spielobjekte.<br>
 * Dabei werden die Positionen aller Spieler neu gesetzt, so dass sich je zwei Objekte nicht überschneiden.<br>
 * Dadurch wird sichergestellt, dass in der Darstellung zu jedem Zeitpunkt, jedes Spielobjekt sichtbar ist.<br>
 * Benötigt werden dafür ebenfalls die Position des Balles und aller Torpfosten, da mit ihnen ebenfalls keine
 * Überschneidung auftreten darf.<br>
 * Außerdem werden die Positionen der Spieler auf einen bestimmten Bereich außerhalb des Spielfeldes beschränkt, so dass
 * Spieler nicht aus dem dargestellten Bereich laufen können.<br>
 * Zusätzlich wird darauf geachtet, dass bei allen Stößen, wie (Anstoß, Freistoß) die fest definierten Abstände der
 * gegnerischen Spieler zur Ballposition eingehalten werden. Ist dies nicht der Fall, werden die Positionen entsprechend
 * korrigiert.<br>
 * Beim Korrigieren der Positionen wird darauf geachtet, dass sich dadurch keine weiteren Überschneidungen ergeben.<br>
 * <b>Eine genauere Beschreibung, sowie die einzelnen Berechnungsschritte befinden sich in dem dazugehörigen Dokument im
 * Implementierungsbericht.</b>
 * 
 */
public class PhysicsCorrectPositions extends GamePhysic {

    /**
     * Die minimale Distanz zur Anstoßposition, die die Spieler bei einem Anstoß einhalten müssen.
     */
    public static double MINIMUM_DISTANCE_TO_KICK_OFF_POSITION = 5.42d;

    /**
     * Die minimale Distanz zur Freistoßposition, die die Spieler bei einem Anstoß einhalten müssen.
     */
    public static double MINIMUM_DISTANCE_TO_FREE_KICK_POSITION = 5.42d;

    /**
     * Der maximal erlaubte Schleifendurchlauf in der {@link #calculate(SoSiTickInformation, TickEvent)}-Methode.
     */
    private static final int MAX_LOOP_COUNT = 30;

    /**
     * Enthält neben dem Spielobjekt, zusätzliche Richtungen, zwischen denen das Spielobjekt Kollisionen mit anderen
     * Objekten korrigieren kann.
     */
    private class ExtendedRadialGameObject {

        /**
         * Referenz auf das Spielobjekt
         */
        public RadialGameObject object;

        /**
         * Eigens definierter Name (für die Methode {@link #toString()}).
         */
        public String name;

        /**
         * Gibt an, ob das Objekt keine Kollisionen mit anderen Objekten korrigieren kann.
         */
        boolean notCorrectable;

        /**
         * Gibt zusammen mit correctionDirection2 den Winkel [0, 180] an, zwischen dem das Objekt eine Kollision mit
         * einem anderen Objekt korrigieren kan.
         */
        private Vector2D correctionDirection1;

        /**
         * Gibt zusammen mit correctionDirection1 den Winkel [0, 180] an, zwischen dem das Objekt eine Kollision mit
         * einem anderen Objekt korrigieren kan.
         */
        private Vector2D correctionDirection2;

        /**
         * Zeigt zwischen den aufgespannten Winkel von correctionDirection1 und correctionDirection2.
         */
        private Vector2D correctionDirection;

        /**
         * Erzeugt ein neues, erweitertes Spielobjekt.
         * 
         * @param object
         *            Referenz auf das Spielobjekt.
         * @param name
         *            Eigens definierter Name (für die Methode {@link #toString()}).
         * @param notCorrectable
         *            Gibt an, ob das Objekt keine Kollisionen mit anderen Objekten korrigieren kann.
         */
        public ExtendedRadialGameObject(RadialGameObject object, String name, boolean notCorrectable) {
            this.object = object;
            this.name = name;
            this.notCorrectable = notCorrectable;

            this.reset();
        }

        /**
         * Setzt den Zustand des ExtendedRadialGameObjects auf den Zustand zurück, den es im Konstruktor hatte.
         */
        public void reset() {
            this.correctionDirection = null;

            if (this.notCorrectable)
                this.setNotCorrectable();
            else
                this.setTotalCorrectable();
        }

        /**
         * Gibt an, ob das ExtendedRadialGameObject in eine bestimmte Richtung eine Kollision korrigieren kann.
         * 
         * @param vector
         *            Der zu überprüfende Korrektur-Vektor.
         * @return True, wenn das ExtendedRadialGameObject in die angegebene Richtung korrigieren kann.
         */
        public boolean canCorrectInDirection(Vector2D vector) {

            if ((vector == null) || (vector.getLength() == 0))
                throw new IllegalArgumentException("Illegal vector.");

            return ((this.isTotalCorrectable()) || ((this.isLimitedCorrectable()) && (Vector2D.isVectorBeweenVectors(
                    vector, this.correctionDirection1, this.correctionDirection2, this.correctionDirection))));
        }

        /**
         * Beschränkt die beiden Vektoren {@link #correctionDirection1} und {@link #correctionDirection2}, durch die
         * übegebene Kollisionsrichtung.<br>
         * <b>Eine genauere Beschreibung, sowie die einzelnen Berechnungsschritte befinden sich in dem dazugehörigen
         * Dokument im Implementierungsbericht.</b>
         * 
         * @param collisionDirection
         *            Die Kollisionsrichtung, anhand der die correctionDirections eingeschränkt werden.
         */
        public void limitCorrectionDirection(Vector2D collisionDirection) {

            if ((collisionDirection == null) || (collisionDirection.getLength() == 0))
                throw new IllegalArgumentException("invalid collisiondDirection.");

            if (!this.isNotCorrectable()) {

                Vector2D tangentDirection1 = collisionDirection.getLeftSideNormalVector();
                Vector2D tangentDirection2 = collisionDirection.getRightSideNormalVector();

                if (this.isTotalCorrectable()) {

                    this.correctionDirection1 = tangentDirection1;
                    this.correctionDirection2 = tangentDirection2;
                    this.correctionDirection = collisionDirection;

                } else if (this.isLimitedCorrectable()) {

                    boolean isCorrectionDirection1BetweenTangentVector1And2 = Vector2D.isVectorBeweenVectors(
                            this.correctionDirection1, tangentDirection1, tangentDirection2, collisionDirection);
                    boolean isCorrectionDirection2BetweenTangentVector1And2 = Vector2D.isVectorBeweenVectors(
                            this.correctionDirection2, tangentDirection1, tangentDirection2, collisionDirection);

                    if (isCorrectionDirection1BetweenTangentVector1And2
                            && isCorrectionDirection2BetweenTangentVector1And2) {

                        if (Vector2D.getScalarProduct(this.correctionDirection, collisionDirection) < 0)

                            this.setNotCorrectable();
                    } else if ((!isCorrectionDirection1BetweenTangentVector1And2)
                            && (!isCorrectionDirection2BetweenTangentVector1And2)) {

                        this.setNotCorrectable();
                    } else {

                        Vector2D innerCorrectionDistance = (isCorrectionDirection1BetweenTangentVector1And2) ? this.correctionDirection1
                                : this.correctionDirection2;
                        Vector2D outerCorrectionDistance = (isCorrectionDirection1BetweenTangentVector1And2) ? this.correctionDirection2
                                : this.correctionDirection1;

                        if (Vector2D.isVectorBeweenVectors(tangentDirection1, innerCorrectionDistance,
                                outerCorrectionDistance, correctionDirection)) {
                            this.correctionDirection1 = tangentDirection1;
                            this.correctionDirection2 = innerCorrectionDistance;
                        } else {
                            this.correctionDirection1 = tangentDirection2;
                            this.correctionDirection2 = innerCorrectionDistance;
                        }

                        // Achtung: correctionDirection darf nicht die Länge 0 haben (müsste aber in diesem Fall
                        // ausgeschlossen sein)
                        this.correctionDirection = Vector2D.addVectors(this.correctionDirection1,
                                this.correctionDirection2);
                    }
                }
            }
        }

        /**
         * Setzt das ExtendedRadialGameObject total korrekturfähig.
         */
        private void setTotalCorrectable() {
            this.correctionDirection1 = new Vector2D(0, 0);
            this.correctionDirection2 = new Vector2D(0, 0);
        }

        /**
         * Prüft, ob das ExtendedRadialGameObject total korrekturfähig ist.
         * 
         * @return True, wenn das ExtendedRadialGameObject total korrekturfähig ist, sonst false.
         */
        private boolean isTotalCorrectable() {
            return ((this.correctionDirection1 != null) && (this.correctionDirection2 != null)
                    && (this.correctionDirection1.getLength() == 0) && (this.correctionDirection2.getLength() == 0));
        }

        /**
         * Setzt das ExtendedRadialGameObject nicht korrekturfähig.
         */
        private void setNotCorrectable() {
            this.correctionDirection1 = null;
            this.correctionDirection2 = null;
        }

        /**
         * Prüft, ob das ExtendedRadialGameObject nicht korrekturfähig ist.
         * 
         * @return True, wenn das ExtendedRadialGameObject nicht korrekturfähig ist, sonst false.
         */
        private boolean isNotCorrectable() {
            return ((this.correctionDirection1 == null) && (this.correctionDirection2 == null));
        }

        /**
         * Prüft, ob das ExtendedRadialGameObject eingeschränkt korrekturfähig ist.
         * 
         * @return True, wenn das ExtendedRadialGameObject eingeschränkt korrekturfähig ist, sonst false.
         */
        private boolean isLimitedCorrectable() {
            return ((this.correctionDirection1 != null) && (this.correctionDirection2 != null)
                    && (this.correctionDirection1.getLength() != 0) && (this.correctionDirection2.getLength() != 0));
        }

        @Override
        public String toString() {

            String typeStr = "unknown";

            if (this.isNotCorrectable())
                typeStr = "notCorrectable";
            else if (this.isTotalCorrectable())
                typeStr = "totalCorrectable";
            else if (this.isLimitedCorrectable())
                typeStr = "limitedCorrectable";

            String correctionDirection1Str = "null";
            if (this.correctionDirection1 != null)
                correctionDirection1Str = this.correctionDirection1.toString();

            String correctionDirection2Str = "null";
            if (this.correctionDirection2 != null)
                correctionDirection2Str = this.correctionDirection2.toString();

            String correctionDirectionStr = "null";
            if (this.correctionDirection != null)
                correctionDirectionStr = this.correctionDirection.toString();

            String str = this.name + ": " + typeStr + ", correctionDirection1: " + correctionDirection1Str
                    + ", correctionDirection2: " + correctionDirection2Str + ", correctionDirection: "
                    + correctionDirectionStr + "\n";

            return str;
        }
    }

    /**
     * Die Position der linken, oberen Ecke des Spielfelds.
     */
    private Position fieldTopLeftPosition;

    /**
     * Die Position der rechten, unteren Ecke des Spielfelds.
     */
    private Position fieldBottomRightPosition;

    /**
     * Die Position der linken, oberen Ecke des Rechtecks, in das alle Spielobjekte korrigiert werden sollen.
     */
    private Position correctionBoxTopLeftPosition;

    /**
     * Die Position der rechten, unteren Ecke des Rechtecks, in das alle Spielobjekte korrigiert werden sollen.
     */
    private Position correctionBoxBottomRightPosition;

    /**
     * Der Ball.
     */
    private final BallGameObject ball;

    /**
     * Team A.
     */
    private final Team teamA;

    /**
     * Team B.
     */
    private final Team teamB;

    /**
     * Die Liste aller Spielobjekte als ExtendedRadialGameObjects.
     */
    private final List<ExtendedRadialGameObject> extGameObjects;

    /**
     * Erzeugt die neue Physik-Klasse, die für das Korrigieren der Spielerpositionen zuständig ist.
     * 
     * @param teamA
     *            {@link Team}-Instanz des Teams A.
     * @param teamB
     *            {@link Team}-Instanz des Teams B.
     * @param ball
     *            Das Simulationsobjekt Ball vom Typ {@link BallGameObject}.
     * @param goal1
     *            Ein Simulationsobjekt Tor vom Typ {@link GoalGameObject}.
     * @param goal2
     *            Ein Simulationsobjekt Tor vom Typ {@link GoalGameObject}.
     * @param fieldTopLeftPosition
     *            Die Position der linken, oberen Ecke des Spielfelds.
     * @param fieldBottomRightPosition
     *            Die Position der rechten, unteren Ecke des Spielfelds.
     * @param correctionBoxTopLeftPosition
     *            Die Position der linken, oberen Ecke des Rechtecks, in das alle Spielobjekte korrigiert werden sollen.
     * @param correctionBoxBottomRightPosition
     *            Die Position der rechten, unteren Ecke des Rechtecks, in das alle Spielobjekte korrigiert werden
     *            sollen.
     */
    public PhysicsCorrectPositions(Team teamA, Team teamB, BallGameObject ball, GoalGameObject goal1,
            GoalGameObject goal2, Position fieldTopLeftPosition, Position fieldBottomRightPosition,
            Position correctionBoxTopLeftPosition, Position correctionBoxBottomRightPosition) {

        this.teamA = teamA;
        this.teamB = teamB;
        this.ball = ball;

        this.fieldTopLeftPosition = fieldTopLeftPosition;
        this.fieldBottomRightPosition = fieldBottomRightPosition;
        this.correctionBoxTopLeftPosition = correctionBoxTopLeftPosition;
        this.correctionBoxBottomRightPosition = correctionBoxBottomRightPosition;

        this.extGameObjects = new LinkedList<ExtendedRadialGameObject>();

        // Team A
        for (int i = 0; i <= this.teamA.getPlayers().size() - 1; ++i)
            this.extGameObjects.add(new ExtendedRadialGameObject(this.teamA.getPlayers().get(i), "Spieler " + i
                    + " (TeamA)", false));
        // Team B
        for (int i = 0; i <= this.teamA.getPlayers().size() - 1; ++i)
            this.extGameObjects.add(new ExtendedRadialGameObject(this.teamB.getPlayers().get(i), "Spieler " + i
                    + " (TeamB)", false));

        // Der Ball
        this.extGameObjects.add(new ExtendedRadialGameObject(ball, "Ball", false));

        // Die beiden Pfosten des 1. Tores
        this.extGameObjects.add(new ExtendedRadialGameObject(goal1.getPostLeft(), "Torpfosten", true));
        this.extGameObjects.add(new ExtendedRadialGameObject(goal1.getPostRight(), "Torpfosten", true));

        // Die beiden Pfosten des 2. Tores
        this.extGameObjects.add(new ExtendedRadialGameObject(goal2.getPostLeft(), "Torpfosten", true));
        this.extGameObjects.add(new ExtendedRadialGameObject(goal2.getPostRight(), "Torpfosten", true));
    }

    /**
     * Gibt den Mittelpunk des übergebenen Rechtecks zurück.
     * 
     * @param topLeftBoxPosition
     *            Die Position der linken, oberen Ecke des übergebenen Rechtecks.
     * @param bottomRightBoxPosition
     *            Die Position der rechten, unteren Ecke des übergebenen Rechtecks.
     * @return Der Mittelpunkt des Rechtecks.
     */
    private static Position getMiddleBoxPosition(Position topLeftBoxPosition, Position bottomRightBoxPosition) {
        double middleBoxPositionX = topLeftBoxPosition.getX()
                + ((bottomRightBoxPosition.getX() - topLeftBoxPosition.getX()) / 2d);
        double middleBoxPositionY = topLeftBoxPosition.getY()
                + ((bottomRightBoxPosition.getY() - topLeftBoxPosition.getY()) / 2d);
        Position middleBoxPosition = new SoSiPosition(middleBoxPositionX, middleBoxPositionY);

        return middleBoxPosition;
    }

    /**
     * Ermittelt den Abstand zwischen einer Position und einer Linie.
     * 
     * @param position
     *            Die Position
     * @param linePosition
     *            Der Aufpunkt, der auf der Linie liegt.
     * @param lineVector
     *            Der Vector, der die Richtung der Linie beschreibt.
     * @return Der Abstand zwischen der Position und der Linie.
     */
    private static double getDistanceBetweenPositionAndLine(Position position, Position linePosition,
            Vector2D lineVector) {

        Vector2D vector = new Vector2D(linePosition, position);
        if (vector.getLength() == 0)
            return 0;
        else {

            double angle = Vector2D.getSmallestAngleBetweenVectors(lineVector, vector);
            return (Math.sin(Math.PI * (angle / 180d)) * vector.getLength());
        }
    }

    /**
     * Liefert einen horizontalen- und vertikalen Vektor zurück, der auf das Spielobjekt angewandt, dieses in das
     * Rechteck hineinkorrigiert, sollte sich das Spielobjekt außerhalb des Rechtecks befinden oder sich mit ihm
     * überschneiden.<br>
     * Ist das für eine Richtung (horizontal/vertikal) nicht der Fall, ist der entsprechende horizontale-/vertikale
     * Korrektur-Vektor der Nullvektor.
     * 
     * @param object
     *            Das Spielobjekt.
     * @param topLeftBoxPosition
     *            Die Position der linken, oberen Ecke der Box.
     * @param bottomRightBoxPosition
     *            Die Position der rechten, unteren Ecke der Box.
     * @return Der horizontale(.vetor1)- und vertikale(.vector2) Korrektur-Vektor.
     */
    private static Vectors getCorrectionVectorsByBoxCollision(RadialGameObject object, Position topLeftBoxPosition,
            Position bottomRightBoxPosition) {

        Vector2D horizontalCorrectionVector = new Vector2D(0, 0);
        Vector2D verticalCorrectionVector = new Vector2D(0, 0);

        BoundSides boundSides = GamePhysic.getObjectBoxCollisionState(object, topLeftBoxPosition,
                bottomRightBoxPosition);

        if (boundSides.hasSides()) {

            double objectRadius = 0.5d * object.getDiameter();
            Position position = object.getPosition();

            if (boundSides.leftSide) {

                double newPositionX = topLeftBoxPosition.getX() + objectRadius;
                horizontalCorrectionVector = new Vector2D(position, new SoSiPosition(newPositionX, position.getY()));
            }

            if (boundSides.rightSide) {

                double newPositionX = bottomRightBoxPosition.getX() - objectRadius;
                horizontalCorrectionVector = new Vector2D(position, new SoSiPosition(newPositionX, position.getY()));
            }

            if (boundSides.topSide) {

                double newPositionY = topLeftBoxPosition.getY() + objectRadius;
                verticalCorrectionVector = new Vector2D(position, new SoSiPosition(position.getX(), newPositionY));
            }

            if (boundSides.bottomSide) {

                double newPositionY = bottomRightBoxPosition.getY() - objectRadius;
                verticalCorrectionVector = new Vector2D(position, new SoSiPosition(position.getX(), newPositionY));
            }
        }

        return new Vectors(horizontalCorrectionVector, verticalCorrectionVector);
    }

    /**
     * Liefert Vektoren zurück, die für das übergebene Spielobjekt und den übergebenen Kreis den Korrektur-vektor
     * beinhaltet, der auf das Objekt und den Kreis angewandt, die beiden voneinander weg korrigiert, falls sich das
     * Objekt mit dem Kreis überschneidet.<br>
     * (Das Objekt wird entlang der übergebenen objectCorrectionDirection korrigiert und der Kreis entgegengesetzt).<br>
     * Ist das nicht der Fall, werden zwei Nullvektoren zurückgegeben.
     * 
     * 
     * @param object
     *            Das Spielobjekt.
     * @param circlePosition
     *            Die Position des Kreises.
     * @param circleDiameter
     *            Der Durchmesser des Kreises.
     * @param objectCorrectionPercent
     *            Die Prozentangabe [0, 100], um wie viel Prozent das Spielobjekt die Überschneidung mit dem Kreis
     *            korrigieren wird.
     * @param objectCorrectionDirection
     *            Die Richtung, in der das Objekt und entgegen der der Kreis korrigiert wird.
     * @return Die beiden Korrektur-vektoren (.vector1 ist der Korrekturvektor für das Objekt, .vector2 der für den
     *         Kreis).
     */
    private static Vectors getCorrectionVectorsByCircleCollision(RadialGameObject object, Position circlePosition,
            double circleDiameter, double objectCorrectionPercent, Vector2D objectCorrectionDirection) {

        if (objectCorrectionDirection.getLength() == 0)
            throw new IllegalArgumentException("objectCorrectionDirection-length is 0.");

        Vector2D objectCorrectionVector = new Vector2D(0, 0);
        Vector2D circleCorrectionVector = new Vector2D(0, 0);

        if (GamePhysic.doCirclesCollide(object.getPosition(), object.getDiameter(), circlePosition, circleDiameter)) {

            double d = (new Vector2D(object.getPosition(), circlePosition)).getLength();
            double h = PhysicsCorrectPositions.getDistanceBetweenPositionAndLine(object.getPosition(),
                    circlePosition, objectCorrectionDirection);
            double c;
            double e;

            if (h == 0) {
                c = (object.getDiameter() / 2d) + (circleDiameter / 2d);
                e = d;
            } else {
                double s = (object.getDiameter() / 2d) + (circleDiameter / 2d);
                double w = Math.asin(h / s) * (180d / Math.PI);
                double w2 = Math.asin(h / d) * (180d / Math.PI);
                c = Math.cos(Math.PI * (w / 180d)) * s;
                e = Math.cos(Math.PI * (w2 / 180d)) * d;

            }

            double x1 = c - e;
            double x2 = c + e;
            double correctionDistance;

            if (Vector2D
                    .getScalarProduct(new Vector2D(circlePosition, object.getPosition()), objectCorrectionDirection) >= 0) {

                // verwende x1
                correctionDistance = x1;
            } else {

                // verwende x2
                correctionDistance = x2;
            }

            double objectCorrectionDistance = (objectCorrectionPercent / 100d) * correctionDistance;
            double circleCorrectionDistance = correctionDistance - objectCorrectionDistance;

            objectCorrectionVector = objectCorrectionDirection.getNewLengthVector(objectCorrectionDistance);
            circleCorrectionVector = objectCorrectionDirection.getInvertedVector().getNewLengthVector(
                    circleCorrectionDistance);
        }

        return new Vectors(objectCorrectionVector, circleCorrectionVector);
    }

    /**
     * Liefert Vektoren zurück, die für das übergebene Spielobjekt 1 und 2 den Korrektur-vektor beinhaltet, der auf die
     * beiden Objekte angewandt, die beiden voneinander weg korrigiert, falls sich die beiden überschneiden.<br>
     * (Das Spielobjekt 1 wird entlang der übergebenen object1CorrectionDirection korrigiert und das Spielobjekt 2
     * entgegen).<br>
     * Ist das nicht der Fall, werden zwei Nullvektoren zurückgegeben.
     * 
     * @param object1
     *            Das Spielobjekt 1.
     * @param object2
     *            Das Spielobjekt 2.
     * @param object1CorrectionPercent
     *            Die Prozentangabe [0, 100], um wie viel Prozent das Spielobjekt 1 die Überschneidung mit dem
     *            Spielobjekt 2 korrigieren wird.
     * @param object1CorrectionDirection
     *            Die Richtung, in der das Spielobjekt 1 und entgegen das Spielobjekt 2 korrigiert wird.
     * @return Die beiden Korrektur-vektoren (.vector1 ist der Korrekturvektor für das Spielobjekt 1, .vector2 der für
     *         das Spielobjekt 2).
     */
    private static Vectors getCorrectionVectorsByObjectCollision(RadialGameObject object1, RadialGameObject object2,
            double object1CorrectionPercent, Vector2D object1CorrectionDirection) {
        return PhysicsCorrectPositions.getCorrectionVectorsByCircleCollision(object1, object2.getPosition(),
                object2.getDiameter(), object1CorrectionPercent, object1CorrectionDirection);
    }

    /**
     * Korrigert das übergebene {@link ExtendedRadialGameObject} aus dem übergebenen Kreis, falls es sich innerhalb
     * diesem befinden sollte oder sich mit diesem überschneidet.<br>
     * Dabei können die correctionDirections des {@link ExtendedRadialGameObject}s entsprechend durch die Angabe des
     * entsprechenden boolean-Parameters eingeschränkt werden.
     * 
     * @param extObject
     *            Das Spielobjekt.
     * @param notAllowedCirclePosition
     *            Die Position des Kreises, in dem das Objekt sich nicht befinden darf bzw nicht damit überschneiden
     *            darf.
     * @param notAllowedCircleDiameter
     *            Der Durchmesser des Kreises, in dem das Objekt sich nicht befinden darf bzw nicht damit überschneiden
     *            darf.
     * @param limitExtObjectsCorrectionDirections
     *            Gibt an, ob nach der Korrektur des Objekts dessen correctionDirections eingeschränkt werden sollen.
     * @return Der Korrektur-Vektor des Spielobjekts.
     */
    private static Vector2D correctExtObjectOutOfNotAllowedCircle(ExtendedRadialGameObject extObject,
            Position notAllowedCirclePosition, double notAllowedCircleDiameter,
            boolean limitExtObjectsCorrectionDirections) {

        Vector2D objectCorrectionVector = new Vector2D(0, 0);

        // Kollision
        if (GamePhysic.doCirclesCollide(extObject.object.getPosition(), extObject.object.getDiameter(),
                notAllowedCirclePosition, notAllowedCircleDiameter)) {

            // Kann das Objekt bewegt werden
            if (extObject.object instanceof MoveableRadialGameObject) {

                Vector2D optionalDefaultCollisionDirection = new Vector2D(0, -1);
                Vectors collisionAndTangentDirections = GamePhysic.getCollisionAndTangentDirections(
                        notAllowedCirclePosition, extObject.object.getPosition(), optionalDefaultCollisionDirection);
                Vector2D objectCorrectionDirection = collisionAndTangentDirections.vector1;
                Vectors correctionVectors = PhysicsCorrectPositions.getCorrectionVectorsByCircleCollision(
                        extObject.object, notAllowedCirclePosition, notAllowedCircleDiameter, 100,
                        objectCorrectionDirection);

                objectCorrectionVector = correctionVectors.vector1;

                // verschiebe das Objekt
                ((MoveableRadialGameObject) extObject.object).setPosition(objectCorrectionVector
                        .getAppliedPosition(extObject.object.getPosition()));

                // schränke die Korrektur-Vektoren ein, wenn erwünscht
                if (limitExtObjectsCorrectionDirections)
                    extObject.limitCorrectionDirection(objectCorrectionVector);
            }
        }

        return objectCorrectionVector;
    }

    /**
     * Korrigert das übergebene {@link ExtendedRadialGameObject} in das übergebene Rechteck, falls es sich außerhalb
     * diesem befinden sollte oder sich mit diesem überschneidet.<br>
     * Dabei können die correctionDirections des {@link ExtendedRadialGameObject}s entsprechend durch die Angabe des
     * entsprechenden boolean-Parameters eingeschränkt werden.
     * 
     * @param extObject
     *            Das Spielobjekt.
     * @param topLeftBoxPosition
     *            Die Position der linken, oberen Ecke des Rechtecks.
     * @param bottomRightBoxPosition
     *            Die Position der rechten, unteren Ecke des Rechtecks.
     * @param limitExtObjectsCorrectionDirections
     *            Gibt an, ob nach der Korrektur des Objekts dessen correctionDirections eingeschränkt werden sollen.
     * @return Der horizontale(.vetor1)- und vertikale(.vector2) Korrektur-Vektor.
     */
    private static Vectors correctExtObjectInBox(ExtendedRadialGameObject extObject, Position topLeftBoxPosition,
            Position bottomRightBoxPosition, boolean limitExtObjectsCorrectionDirections) {

        Vectors objectCorrectionVectors = new Vectors(new Vector2D(0, 0), new Vector2D(0, 0));

        // Kollision
        BoundSides boundSides = GamePhysic.getObjectBoxCollisionState(extObject.object, topLeftBoxPosition,
                bottomRightBoxPosition);
        if (boundSides.hasSides()) {

            // Kann das Objekt bewegt werden
            if (extObject.object instanceof MoveableRadialGameObject) {

                objectCorrectionVectors = PhysicsCorrectPositions.getCorrectionVectorsByBoxCollision(
                        extObject.object, topLeftBoxPosition, bottomRightBoxPosition);

                Vector2D objectCorrectionVector = Vector2D.addVectors(objectCorrectionVectors.vector1,
                        objectCorrectionVectors.vector2);

                // verschiebe das Objekt
                ((MoveableRadialGameObject) extObject.object).setPosition(objectCorrectionVector
                        .getAppliedPosition(extObject.object.getPosition()));

                // schränke die Korrektur-Vektoren ein, wenn erwünscht
                if (limitExtObjectsCorrectionDirections)
                    extObject.limitCorrectionDirection(objectCorrectionVector);
            }
        }

        return objectCorrectionVectors;
    }

    /**
     * Korrigert ein {@link ExtendedRadialGameObject} in die erlaubte Zone (= außerhalb des übergebenen
     * notAllowed-Kreises und innerhalb des übergebenen Rechtecks), nachdem das {@link ExtendedRadialGameObject} zuvor
     * nach einer Bandenkollision in das Rechteck korrigiert wurde (und sich nun evtl mit dem notAllowed-Kreis
     * überschneidet).<br>
     * Dabei können die correctionDirections des {@link ExtendedRadialGameObject}s entsprechend durch die Angabe des
     * entsprechenden boolean-Parameters eingeschränkt werden. <b>Eine genauere Beschreibung, sowie die einzelnen
     * Berechnungsschritte befinden sich in dem dazugehörigen Dokument im Implementierungsbericht.</b>
     * 
     * @param extObject
     *            Das Spielobjekt.
     * @param topLeftBoxPosition
     *            Die Position der linken, oberen Ecke des Rechtecks.
     * @param bottomRightBoxPosition
     *            Die Position der rechten, unteren Ecke des Rechtecks.
     * @param notAllowedCirclePosition
     *            Die Position des Kreises, in dem das Objekt sich nicht befinden darf bzw nicht damit überschneiden
     *            darf.
     * @param notAllowedCircleDiameter
     *            Der Durchmesser des Kreises, in dem das Objekt sich nicht befinden darf bzw nicht damit überschneiden
     *            darf.
     * @param boundSides
     *            Die Informationen der letzten Kollision mit dem Rechteck.
     * @param limitExtObjectsCorrectionDirections
     *            Gibt an, ob nach der Korrektur des Objekts dessen correctionDirections eingeschränkt werden sollen.
     * @return Vector, um den das Objekt von seiner ursprünglichen Position weggeschoben wurde.
     */
    private static Vector2D correctExtObjectAfterBoxCollisionInAllowedArea(ExtendedRadialGameObject extObject,
            Position topLeftBoxPosition, Position bottomRightBoxPosition, Position notAllowedCirclePosition,
            double notAllowedCircleDiameter, BoundSides boundSides, boolean limitExtObjectsCorrectionDirections) {

        Vector2D objectCorrectionVector = new Vector2D(0, 0);

        // Kollision
        if (GamePhysic.doCirclesCollide(extObject.object.getPosition(), extObject.object.getDiameter(),
                notAllowedCirclePosition, notAllowedCircleDiameter)) {

            // Kann das Objekt bewegt werden
            if (extObject.object instanceof MoveableRadialGameObject) {

                Vectors objectCorrectionDirections;

                if (boundSides.leftSide) {
                    if (boundSides.topSide)
                        objectCorrectionDirections = new Vectors(new Vector2D(0, 1), new Vector2D(1, 0));
                    else if (boundSides.bottomSide)
                        objectCorrectionDirections = new Vectors(new Vector2D(0, -1), new Vector2D(1, 0));
                    else
                        objectCorrectionDirections = new Vectors(new Vector2D(0, 1), new Vector2D(0, -1));
                } else if (boundSides.rightSide) {
                    if (boundSides.topSide)
                        objectCorrectionDirections = new Vectors(new Vector2D(0, 1), new Vector2D(-1, 0));
                    else if (boundSides.bottomSide)
                        objectCorrectionDirections = new Vectors(new Vector2D(0, -1), new Vector2D(-1, 0));
                    else
                        objectCorrectionDirections = new Vectors(new Vector2D(0, 1), new Vector2D(0, -1));
                } else
                    objectCorrectionDirections = new Vectors(new Vector2D(1, 0), new Vector2D(-1, 0));

                Vectors correctionVectors1 = PhysicsCorrectPositions.getCorrectionVectorsByCircleCollision(
                        extObject.object, notAllowedCirclePosition, notAllowedCircleDiameter, 100,
                        objectCorrectionDirections.vector1);
                Vectors correctionVectors2 = PhysicsCorrectPositions.getCorrectionVectorsByCircleCollision(
                        extObject.object, notAllowedCirclePosition, notAllowedCircleDiameter, 100,
                        objectCorrectionDirections.vector2);

                Vectors objectCorrectionVectors = new Vectors(correctionVectors1.vector1, correctionVectors2.vector1);

                // entscheide, welcher Vektor aus objectCorrectionVectors verwendet wird
                Vector2D smallerObjectCorrectionVector;
                Vector2D longerObjectCorrectionVector;

                if (objectCorrectionVectors.vector1.getLength() >= objectCorrectionVectors.vector2.getLength()) {
                    smallerObjectCorrectionVector = objectCorrectionVectors.vector2;
                    longerObjectCorrectionVector = objectCorrectionVectors.vector1;
                } else {
                    smallerObjectCorrectionVector = objectCorrectionVectors.vector1;
                    longerObjectCorrectionVector = objectCorrectionVectors.vector2;
                }

                Vector2D finalObjectCorrectionVector = null;

                // teste, ob sich durch das Anwenden des smallerObjectCorrectionVector keine neue Bandenkollision ergibt
                Position newObjectPosition = smallerObjectCorrectionVector.getAppliedPosition(extObject.object
                        .getPosition());
                BoundSides boundSides2 = GamePhysic.getCircleBoxCollisionState(newObjectPosition,
                        extObject.object.getDiameter(), topLeftBoxPosition, bottomRightBoxPosition);
                if (!boundSides2.hasSides()) {

                    finalObjectCorrectionVector = smallerObjectCorrectionVector;
                } else {

                    // teste, ob sich durch das Anwenden des longerObjectCorrectionVector keine neue Bandenkollision
                    // ergibt
                    newObjectPosition = longerObjectCorrectionVector.getAppliedPosition(extObject.object.getPosition());
                    boundSides2 = GamePhysic.getCircleBoxCollisionState(newObjectPosition,
                            extObject.object.getDiameter(), topLeftBoxPosition, bottomRightBoxPosition);

                    if (!boundSides2.hasSides()) {

                        finalObjectCorrectionVector = longerObjectCorrectionVector;
                    }
                }

                if (finalObjectCorrectionVector != null) {

                    objectCorrectionVector = finalObjectCorrectionVector;

                    // verschiebe das Objekt
                    ((MoveableRadialGameObject) extObject.object).setPosition(objectCorrectionVector
                            .getAppliedPosition(extObject.object.getPosition()));

                    // schränke die Korrektur-Vektoren ein, wenn erwünscht
                    if (limitExtObjectsCorrectionDirections)
                        extObject.limitCorrectionDirection(objectCorrectionVector);
                }
            }
        }

        return objectCorrectionVector;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Korrigert ein {@link ExtendedRadialGameObject} in die erlaubte Zone (= außerhalb des übergebenen
     * notAllowed-Kreises und innerhalb des übergebenen Rechtecks).<br>
     * <b>Eine genauere Beschreibung, sowie die einzelnen Berechnungsschritte befinden sich in dem dazugehörigen
     * Dokument im Implementierungsbericht.</b>
     * 
     * @param extObject
     *            Das Spielobjekt.
     * @param topLeftBoxPosition
     *            Die Position der linken, oberen Ecke des Rechtecks.
     * @param bottomRightBoxPosition
     *            Die Position der rechten, unteren Ecke des Rechtecks.
     * @param notAllowedCirclePosition
     *            Die Position des Kreises, in dem das Objekt sich nicht befinden darf bzw nicht damit überschneiden
     *            darf.
     * @param notAllowedCircleDiameter
     *            Der Durchmesser des Kreises, in dem das Objekt sich nicht befinden darf bzw nicht damit überschneiden
     *            darf.
     * @return Vector, um den das Objekt von seiner ursprünglichen Position weggeschoben wurde.
     */
    private static Vector2D correctExtObjectInAllowedArea(ExtendedRadialGameObject extObject,
            Position topLeftBoxPosition, Position bottomRightBoxPosition, Position notAllowedCirclePosition,
            double notAllowedCircleDiameter) {

        Position oldPosition = extObject.object.getPosition();

        // wurde ein notAllowedCircle angegeben
        if (notAllowedCirclePosition != null) {

            // korrigiere das Objekt, wenn es sich mit dem notAllowedCircle überschneidet
            Vector2D notAllowedCircleCorrectionVector = PhysicsCorrectPositions
                    .correctExtObjectOutOfNotAllowedCircle(extObject, notAllowedCirclePosition,
                            notAllowedCircleDiameter, false);

            // überschneidet sich anschließend das korrigierte Objekt mit der Box
            BoundSides boundSides = GamePhysic.getObjectBoxCollisionState(extObject.object, topLeftBoxPosition,
                    bottomRightBoxPosition);
            if (boundSides.hasSides()) {

                // korrigere das Objekt in die Box und schränke die correctionDirection's ein
                PhysicsCorrectPositions.correctExtObjectInBox(extObject, topLeftBoxPosition, bottomRightBoxPosition,
                        true);

                // überschneidet sich anschließend das Objekt erneut mit dem notAllowedCircle, dann korrigiere das
                // Objekt aus dem notAllowedCircle und in die Box und schränke die correctionDirection's ein
                PhysicsCorrectPositions.correctExtObjectAfterBoxCollisionInAllowedArea(extObject,
                        topLeftBoxPosition, bottomRightBoxPosition, notAllowedCirclePosition, notAllowedCircleDiameter,
                        boundSides, true);

            } else {

                // schränke die correctionDirection des Objekts (bzgl. der Überschneidung mit dem notAllowedCircle) ein
                if (notAllowedCircleCorrectionVector.getLength() != 0)
                    extObject.limitCorrectionDirection(notAllowedCircleCorrectionVector);
            }
        } else {

            // korrigere das Objekt in die Box und schränke die correctionDirection's ein
            PhysicsCorrectPositions.correctExtObjectInBox(extObject, topLeftBoxPosition, bottomRightBoxPosition,
                    true);
        }

        return new Vector2D(oldPosition, extObject.object.getPosition());
    }

    /**
     * Korrigert/verschiebt zwei kollidierende {@link ExtendedRadialGameObject}s, so dass sie sich nicht mehr
     * überschneiden und nebeneinander stehen.<br>
     * Dabei wird zuerst geprüft, ob mind eines der beiden Objekte entlang der Zentralrichtung korrigieren kann. Ist das
     * nicht der Fall, werden weitere Richtungen überprüft bis eine Richtung gefunden wurde, in der mind. ein Objekt
     * korrigieren kann.<br>
     * Dabei werden die correctionDirections der beiden Objekte entsprechend eingeschränkt.
     * <b>Eine genauere Beschreibung, sowie die einzelnen Berechnungsschritte befinden sich in dem dazugehörigen
     * Dokument im Implementierungsbericht.</b>
     * 
     * @param extObject1
     *            Das Spielobjekt 1.
     * @param extObject2
     *            Das Spielobjekt 2.
     * @param extObject1CorrectionPercent
     *            Die Prozentangabe [0, 100], um wie viel Prozent das Spielobjekt 1 die Überschneidung mit dem
     *            Spielobjekt 2 korrigieren wird.
     * @return Die beiden Korrektur-vektoren (.vector1 ist der Korrekturvektor für das Spielobjekt 1, .vector2 der für
     *         das Spielobjekt 2).
     */
    private Vectors correctCollidingExtObjects(ExtendedRadialGameObject extObject1,
            ExtendedRadialGameObject extObject2, double extObject1CorrectionPercent) {

        Vectors correctionVectors = new Vectors(new Vector2D(0, 0), new Vector2D(0, 0));

        // Kollision
        if (GamePhysic.doObjectsCollide(extObject1.object, extObject2.object)) {

            Vector2D optionalDefaultCollisionDirection = new Vector2D(PhysicsCorrectPositions.getMiddleBoxPosition(
                    this.fieldTopLeftPosition, this.fieldBottomRightPosition), extObject1.object.getPosition());
            Vectors collisionAndTangentDirections = GamePhysic
                    .getCollisionAndTangentDirections(extObject2.object.getPosition(), extObject1.object.getPosition(),
                            optionalDefaultCollisionDirection);

            boolean canCorrect = false;
            int angleCounter = 0;
            final double ANGLE_STEP = 5;
            double object1CorrectionPercent = 100;
            boolean limitObject1CorrectionDirections = false;
            boolean limitObject2CorrectionDirections = false;

            Vector2D testingObject1CorrectionDirection = collisionAndTangentDirections.vector1;
            Vector2D testingObject2CorrectionDirection = testingObject1CorrectionDirection.getInvertedVector();

            while (!canCorrect && (angleCounter * ANGLE_STEP <= 360)) {

                canCorrect = true;
                object1CorrectionPercent = 100;
                limitObject1CorrectionDirections = false;
                limitObject2CorrectionDirections = false;

                testingObject1CorrectionDirection = collisionAndTangentDirections.vector1.getRotatedVector(angleCounter
                        * ANGLE_STEP, true);
                testingObject2CorrectionDirection = testingObject1CorrectionDirection.getInvertedVector();
                ++angleCounter;

                // kann extObject1 korrigieren
                if (extObject1.canCorrectInDirection(testingObject1CorrectionDirection)) {

                    // kann extObject2 korrigieren
                    if (extObject2.canCorrectInDirection(testingObject2CorrectionDirection)) {

                        object1CorrectionPercent = extObject1CorrectionPercent;

                    } else {
                        // extObject1 kann nur korrigieren (-> 100%)
                        object1CorrectionPercent = 100;

                        // schränke die correctionDirection ein
                        limitObject1CorrectionDirections = true;
                    }
                } else {
                    // kann extObject2 korrigieren
                    if (extObject2.canCorrectInDirection(testingObject2CorrectionDirection)) {

                        // extObject2 kann nur korrigieren (-> 0%)
                        object1CorrectionPercent = 0;

                        // schränke die correctionDirection ein
                        limitObject2CorrectionDirections = true;

                    } else {
                        // beide können nicht korrigieren
                        canCorrect = false;

                        // schränke ihre correctionDirection's ein
                        limitObject1CorrectionDirections = true;
                        limitObject2CorrectionDirections = true;
                    }
                }
            }

            if (canCorrect) {

                correctionVectors = PhysicsCorrectPositions.getCorrectionVectorsByObjectCollision(extObject1.object,
                        extObject2.object, object1CorrectionPercent, testingObject1CorrectionDirection);

                if ((extObject1.object instanceof MoveableRadialGameObject)
                        && (correctionVectors.vector1.getLength() != 0)) {

                    // setze die neue Position von extObject1
                    ((MoveableRadialGameObject) extObject1.object).setPosition(correctionVectors.vector1
                            .getAppliedPosition(extObject1.object.getPosition()));

                    // schränke die correctionDirections von extObject1 ein, wenn erwünscht
                    if (limitObject1CorrectionDirections)
                        extObject1.limitCorrectionDirection(testingObject1CorrectionDirection);
                }

                if ((extObject2.object instanceof MoveableRadialGameObject)
                        && (correctionVectors.vector2.getLength() != 0)) {

                    // setze die neue Position von extObject2
                    ((MoveableRadialGameObject) extObject2.object).setPosition(correctionVectors.vector2
                            .getAppliedPosition(extObject2.object.getPosition()));

                    // schränke die correctionDirections von extObject2 ein, wenn erwünscht
                    if (limitObject2CorrectionDirections)
                        extObject1.limitCorrectionDirection(testingObject2CorrectionDirection);
                }
            }
        }

        return correctionVectors;
    }

    /**
     * Bestimmt für jede Spielsituation, sowie für den derzeitigen Simulationszustand die passenden Parameter, mit denen
     * die Funktion
     * {@link #calculate_correctExtObjectInAllowedArea(ExtendedRadialGameObject, SoSiTickInformation, TickEvent)}
     * aufgerufen wird.
     * 
     * @param extObject
     *            Das Spielobjekt.
     * @param tickInformationOfTeamA
     *            Die Tick-Informationen des Teams A.
     * @param simulationState
     *            Der derzeitige Simulationszustand.
     * @return Vector, um den das Objekt von seiner ursprünglichen Position weggeschoben wurde.
     */
    private Vector2D calculate_correctExtObjectInAllowedArea(ExtendedRadialGameObject extObject,
            SoSiTickInformation tickInformationOfTeamA, TickEvent simulationState) {

        Position notAllowedCirclePosition = null;
        double notAllowedCircleDiameter = 0;

        Position topLeftBoxPosition = this.correctionBoxTopLeftPosition;
        Position bottomRightBoxPosition = this.correctionBoxBottomRightPosition;

        PlayerGameObject playerWithBallOrWhoKickedBall;

        if (simulationState == TickEvent.KICK_OFF) {

            notAllowedCirclePosition = this.ball.getPosition();
            notAllowedCircleDiameter = PhysicsCorrectPositions.MINIMUM_DISTANCE_TO_KICK_OFF_POSITION * 2d;

            // links
            if (((tickInformationOfTeamA.isPlayingOnTheLeft()) && (this.teamA.getPlayers().contains(extObject.object)))
                    || ((!tickInformationOfTeamA.isPlayingOnTheLeft()) && (this.teamB.getPlayers()
                            .contains(extObject.object)))) {
                topLeftBoxPosition = this.fieldTopLeftPosition;
                bottomRightBoxPosition = new SoSiPosition(this.fieldTopLeftPosition.getX()
                        + ((this.fieldBottomRightPosition.getX() - this.fieldTopLeftPosition.getX()) / 2d),
                        this.fieldBottomRightPosition.getY());
            } else {
                topLeftBoxPosition = new SoSiPosition(this.fieldTopLeftPosition.getX()
                        + ((this.fieldBottomRightPosition.getX() - this.fieldTopLeftPosition.getX()) / 2d),
                        this.fieldTopLeftPosition.getY());
                bottomRightBoxPosition = this.fieldBottomRightPosition;
            }
        } else if (simulationState == TickEvent.FREE_KICK) {

            notAllowedCirclePosition = this.ball.getPosition();
            notAllowedCircleDiameter = PhysicsCorrectPositions.MINIMUM_DISTANCE_TO_FREE_KICK_POSITION * 2d;
            topLeftBoxPosition = this.fieldTopLeftPosition;
            bottomRightBoxPosition = this.fieldBottomRightPosition;
        }

        // Ballbesitz abfragen
        if (simulationState == TickEvent.FREE_KICK || simulationState == TickEvent.KICK_OFF) {

            // Falls KickOff oder Abstoß vorliegt, soll Spieler, welcher den Ball gekickt hat (und daher nicht mehr im
            // Ballbesitz ist), in der Physik weiterhin als "in Ballbesitz" gewertet werden.
            playerWithBallOrWhoKickedBall = this.ball.getBallPossession() != null ? this.ball.getBallPossession()
                    : this.ball.getLastBallPosession();
        } else {

            // Im Normalfall ist Ballbesitz für Physik = aktueller Ballbesitz
            playerWithBallOrWhoKickedBall = this.ball.getBallPossession();
        }

        if (extObject.object instanceof MoveableRadialGameObject) {

            if ((extObject.object instanceof PlayerGameObject) && (extObject.object == playerWithBallOrWhoKickedBall)) {

                // Objekt ist Spieler, welcher in Ballbesitz ist oder bei Freistoß bzw Abstoß gerade die Ball gekickt
                // hat
                return PhysicsCorrectPositions.correctExtObjectInAllowedArea(extObject,
                        this.correctionBoxTopLeftPosition, this.correctionBoxBottomRightPosition, null,
                        notAllowedCircleDiameter);
            } else if (extObject.object instanceof BallGameObject) {

                // Objekt ist ein Ball
                return PhysicsCorrectPositions.correctExtObjectInAllowedArea(extObject,
                        this.correctionBoxTopLeftPosition, this.correctionBoxBottomRightPosition, null,
                        notAllowedCircleDiameter);
            } else {

                // Anderes Objekt (Spieler ohne Ballbesitz, etc.)
                return PhysicsCorrectPositions.correctExtObjectInAllowedArea(extObject, topLeftBoxPosition,
                        bottomRightBoxPosition, notAllowedCirclePosition, notAllowedCircleDiameter);
            }
        } else
            return new Vector2D(0, 0);
    }

    // private int maxReachedLoopCount = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void calculate(SoSiTickInformation tickInformationOfTeamA, TickEvent simulationState) {

        for (ExtendedRadialGameObject extGameObject : this.extGameObjects)
            extGameObject.reset();

        for (ExtendedRadialGameObject extGameObject : this.extGameObjects) {
            this.calculate_correctExtObjectInAllowedArea(extGameObject, tickInformationOfTeamA, simulationState);
            if ((this.ball.getBallPossession() != null) && (this.ball.getBallPossession() == extGameObject.object))
                this.ball.setPosition(extGameObject.object.getPosition());
        }

        boolean sthChanged = true;
        int loopCounter = 0;

        while ((sthChanged) && (loopCounter < MAX_LOOP_COUNT)) {
            sthChanged = false;
            ++loopCounter;
            for (int i = 0; i <= this.extGameObjects.size() - 2; ++i) {

                for (int j = i + 1; j <= this.extGameObjects.size() - 1; ++j) {

                    ExtendedRadialGameObject extObject1 = this.extGameObjects.get(i);
                    ExtendedRadialGameObject extObject2 = this.extGameObjects.get(j);

                    if ((extObject1.object instanceof MoveableRadialGameObject)
                            || (extObject2.object instanceof MoveableRadialGameObject)) {

                        if (((extObject1.object instanceof BallGameObject)
                                && (extObject2.object instanceof PlayerGameObject) && (((BallGameObject) extObject1.object)
                                .getBallPossession() == extObject2.object))
                                || ((extObject1.object instanceof PlayerGameObject)
                                        && (extObject2.object instanceof BallGameObject) && (((BallGameObject) extObject2.object)
                                        .getBallPossession() == extObject1.object))) {

                        } else {

                            if (extObject1 != extObject2) {
                                if (GamePhysic.doObjectsCollide(extObject1.object, extObject2.object)) {

                                    Vectors correctionVectors = this.correctCollidingExtObjects(extObject1, extObject2,
                                            50);

                                    Vector2D correctionVector1 = this.calculate_correctExtObjectInAllowedArea(
                                            extObject1, tickInformationOfTeamA, simulationState);

                                    Vector2D correctionVector2 = this.calculate_correctExtObjectInAllowedArea(
                                            extObject2, tickInformationOfTeamA, simulationState);

                                    if ((correctionVectors.vector1.getLength() != 0)
                                            || (correctionVectors.vector2.getLength() != 0)
                                            || (correctionVector1.getLength() != 0)
                                            || (correctionVector2.getLength() != 0))
                                        sthChanged = true;

                                }
                            }
                        }
                    }
                }
            }

            if (this.ball.getBallPossession() != null) {
                this.ball.setPosition(this.ball.getBallPossession().getPosition());
            }
        }

        // if (loopCounter > maxReachedLoopCount) {
        // maxReachedLoopCount = loopCounter;
        // System.out.println("New max-reached-loop-counter: " + maxReachedLoopCount);
        // }
    }

}
