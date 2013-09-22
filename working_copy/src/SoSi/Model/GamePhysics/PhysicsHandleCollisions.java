package SoSi.Model.GamePhysics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import SoSi.Model.TickEvent;
import SoSi.Model.Calculation.SoSiTickInformation;
import SoSi.Model.Calculation.Team;
import SoSi.Model.Calculation.Vector2D;
import SoSi.Model.GameObjects.BallGameObject;
import SoSi.Model.GameObjects.GoalGameObject;
import SoSi.Model.GameObjects.MoveableRadialGameObject;
import SoSi.Model.GameObjects.PlayerGameObject;
import SoSi.Model.GameObjects.PostGameObject;
import SoSi.Model.GameObjects.RadialGameObject;

/**
 * Diese Klasse ist zuständig für die Kollisionsberechnung aller Spielobjekte.<br>
 * Bei einem Zusammenstoß zwischen zwei je Spiel-Objekten, werden deren movementDirections entsprechend den
 * physikalischen Gesetzen eines elastischen Stoßes neu gesetzt.<br>
 * Dieser Stoß ist abhängig von dem Massenunterschied der zwei kollidieren Objekte, sowie deren Richtungsvektoren und
 * Geschwindigkeiten.<br>
 * Außerdem wird zwischen den bewegungsfähigen {@link MoveableRadialGameObject}s und den nicht bewegungsfähigen
 * {@link PostGameObject}s unterschieden.<br>
 * {@link PostGameObject}s besitzen in der Berechnung eine "unendlich große" Masse, d.h. sie werden bei einer Kollision
 * nicht von ihrer Position verschoben.<br>
 * Zusätzlich wird die neu berechnete movementDirection um einen festgelegten Wert reduziert, der durch den
 * Kollisionspartner festgelegt ist.<br>
 * Die Positionen der Kollisionspartner werden in dieser Klasse nicht verändert.
 */
public class PhysicsHandleCollisions extends GamePhysic {

    /**
     * Die Masse eines Spieler (im Bezug auf die Masse eines Balles)
     */
    private static final double MASS_PLAYER = 1d;

    /**
     * Die Masse eines Balles (im Bezug auf die Masse eines Spielers)
     */
    private static final double MASS_BALL = 0.05d;

    /**
     * Prozent-Wert der akuellen Geschwindigkeit, um den die Geschwindigkeit eines Balles abnimmt, wenn er mit einem
     * anderen Spielobjekt kollidiert.
     */
    private static final double COLLISION_BALL_DECELERATION_PERCENT = 5d;

    /**
     * Prozent-Wert der akuellen Geschwindigkeit, um den die Geschwindigkeit eines Spielers abnimmt, wenn er mit einem
     * Ball kollidiert.
     */
    private static final double COLLISION_PLAYER_BALL_DECELERATION_PERCENT = 5d;

    /**
     * Prozent-Wert der akuellen Geschwindigkeit, um den die Geschwindigkeit eines Spielers abnimmt, wenn er mit einem
     * anderen Spieler kollidiert.
     */
    private static final double COLLISION_PLAYER_PLAYER_DECELERATION_PERCENT = 30d;

    /**
     * Prozent-Wert der akuellen Geschwindigkeit, um den die Geschwindigkeit eines Spielers abnimmt, wenn er mit einem
     * Pfosten kollidiert.
     */
    private static final double COLLISION_PLAYER_POST_DECELERATION_PERCENT = 80d;

    /**
     * Diese innere Klasse dient als Rückgabetyp der Methode oneDimensionalElasticCentralCollision.<br>
     * Sie beinhaltet die resultierenden Geschwindigkeiten (Achtung: können negativ sein), die in der genannten Methode
     * berechnet wurden.
     */
    private static class Velocities {

        /**
         * Die erste Geschwindigkeit
         */
        public double velocity1;

        /**
         * Die zweite Geschwindigkeit
         */
        public double velocity2;

