package SoSi.Model.Calculation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import sep.football.AI;

/**
 * Lädt die beiden Team-KIs aus dem Dateisystem. Zusätzlich finden entsprechende Überprüfungen statt, ob es sich bei der
 * angegebenen Datei um eine gültige Java-Class-Datei handelt, welche das AI-Interface implementiert. Des weiteren
 * werden beim Laden der KIs die notwendigen Ausführungsrechte gesetzt, so dass die KIs in ihren erlaubten Aktionen
 * nicht beschränkt werden, jedoch unerlaubte Zugriffe (wie z.B. System.exit() oder Reflection) weitmöglichst verhindert
 * werden.
 */
public final class AILoader {

    private static final String CLASS_FILE_EXTENSION = ".class";

    /**
     * Private-Konstruktor der Klasse, da diese nur static Elemente enthält und eine Instanzierung der Klasse daher
     * nicht gewünscht ist.
     */
    private AILoader() {
    }

    /**
     * Lädt ein JAR-File über den angegebenen Dateipfad. Die geladene Klasse wird als KI für eines der Teams verwendet.<br>
     * Der geladenen KI werden die benötigten Ausführungsrechte zugesichert, jedoch erweiterte Rechte, welche z.B. für
     * System-Aufrufe oder Reflection benötigt werden, entzogen.
     * 
     * @param pathToJarFile
     *            Der Dateipfad der zu ladenden .class-Datei
     * @return Eine Instanz der über den angegebenen Dateipfad geladenen KI-Klasse.
     * @throws AiLoadingException
     *             Wird geworfen, wenn das Laden der KI über den angegebenen Dateipfad fehlschlägt
     * @throws MalformedURLException
     * @see AiLoadingException
     */
    public static AI LoadAI(String pathToJarFile) throws AiLoadingException {

        try {
            File file = new File(pathToJarFile);
            JarFile jarFile = new JarFile(file);
            try {
                URL url = file.toURI().toURL();
                URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { url });
                try {
                    ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
                    ArrayList<AI> aiCandidates = new ArrayList<AI>();

                    // Iterate through all files in the jar file
                    for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                        JarEntry currentEntry = entries.nextElement();
                        String rawString = currentEntry.getName();

                        // Check if it's a class file
                        if (rawString.endsWith(CLASS_FILE_EXTENSION)) {

                            int rawStringLength = rawString.length();
                            int extensionLength = CLASS_FILE_EXTENSION.length();

                            if (rawStringLength > extensionLength) {

                                // Generate full quallified name
                                try {
                                    String fullQuallifiedName = rawString.replace("/", ".").substring(0,
                                            rawStringLength - extensionLength);
                                    classList.add(urlClassLoader.loadClass(fullQuallifiedName));
                                } catch (NoClassDefFoundError e) {
                                    throw new AiLoadingException(
                                            "NoClassDefFound - FQN error.\nMessage: " + e.getMessage(), e);
                                }
                            }
                        }
                    }

                    // Try to initialize new objects
                    for (Class<?> currentClass : classList) {
                        Object currentObject;
                        try {
                            currentObject = currentClass.newInstance();
                            if (currentObject instanceof AI) {
                                aiCandidates.add((AI) currentObject);
                            }
                        } catch (InstantiationException e) {
                            // do nothing
                        } catch (IllegalAccessException e) {
                            // do nothing
                        } catch (ExceptionInInitializerError e) {
                            // do nothing
                        } catch (NoClassDefFoundError e) {
                            // do nothing
                        }
                    }

                    jarFile.close();
                    urlClassLoader.close();
                    if (aiCandidates.size() == 1) {
                        return aiCandidates.get(0);
                    } else if (aiCandidates.size() < 1) {
                        throw new AiLoadingException("Count of AI-Candidates is not 1.\n"
                                + "(Jar-file contains no Class-file which implements interface \"AI\")\n\n" + "File: "
                                + file.getName());
                    } else {
                        // Generate String with all found AI-Candidates
                        StringBuilder aiCandidatesNames = new StringBuilder();
                        for (AI ai : aiCandidates) {
                            if (aiCandidatesNames.length() != 0) {
                                aiCandidatesNames.append("\n");
                            }

                            aiCandidatesNames.append(" - ");
                            aiCandidatesNames.append(ai.getClass().getName());
                        }

                        throw new AiLoadingException("Count of AI-Candidates is not 1.\n"
                                + "(Jar-file contains two or more Class-files which implements interface \"AI\"):\n"
                                + aiCandidatesNames.toString() + "\n\n" + "File: " + file.getName());
                    }
                } finally {
                    urlClassLoader.close();
                }
            } finally {
                jarFile.close();
            }

        } catch (IOException e) {
            throw new AiLoadingException("IO-Exception", e);
        } catch (ClassNotFoundException e) {
            throw new AiLoadingException("Class not found", e);
        } catch (AiLoadingException e) {
            throw e;
        } catch (Throwable e) {
            String messageTemplate = "An unknown exception during AI-loading occured. Details: \n"
                    + "Type: %s\nMessage: %s\nFilename: %s";
            throw new AiLoadingException(String.format(messageTemplate, e.getClass().getName(), e.getMessage(),
                    pathToJarFile), e);
        }
    }

    /**
     * Eine Exception, welche geworfen wird, wenn die KI nicht über den angegebenen Dateipfad geladen werden konnte.
     * Dies ist der Fall, wenn die angegebene Datei nicht vorhanden ist, keine Leserechte vorliegen oder der Inhalt
     * keiner gülten Java-Class-Datei entspricht, welche das Interface {@link AI} implementiert.
     */
    public static class AiLoadingException extends Exception {

        /**
         * Random generated.
         */
        private static final long serialVersionUID = -7671681737883487009L;

        public AiLoadingException() {
            super();
        }

        public AiLoadingException(String message) {
            super(message);
        }

        public AiLoadingException(String message, Throwable throwable) {
            super(message, throwable);
        }

    }
}
