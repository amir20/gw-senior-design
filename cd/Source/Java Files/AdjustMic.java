
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * Allows the user to monitor the status of the microphone
 * The monitor is useful when the application is being setup for the first time
 *
 * @author Amir Raminfar
 */
public class AdjustMic extends JPanel implements Runnable {
    private TargetDataLine line;
    private Thread thread;
    private AudioFormat format = null;
    private int g_max = 0;
    private double time;
    private int timetolive = 5000;

    /**
     * Draws the monitor on the panel
     *
     * @param c    Graphic Bar color
     * @param data data that contain the bytes of sound
     */
    private void createWaveForm(Color c, byte[] data) {
        //Create a bar to represent the volume of the mic
        if (this.getGraphics() != null) {
            Graphics2D g2 = (Graphics2D) this.getGraphics();
            g2.setColor(c);
            Dimension d = this.getSize();
            int w = d.width;
            int h = d.height;
            g2.clearRect(0, 0, d.width, d.height);
            int[] audioData = null;
            int nlengthInSamples = data.length / 2;
            audioData = new int[nlengthInSamples];

            // join bytes to create a 16 bit sample
            for (int i = 0; i < nlengthInSamples; i++) {
                int MSB = (int) data[2 * i];
                int LSB = (int) data[2 * i + 1];
                audioData[i] = MSB << 8 | 255 & LSB;
            }

            int vol = 0;
            // Find max value
            for (int i : audioData) {
                if (Math.abs(i) > vol)
                    vol = Math.abs(i);
            }

            //Draw the vertical bar
            int maxAudio = 32500;
            float percentage = (float) vol / (float) maxAudio;
            int pheight = (int) (percentage * (float) h);
            g2.fillRect(0, h - pheight, w, pheight);
            // Setup marker
            if (vol > g_max) {
                g_max = vol;
                time = System.currentTimeMillis();
            }
            if (Math.abs(time - System.currentTimeMillis()) > timetolive) {
                time = System.currentTimeMillis();
                g_max = vol;
            }


            percentage = (float) g_max / (float) maxAudio;
            pheight = (int) (percentage * (float) h);
            // Fade the marker
            g2.setColor(new Color(0, 189, 0, (int) ((double) 255 * ((double) timetolive - Math.abs(time - System.currentTimeMillis())) / (double) timetolive)));
            g2.fillRect(0, h - pheight, w, 4);
        }
    }

    /**
     * Creates a new instance of AdjustMic with c being the background color
     *
     * @param c background color
     */

    public AdjustMic(Color c) {
        this.setPreferredSize(new Dimension(10, 200));
        this.setBackground(c);
        line = null;
        thread = null;
    }

    /**
     * Start the thread for monitoring
     */
    public void start() {
        if (thread == null) {
            thread = new Thread(this, "Monitor Microphone");
            thread.start();
        }
    }

    /**
     * Shutdowns the thread
     *
     * @param msg message to console
     */
    public void shutDown(String msg) {
        if (thread != null) {
            if (msg != null) {
                System.out.println(msg);
            }
        }
        thread = null;
    }

    /**
     * Returns the AudioFormat which the WAV file was recorded in
     */
    public AudioFormat getFormat() {
        return new AudioFormat(16000, 16, 1, true, true);
    }

    /**
     * Function which the thread does its proccessing
     */
    public void run() {
        format = getFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        //Exit thread if line does not support recording
        if (!AudioSystem.isLineSupported(info)) {
            shutDown("Line matching " + info + " not supported.");
            return;
        }

        //Setup line
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, line.getBufferSize());
        } catch (LineUnavailableException ex) {
            shutDown("Unable to open the line: " + ex);
            return;
        } catch (SecurityException ex) {
            shutDown(ex.toString());
            return;
        } catch (Exception ex) {
            shutDown(ex.toString());
            return;
        }


        //Setup buffer
        //Some code was used from java.sun.com for recording sound
        int frameSizeInBytes = format.getFrameSize();
        int bufferLengthInFrames = line.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];

        // Start recording
        line.start();
        while (thread != null) {
            if ((line.read(data, 0, bufferLengthInBytes)) == -1) {
                break;
            }
            createWaveForm(Color.lightGray, data);
        }

        //stop sound
        line.stop();
        line.close();
        line = null;
        shutDown(null);

    }
}

