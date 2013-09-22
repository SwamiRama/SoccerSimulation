package SoSi.View;

public class FpsHelper {

    /**
     * Letzter Ermittelter FPS-Wert
     */
    int frameRate = 0;

    /**
     * Aktiver Frames-Counter
     */
    int frameCounter = 0;

    /**
     * Seit letztem Frame verstrichene Zeit
     */
    double elapsedTime = 0;

    /**
     * Messzeit-Schwellwert
     */
    double calculationThreshold;
    
    /**
     * Zeitpunkt des letzten Aufrufs von NextFrame()
     */
    double lastCallTime;
    
    long fpsSum = 0;
    int fpsCount = 0;
    double fpsAverage = 0;

    /**
     * FPS-Messung mit Messzeit-Schwellwert von einer Sekunde anlegen
     */
    public FpsHelper() {
        this(null);
    }

    /**
     * FPS-Messung mit eigenem Messzeit-Schwellwert anlegen
     * 
     * @param calculationThreshold
     *            Mindest-Messzeit, über die Frame-Messung stattfinden soll
     */
    public FpsHelper(Double calculationThreshold) {
        if (calculationThreshold != null)
            this.calculationThreshold = calculationThreshold.doubleValue();
        else
            this.calculationThreshold = 1;
        
        this.lastCallTime = System.currentTimeMillis() / 1000d;
    }

    /**
     * Trigger für neuen gezeichneten Frame / Berechnungsschritt
     */
    public void nextFrame() {
        frameCounter++;
        
        elapsedTime += System.currentTimeMillis() / 1000d - this.lastCallTime;
        this.lastCallTime = System.currentTimeMillis() / 1000d;

        if (elapsedTime > calculationThreshold) {
            elapsedTime -= calculationThreshold;
            frameRate = (int) (frameCounter / calculationThreshold);
            frameCounter = 0;
            
            fpsCount++;
            fpsSum+=frameRate;
            fpsAverage = fpsSum / (double)fpsCount;
        }
    }
    
    public int getFps() {
        return frameRate;
    }
    
    public double getAverageFps() {
        return fpsAverage;
    }

}
