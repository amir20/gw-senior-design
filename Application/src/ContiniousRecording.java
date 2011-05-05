
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
 * Continious Recording allows the user to initiate the recording phase via voice
 * This class has lower accuracy than expected and should be used with minimal background noise
 *
 * @author Amir Raminfar
 */
public class ContiniousRecording extends JPanel implements Runnable {
    private TargetDataLine line;
    private Thread thread;
    private AudioFormat format = null;
    private CircularArray out;
    private UserWin parent;

    /**
     * Displays a live monitor of what the user said
     *
     * @param c Color to paint the monitor
     */
    private void createWaveForm(Color c) {
        byte[] audioBytes = out.getBytes();

        //Draws Continious Recording Monitor
        if (audioBytes != null && this.getGraphics() != null) {
            Graphics2D g2 = (Graphics2D) this.getGraphics();
            g2.setBackground(Color.white);
            g2.setColor(c);
            Dimension d = this.getSize();
            int w = d.width;
            int h = d.height;
            g2.clearRect(0, 0, d.width, d.height);
            int[] audioData = null;

            int nlengthInSamples = audioBytes.length / 2;
            audioData = new int[nlengthInSamples];


            for (int i = 0; i < nlengthInSamples; i++) {
                int MSB = (int) audioBytes[2 * i];
                int LSB = (int) audioBytes[2 * i + 1];
                audioData[i] = MSB << 8 | 255 & LSB;

            }

            int maxAudio = 36044;
            int inc = audioData.length / w;
            int halfofheight = (int) h / 2;
            int y = halfofheight;
            //Graph the points
            for (int i = 1; i < w - 2; i++) {
                g2.drawLine(i, y, i + 1, (int) (halfofheight - halfofheight * (float) audioData[i * inc] / (float) maxAudio));
                y = (int) (halfofheight - halfofheight * (float) audioData[i * inc] / (float) maxAudio);
            }
        }
    }

    /**
     * Creates a new instance of ContiniousRecording
     *
     * @param parent owner of this component
     */
    public ContiniousRecording(UserWin parent) {
        this.setPreferredSize(new Dimension(100, 20));
        this.setBackground(Color.white);
        line = null;
        thread = null;
        this.parent = parent;
    }

    /**
     * Start the thread for recording
     */
    public void start() {
        if (thread == null) {
            thread = new Thread(this, "Continious Recording");
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
     * Proccesses live sound
     */
    public void run() {
        format = getFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

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

        //Setup circular array
        out = new CircularArray(32000);
        int frameSizeInBytes = format.getFrameSize();
        int bufferLengthInFrames = line.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead;

        line.start();
        while (thread != null) {
            if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                break;
            }
            out.write(data, numBytesRead);
            // Try to recognize the word
            if (new Recognize(new Library(new UserOptions().getProp("ComputerPhrase")), new WAVFeature(out.getBytes(), 100)).getShortestPath() < 185) {
                createWaveForm(Color.green);
                new RecognitionThread(parent);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Clear
                out.clear();
            }

            createWaveForm(Color.red);
        }

        line.stop();
        line.close();
        line = null;
        shutDown(null);


    }

    /**
     * returns true if this thread is live
     */
    public boolean isRunning() {
        return thread != null;
    }
}

/**
 * Circular array used for continious recording
 *
 * @author Amir Raminfar
 */
class CircularArray {
    private byte[] bytes;
    private int header;
    private final int size;

    /**
     * Create a Circular Array with the given size
     *
     * @param size size of the circular array
     */
    CircularArray(final int size) {
        bytes = new byte[size];
        this.size = size;
        header = 0;
    }

    /**
     * Appends b to this circular array
     *
     * @param b     bytes
     * @param bsize size of bytes
     */
    public void write(byte[] b, int bsize) {
        for (int i = 0; i < bsize; i++) {
            bytes[header] = b[i];
            header = (header + 1) % size;
        }
    }

    /**
     * Flattens the circular array and returns it
     */
    public byte[] getBytes() {
        byte[] result = new byte[size];
        for (int i = 0; i < size; i++) {
            int pointer = (header + i) % size;
            result[i] = bytes[pointer];
        }
        return result;
    }

    /**
     * Clears the array and reset the header
     */
    public void clear() {
        for (int i = 0; i < size; i++) {
            bytes[i] = 0;
        }
        header = 0;

    }
}
