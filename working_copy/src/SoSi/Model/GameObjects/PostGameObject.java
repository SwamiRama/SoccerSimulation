package SoSi.Model.GameObjects;

import sep.football.Position;

/**
 * Beinhaltet allgemeine Daten eines Pfostens in einem Tor.<br>
 * Der Pfosten ist nicht beweglich und hat daher eine fixe Position.
 */
public class PostGameObject extends RadialGameObject {
    
    private static final double POST_DIAMETER = 0.5; 

    /**
     * Erstellt ein Spielobjekt auf dem Feld und setzt es gleich.
     * 
     * @param position
     *            Die Position innerhalb des Spielfeldes.
     */
    public PostGameObject(Position position) {
        super(position, POST_DIAMETER);
    }
      
}