        /**
         * Erzeugt eine neue Sammlung von zwei Geschwindigkeitswerten.
         * 
         * @param velocity1
         *            Die erste Geschwindigkeit.
         * @param velocity2
         *            Die zweite Geschwindigkeit.
         */
        public Velocities(double velocity1, double velocity2) {
            this.velocity1 = velocity1;
            this.velocity2 = velocity2;
        }
    }

    /**
     * Der Ball
     */
    private final BallGameObject ball;

    /**
     * Die Liste aller (kollidierbarer) Spielobjekte
     */
    private final List<RadialGameObject> gameObjects;

    /**
     * Erzeugt die neue Physik-Klasse, die für die Kollisionsberechnung aller Spielobjekte zuständig ist.
     * 
     * @param teamA
     *            {@link Team}-Instanz des Teams A.
     * @param teamB
     *            {@link Team}-Instanz des Teams B.
     * @param ball
     *            Das Simulationsobjekt Ball vom Typ {@link BallGameObject}
     * @param goal1
     *            Ein Simulationsobjekt Tor vom Typ {@link GoalGameObject}
     * @param goal2
     *            Ein Simulationsobjekt Tor vom Typ {@link GoalGameObject}
     */
    public PhysicsHandleCollisions(Team teamA, Team teamB, BallGameObject ball, GoalGameObject goal1,
            GoalGameObject goal2) {
        this.gameObjects = new ArrayList<RadialGameObject>();

        // Team A
        for (RadialGameObject playerTeamA : teamA.getPlayers())
            this.gameObjects.add(playerTeamA);

        // Team B
        for (RadialGameObject playerTeamB : teamB.getPlayers())
            this.gameObjects.add(playerTeamB);

        // Der Ball
        this.ball = ball;
        this.gameObjects.add(ball);

        // Die beiden Pfosten des 1. Tores
        this.gameObjects.add(goal1.getPostLeft());
        this.gameObjects.add(goal1.getPostRight());

        // Die beiden Pfosten des 2. Tores
        this.gameObjects.add(goal2.getPostLeft());
        this.gameObjects.add(goal2.getPostRight());
    }

    /**
     * Teilt den Bewegungsvektor eines Objekts auf in die Komponenten parallel zur übergebenen Kollisionsrichtung und
     * Richtung des zugehörigen Tangentialvektor, so dass die Komponenten addiert wieder den Bewegungsvektor ergeben.
     * Die Kollisions-Komponenten der Bewegungsvektoren werden später als Parameter für die Berechnung des
     * eindimensionalen, zentralen elastischen Stoßes verwendet.
     * 
     * @param object
     *            Das Objekt, dessen Bewegungsvektor aufgespalten werden soll.
     * @param collisionAndTangentDirections
     *            Die Kollisionsrichtung mit Länge > 0 (als .vector1) und die Richtung des dazugehörigen
     *            Tangentialvektors mit Länge > 0 (als .vector2).
     * @return Der Kollisions-Part (als .vector1) und der Tangential-Part (als .vector2);
     */
    private static Vectors splitMovementVector(RadialGameObject object, Vectors collisionAndTangentDirections) {

        // Der Kollisionsvektor und Tangentialvektor sollten immer eine Länge > 0 haben.
        if ((collisionAndTangentDirections.vector1.getLength() == 0)
                || (collisionAndTangentDirections.vector2.getLength() == 0))
            throw new ArithmeticException("Not expected");

        // wenn sich das Objekt nicht bewegen kann bzw. der Bewegungsvektor die Länge 0 hat
        if ((!(object instanceof MoveableRadialGameObject))
                || (((MoveableRadialGameObject) object).getMovementDirection().getLength() == 0))
            return new Vectors(new Vector2D(0, 0), new Vector2D(0, 0));
        else {
            Vector2D movementVector = ((MoveableRadialGameObject) object).getMovementDirection();

            Vector2D collisionPartVector = Vector2D.rightAngleProjectionOfVectorInVector(movementVector,
                    collisionAndTangentDirections.vector1);
            Vector2D tangentPartVector = Vector2D.rightAngleProjectionOfVectorInVector(movementVector,
                    collisionAndTangentDirections.vector2);

            return new Vectors(collisionPartVector, tangentPartVector);
        }
    }

