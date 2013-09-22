package SoSi.ControllerPkg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import SoSi.Model.PlaybackHandler;

/**
 * Abstrakte Klasse, welche ActionListener implementiert und eine Referenzen auf PlaybackHandler besitzt. Die Klasse
 * wird bei einer entsprechenden Interaktion des Users aufgerufen, bei welcher eine Referenz auf PlaybackHandler
 * notwendig ist, da Änderungen an den Wiedergabeoptionen vorzunehmen sind.
 */
public abstract class ControllerAction implements ActionListener {

	/**
	 * Referenz auf {@link PlaybackHandler}. Dessen Methoden werden aufgerufen, wenn Änderungen an der Wiedergabe bzw.
	 * der Simulation vorgenommen werden sollen.
	 */
	protected PlaybackHandler playbackHandler;

	/**
	 * Erstellt eine Abstrakte ActionListener Instanz, welche eine Referenz auf PlaybackHandler besitzt.
	 * 
	 * @param playbackHandler
	 *            Referenz auf den PlaybackHandler.
	 */
	public ControllerAction(PlaybackHandler playbackHandler) {
		this.playbackHandler = playbackHandler;
	}

	/**
	 * Wird bei einer entsprechenden Interaktion des Benutzers ausgeführt.
	 */
	public abstract void actionPerformed(ActionEvent e);

}
