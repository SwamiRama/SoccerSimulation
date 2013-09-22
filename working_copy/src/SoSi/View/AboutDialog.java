package SoSi.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Das Fenster, welches die Versionsnummer und Informationen über das Simulationsprogramm enthält.
 */
public class AboutDialog extends JFrame {

	/**
	 * Random generated
	 */
	private static final long serialVersionUID = 1L;

	private final Image backgroundImage;

	/**
	 * Erstellt das Fenster, welches die Versionsnummer und Informationen über das Simulationsprogramm enthält.
	 */
	public AboutDialog() {
		super("Über");

		// Hintergrundgrafik laden
		ImageIcon backroundIcon = new ImageIcon(getClass().getResource("/resources/images/aboutBackground.jpg"));
		this.backgroundImage = backroundIcon.getImage();
		final Dimension imageSize = new Dimension(backgroundImage.getWidth(null), backgroundImage.getHeight(null));

		// Panel zum Zeichnen des Hintergrundbildes
		JPanel backgroundPanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				super.paintComponents(g2d);

				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));

				setPreferredSize(imageSize);
				setMinimumSize(imageSize);
				setMaximumSize(imageSize);
				setSize(imageSize);
				setLayout(null);
				g2d.drawImage(backgroundImage, 0, 0, null);
			}
		};

        JLabel contentLabel = new JLabel("<html><body width='"+backroundIcon.getIconWidth()+"'><b>" +
        		"<p align=\"center\">SoSi - The Soccer Simulator" +
        		"<br></p><hr><div align='center'><p align=\"center\"></b><br>" +
                " Dieses Projekt ist im Rahmen des <br>" +
                " Software Engineering Praktikums <br>" +
                " entstanden. <br>" +
                " Folgende Personen waren <br>" +
                " daran beteiligt:<br><br>" +
                "<font size=\"5\"><i>Bastian Birkeneder, Matthias Cetto, Bernhard Doll, <br>" +
                "Manuel Reischl, Konstantin Riel</i></font><br>" +
                "<font size=\"3\">Passau 2012 - 2013</font><br><br>"  +
                " </div></p></body></html>");
		contentLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));

		// Größe der Komponenten setzen
		contentLabel.setPreferredSize(imageSize);
		backgroundPanel.setPreferredSize(imageSize);

		// Komponenten ineinander hinzufügen
		backgroundPanel.add(contentLabel, BorderLayout.CENTER);
		this.getContentPane().add(backgroundPanel);

		// Farben setzen
		this.setBackground(Color.BLACK);

		this.pack();
		this.setSize(new Dimension(500, 400));
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(false);
	}

}