    /**
     * Beinhaltet die physikalischen Berechnungen eines eindimensionalen, zentralen elastischen Stoßes zwischen zwei
     * Kollisionsobjekten.<br>
     * Dabei werden die Kollisionsobjekte abhängig von ihrem Massenunterschied, sowie ihren Geschwindigkeiten elastisch
     * voneinander weg- bzw. in die selbe Richtung gestoßen.<br>
     * Die Kollisionsobjekte werden einerseits angegeben durch ihre Geschwindigkeit (entspricht der Vektorlänge) vor dem
     * Stoß.<br>
     * Das Vorzeichen der Geschwindigkeit spiegelt die Bewegungsrichtung wider, d.h. Objekte mit gleichem Vorzeichen
     * bewegen sich in die selbe Richtung fort.<br>
     * Andererseits beseteht die Möglichkeit eines Objektes eine "unendliche Masse" zu besitzen, d.h. sie werden von der
     * Kollision nicht beeinflusst.<br>
     * Das ist in der Realität nicht möglich, ermöglicht es aber später, nicht zwischen beweglichen
     * {@link MoveableRadialGameObject}s und unbeweglichen {@link PostGameObject}s (besitzen hier eine
     * "unendliche Masse") unterscheiden zu müssen.<br>
     * Besitzt keines der Objekte eine "unendliche Masse", bestimmt der Massenunterschied massRatio den Wert, der sich
     * aus der Division der Masse von Objekt 1 durch deren von Objekt 2 ergibt.<br>
     * Zurückgegeben wird eine {@link Velocities}-Instanz, die die beiden resultierenden Geschwindigkeiten (deren
     * Vorzeichen bestimmt die Richtung der Geschwindigkeit, wie oben beschrieben) der beiden Kollisionsobjekte.
     * 
     * @param velocity1
     *            Die Geschwindigkeit des Kollisionsobjekts 1 (Vorzeichen bestimmt die Richtung) vor dem Stoß.
     * @param hasEndlessMass1
     *            Bestimmt, ob das Kollisionsobjekt 1 eine "unendliche Masse" besitzt.
     * @param velocity2
     *            Die Geschwindigkeit des Kollisionsobjekts 2 (Vorzeichen bestimmt die Richtung) vor dem Stoß.
     * @param hasEndlessMass2
     *            Bestimmt, ob das Kollisionsobjekt 2 eine "unendliche Masse" besitzt.
     * @param massRatio
     *            Division aus der Masse des Kollisionsobjektes 1 mit deren des Kollisionsobjektes 2, falls keines der
     *            beiden eine "unendliche Masse" besitzt.
     * @return Die beiden resultierenden Geschwindigkeiten nach dem Stoß (Vorzeichen bestimmt die Richtung).
     * @throws IllegalArgumentException
     *             Die Exception, wenn massRatio einen ungültigen Wert besitzt.
     */
    private static Velocities oneDimensionalElasticCentralCollision(double velocity1, boolean hasEndlessMass1,
            double velocity2, boolean hasEndlessMass2, double massRatio) {

        if (massRatio <= 0)
            throw new IllegalArgumentException("massRatio has invalid value");

        double newVelocity1;
        double newVelocity2;

        if (hasEndlessMass1 || hasEndlessMass2) {
            if (hasEndlessMass1)
                newVelocity1 = velocity1;
            newVelocity2 = (2d * velocity1) - velocity2;
            if (hasEndlessMass2)
                newVelocity2 = velocity2;
            newVelocity1 = (2d * velocity2) - velocity1;
        } else {
            double part = (velocity1 / (1d + (1d / massRatio))) + (velocity2 / (1d + massRatio));
            newVelocity1 = (2d * part) - velocity1;
            newVelocity2 = (2d * part) - velocity2;
        }

        return new Velocities(newVelocity1, newVelocity2);
    }

