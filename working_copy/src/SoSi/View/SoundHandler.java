package SoSi.View;

import java.util.Observable;
import java.util.Observer;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import SoSi.Model.PlaybackHandler;
import SoSi.Model.SoccerUpdateEvent;

/**
 * Die Klasse dient zur Augabe von Sounds. Diese werden bei speziellen Spielereignissen (wie z.B. Foul, Tor, Anstoß oder
 * Spielende) wiedergegeben.
 */
public class SoundHandler implements Observer {

    private static final String FOUL_FILEPATH = "Foul.wav";
    private static final String GOAL_FILEPATH = "Applaus.wav";
    private static final String KICK_FILEPATH = "Kick1.wav";
    private static final String HALFTIME_FILEPATH = "Halbzeit.wav";
    private static final String END_FILEPATH = "Abpfiff.wav";

    private final Clip foulClip;
    private final Clip goalClip;
    private final Clip kickClip;
    private final Clip halfTimeClip;
    private final Clip endClip;

    private static final String ERROR_MESSAGE = "Error while loading soundfiles %s (%s)!\n" + "Details: %s\n";

    /**
     * Flag, ob Sounds ausgegeben werden sollen
     */
    private boolean isEnabled = false;

    /**
     * Erstellt eine neue Instanz der Klasse. Um auf Ereignisse reagieren zu können, registriert sie sich beim
     * {@link PlaybackHandler} als Observer. <br>
     * Damit die Klasse beginnt, Sounds auszugeben, ist ein Aufruf von setEnabled(true) erforderlich.
     * 
     * @param playbackHandler
     *            Referenz auf {@link PlaybackHandler}.
     */
    public SoundHandler(PlaybackHandler playbackHandler) {
        playbackHandler.addObserver(this);

        this.foulClip = loadFile(FOUL_FILEPATH);
        this.goalClip = loadFile(GOAL_FILEPATH);
        this.kickClip = loadFile(KICK_FILEPATH);
        this.halfTimeClip = loadFile(HALFTIME_FILEPATH);
        this.endClip = loadFile(END_FILEPATH);
    }

    /**
     * Aktiviert und deaktiviert die Soundausgabe.
     * 
     * @param enabled
     *            Booleanflag, ob die Soundausgabe aktiviert oder deaktiviert werden soll. (true für aktiviert, false
     *            für deaktiviert.
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (isEnabled) {
            try {
                switch ((SoccerUpdateEvent) arg) {
                case FOUL_OFF:
                case FOUL_OFFSIDE:
                case FOUL_TACKLING:
                    playClip(foulClip);
                    break;

                case PLAYBACK_END_REACHED:
                    playClip(endClip);
                    break;

                case HALFTIME:
                    playClip(halfTimeClip);
                    break;

                case GOAL_SCORED:
                    playClip(halfTimeClip);
                    playClip(goalClip);
                    break;

                case KICK_OFF:
                case FREE_KICK:
                    playClip(kickClip);
                    break;

                default:
                    break;
                }
            } catch (Exception e) {
                System.err.println("Error while playing some soundfiles.");
            }
        }
    }

    /**
     * Spielt einen Clip ab. Dabei wird überpürft, ob dieser nicht bereits wiedergegeben wird und die Wiedergabeposition
     * wird an den Anfang gesetzt.<br>
     * Ist der übergebene Parameter clipToPlay null, so erfolgt keine Wiedergabe.
     * 
     * @param clipToPlay
     *            Abzuspielende Clip-Instanz.
     */
    private static void playClip(Clip clipToPlay) {
        // if (clipToPlay != null) {
        // System.out.println(clipToPlay.toString());
        // System.out.println("active: " + clipToPlay.isActive());
        // System.out.println("running: " + clipToPlay.isRunning());
        // } else {
        // System.out.println("Clip is null");
        // }

        if (clipToPlay != null && !clipToPlay.isRunning()) {
            clipToPlay.stop();
            clipToPlay.setFramePosition(0);
            clipToPlay.start();
        }
    }

    /**
     * Lädt eine Sounddatei und gibt, falls erfolgreich, die entsprechende {@link Clip}-Instanz zurück
     * 
     * @param filename
     *            Dateiname der zu ladenden Sounddatei
     * @return Bei Erfolg die geladene Sounddatei als {@link Clip}-Instanz, ansonsten bei einem Fehler <code>null</code>
     */
    private Clip loadFile(String filename) {
        Clip clip = null;
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResource(
                    "/resources/sounds/" + filename));
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioInputStream);
        } catch (LineUnavailableException ex) {
            System.err.println(String.format(ERROR_MESSAGE, filename, "LineUnavailable", ex.toString()));
            if (ex.getCause() != null) {
                System.err.println("The cause was: " + ex.getCause().getMessage());
            }
            // ex.printStackTrace();
        } catch (IOException ex) {
            System.err.println(String.format(ERROR_MESSAGE, filename, "IOExcepion", ex.toString()));
            if (ex.getCause() != null) {
                System.err.println("The cause was: " + ex.getCause().getMessage());
            }
            // ex.printStackTrace();
        } catch (UnsupportedAudioFileException ex) {
            System.err.println(String.format(ERROR_MESSAGE, filename, "Unsupported audiofile", ex.toString()));
            if (ex.getCause() != null) {
                System.err.println("The cause was: " + ex.getCause().getMessage());
            }
            // ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            System.err.println(String.format(ERROR_MESSAGE, filename, "Maybe no audiocard installed?", ex.toString()));
            if (ex.getCause() != null) {
                System.err.println("The cause was: " + ex.getCause().getMessage());
            }
            // ex.printStackTrace();
        }
        return clip;
    }
}