    /**
     * Berechnet die resultierenden Kollisions-Parts der beiden kollidierenden Objekte nach einer Kollision anhand eines
     * eindimensionalen, zentralen elastischen Stoßes. <br>
     * Dafür bekommt die Methode die Kollisions-Parts beider Objekte, die außerhalb mittels der übergebenen
     * Kollisionsrichtung aufgespalten wurden. Auf die beiden Kollisions-Parts wird die Methode
     * {@link #oneDimensionalElasticCentralCollision(double, boolean, double, boolean, double)} angewandt und die
     * resultierenden Kollisions-Parts beider Objekte berechnet und zurückgegeben.
     * 
     * @param collisionAndTangentDirections
     *            Die Kollisionsrichtung mit Länge > 0 (als .vector1) und die Richtung des dazugehörigen
     *            Tangentialvektors mit Länge > 0 (als .vector2).
     * @param collisionPartVector1
     *            Der Kollisions-Part des ersten Objekts.
     * @param hasEndlessMass1
     *            Bestimmt, ob das Kollisionsobjekt 1 eine "unendliche Masse" besitzt.
     * @param collisionPartVector2
     *            Der Kollisions-Part des zweiten Objekts.
     * @param hasEndlessMass2
     *            Bestimmt, ob das Kollisionsobjekt 2 eine "unendliche Masse" besitzt.
     * @param massRatio
     *            Division aus der Masse des Kollisionsobjektes 1 mit deren des Kollisionsobjektes 2, falls keines der
     *            beiden eine "unendliche Masse" besitzt.
     * @return Die resultierenden Kollisions-Parts der beiden kollidierenden Objekte.
     */
    private static Vectors calculateOneDimensionalElasticCentralCollisionVectors(Vectors collisionAndTangentDirections,
            Vector2D collisionPartVector1, boolean hasEndlessMass1, Vector2D collisionPartVector2,
            boolean hasEndlessMass2, double massRatio) {

        // Der Zentralvektor und Tangentialvektor sollten immer eine Länge > 0 haben.
        if ((collisionAndTangentDirections.vector1.getLength() == 0)
                || (collisionAndTangentDirections.vector2.getLength() == 0))
            throw new ArithmeticException("Not expected");

        int collisionPartVector1Sgn = 1;
        int collisionPartVector2Sgn = 1;

        // Die Kollisions-Vektoren, die in in die andere Richtung zeigen, wie der Kollisionsvektor bekommen ein
        // negatives
        // Vorzeichen
        if (Vector2D.getScalarProduct(collisionPartVector1, collisionAndTangentDirections.vector1) < 0)
            collisionPartVector1Sgn = -1;
        if (Vector2D.getScalarProduct(collisionPartVector2, collisionAndTangentDirections.vector1) < 0)
            collisionPartVector2Sgn = -1;

        double velocity1 = collisionPartVector1Sgn * collisionPartVector1.getLength();
        double velocity2 = collisionPartVector2Sgn * collisionPartVector2.getLength();

        Velocities resultVelocities = PhysicsHandleCollisions.oneDimensionalElasticCentralCollision(velocity1,
                hasEndlessMass1, velocity2, hasEndlessMass2, massRatio);

        Vector2D resultCollisionPartVector1;
        if (resultVelocities.velocity1 >= 0)
            resultCollisionPartVector1 = collisionAndTangentDirections.vector1.getNewLengthVector(Math
                    .abs(resultVelocities.velocity1));
        else
            resultCollisionPartVector1 = collisionAndTangentDirections.vector1.getInvertedVector().getNewLengthVector(
                    Math.abs(resultVelocities.velocity1));

        Vector2D resultCollisionPartVector2;
        if (resultVelocities.velocity2 >= 0)
            resultCollisionPartVector2 = collisionAndTangentDirections.vector1.getNewLengthVector(Math
                    .abs(resultVelocities.velocity2));
        else
            resultCollisionPartVector2 = collisionAndTangentDirections.vector1.getInvertedVector().getNewLengthVector(
                    Math.abs(resultVelocities.velocity1));

        return new Vectors(resultCollisionPartVector1, resultCollisionPartVector2);
    }

    /**
     * Berechnet und setzt die resultierenden Bewegungsvektoren der zwei übergebenen Objekte anhand eines
     * eindimensionalen, zentralen elastischen Stoßes.
     * 
     * @param object1
     *            Das erste Objekt.
     * @param object2
     *            Das zweite Objekt.
     * @param massRatio
     *            Division aus der Masse des Kollisionsobjektes 1 mit deren des Kollisionsobjektes 2, falls keines der
     *            beiden eine "unendliche Masse" besitzt.
     */
    private static void applyCollision(RadialGameObject object1, RadialGameObject object2, double massRatio) {

        Vectors collisionAndTangentDirections = GamePhysic.getCollisionAndTangentDirectionsForCollisions(object1,
                object2);

        Vectors partVectors1 = PhysicsHandleCollisions.splitMovementVector(object1, collisionAndTangentDirections);
        Vectors partVectors2 = PhysicsHandleCollisions.splitMovementVector(object2, collisionAndTangentDirections);

        boolean hasEndlessMass1 = !(object1 instanceof MoveableRadialGameObject);
        boolean hasEndlessMass2 = !(object2 instanceof MoveableRadialGameObject);

        Vectors resultCollisionPartVectors = PhysicsHandleCollisions
                .calculateOneDimensionalElasticCentralCollisionVectors(collisionAndTangentDirections,
                        partVectors1.vector1, hasEndlessMass1, partVectors2.vector1, hasEndlessMass2, massRatio);

        Vector2D resultMovementVector1 = Vector2D.addVectors(resultCollisionPartVectors.vector1, partVectors1.vector2);
        Vector2D resultMovementVector2 = Vector2D.addVectors(resultCollisionPartVectors.vector2, partVectors2.vector2);

        if (object1 instanceof MoveableRadialGameObject)
            ((MoveableRadialGameObject) object1).setMovementDirection(resultMovementVector1);
        if (object2 instanceof MoveableRadialGameObject)
            ((MoveableRadialGameObject) object2).setMovementDirection(resultMovementVector2);
    }

    /**
     * Liefert die Division aus der Masse des Objekts 1 mit deren des Objekts 2.
     * 
     * @param object1
     *            Das erste Objekt.
     * @param object2
     *            Das zweite Objekt.
     * @return Die Division aus der Masse des Objekts 1 mit deren des Objekts 2.
     */
    private static double getMassRatio(RadialGameObject object1, RadialGameObject object2) {
        if ((object1 instanceof MoveableRadialGameObject) && (object2 instanceof MoveableRadialGameObject)) {
            double massObject1 = 1;
            double massObject2 = 1;

            if (object1 instanceof BallGameObject)
                massObject1 = PhysicsHandleCollisions.MASS_BALL;
            if (object2 instanceof BallGameObject)
                massObject2 = PhysicsHandleCollisions.MASS_BALL;

            if (object1 instanceof PlayerGameObject)
                massObject1 = PhysicsHandleCollisions.MASS_PLAYER;
            if (object2 instanceof PlayerGameObject)
                massObject2 = PhysicsHandleCollisions.MASS_PLAYER;

            return (massObject1 / massObject2);
        } else
            return 1;
    }

    /**
     * Zieht der movementDirection des übergebenen Objekts einen Geschwindigkeitsbetrag ab, der durch eine Kollision mit
     * dem collisionObject entstanden ist.<br>
     * Dieser ist abhängig, von der Art der beiden übergebenen Kollisionsobjekte.
     * 
     * @param object
     *            Das Objet, dessen movementDirection an Geschwindigkeit verlieren soll.
     * @param collisionObject
     */
    private static void looseCollisionVelocities(RadialGameObject object, RadialGameObject collisionObject) {

        // ist das Objekt beweglich?
        if (object instanceof MoveableRadialGameObject) {

            double looseSpeedPercent = 0;

            if (object instanceof BallGameObject)
                looseSpeedPercent = PhysicsHandleCollisions.COLLISION_BALL_DECELERATION_PERCENT;

            if (object instanceof PlayerGameObject) {
                if (collisionObject instanceof BallGameObject)
                    looseSpeedPercent = PhysicsHandleCollisions.COLLISION_PLAYER_BALL_DECELERATION_PERCENT;
                if (collisionObject instanceof PlayerGameObject)
                    looseSpeedPercent = PhysicsHandleCollisions.COLLISION_PLAYER_PLAYER_DECELERATION_PERCENT;
                if (collisionObject instanceof PostGameObject)
                    looseSpeedPercent = PhysicsHandleCollisions.COLLISION_PLAYER_POST_DECELERATION_PERCENT;
            }

            // verringere die movementDirection um den Prozent-Wert
            ((MoveableRadialGameObject) object).setMovementDirection(GamePhysic.looseMovementDirectionSpeedPercent(
                    ((MoveableRadialGameObject) object).getMovementDirection(), looseSpeedPercent));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void calculate(SoSiTickInformation tickInformationOfTeamA, TickEvent simulationState) {
        if (simulationState == TickEvent.KICK_OFF || simulationState == TickEvent.FREE_KICK)
            return; // don't check for collisions on kickOff and freeKick

        List<RadialGameObject> gameObjectsToCheck = new LinkedList<RadialGameObject>(this.gameObjects);

        if (this.ball.getBallPossession() != null)
            gameObjectsToCheck.remove(this.ball);

        for (int i = 0; i < gameObjectsToCheck.size(); ++i) {
            RadialGameObject collidableRadialGameObject1 = gameObjectsToCheck.get(i);
            for (int j = i + 1; j < gameObjectsToCheck.size(); ++j) {
                RadialGameObject collidableRadialGameObject2 = gameObjectsToCheck.get(j);

                // kollidieren die beiden Objekte?
                if (PhysicsHandleCollisions.doObjectsCollide(collidableRadialGameObject1, collidableRadialGameObject2)) {

                    // Wenn ein Spieler mit dem Ball kollidiert, dann setze das Attribut lastBallContact des Spielers
                    if ((collidableRadialGameObject1 instanceof BallGameObject)
                            && (collidableRadialGameObject2 instanceof PlayerGameObject)) {
                        ((BallGameObject) collidableRadialGameObject1)
                                .setBallContact((PlayerGameObject) collidableRadialGameObject2);
                    }
                    if ((collidableRadialGameObject2 instanceof BallGameObject)
                            && (collidableRadialGameObject1 instanceof PlayerGameObject)) {
                        ((BallGameObject) collidableRadialGameObject2)
                                .setBallContact((PlayerGameObject) collidableRadialGameObject1);
                    }

                    // Kollisionsberechnung
                    PhysicsHandleCollisions.applyCollision(collidableRadialGameObject1, collidableRadialGameObject2,
                            PhysicsHandleCollisions.getMassRatio(collidableRadialGameObject1,
                                    collidableRadialGameObject2));

                    // Setze den Kollisions-Geschwindigkeitsverlust beider Objekte
                    PhysicsHandleCollisions.looseCollisionVelocities(collidableRadialGameObject1,
                            collidableRadialGameObject2);
                    PhysicsHandleCollisions.looseCollisionVelocities(collidableRadialGameObject2,
                            collidableRadialGameObject1);
                }
            }
        }

    }
}
