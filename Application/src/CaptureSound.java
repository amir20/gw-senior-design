
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * This class captures sound for two seconds and allows the application to retrieve it.
 *
 * @author Amir Raminfar
 */
public class CaptureSound implements Runnable {
    private TargetDataLine line;
    private Thread thread;
    private AudioInputStream audioInputStream = null;
    private AudioFormat format = null;
    private byte[] audioBytes = null;
    private RecordingInterface window;


    /**
     * Creates a new instance of CaptureSound
     * The object that creates an instance of CaptureSound must implement RecodringInterface.
     * Once the recording is finished, the updateView located within the window is called
     *
     * @param window window which is recording sound
     */
    public CaptureSound(RecordingInterface window) {
        audioInputStream = null;
        audioBytes = null;
        line = null;
        thread = null;
        this.window = window;
    }

    /**
     * Clears audio bytes by setting it to null
     */
    public void clearAudioBytes() {
        audioBytes = null;
    }


    /**
     * Start the thread for recording
     */
    public void start() {
        if (thread == null) {
            audioBytes = null;
            audioInputStream = null;
            thread = new Thread(this, "Capture Sound");
            thread.start();
        }
    }

    /**
     * Return the instance of AudioInputStream
     */
    public AudioInputStream getInputStream() {
        return audioInputStream;
    }

    /**
     * Returns an array of bytes
     */
    public byte[] getAudioBytes() {
        return audioBytes;
    }

    /**
     * Shutdowns the thread
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
     * Threads proccessing function
     */
    public void run() {
        format = getFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            JOptionPane.showMessageDialog(null, "Line matching " + info + " not supported.", "Microphone not found", JOptionPane.ERROR_MESSAGE);
            shutDown("Line matching " + info + " not supported.");
            return;
        }
        RecordingPopup monitor = new RecordingPopup(2000);

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
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int frameSizeInBytes = format.getFrameSize();
        int bufferLengthInFrames = line.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead;

        line.start();
        while (line.getMicrosecondPosition() / 1000 < 2000) {
            if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                break;
            }
            //Write data to out stream
            out.write(data, 0, numBytesRead);
            String s = String.format("%.3f", ((float) (line.getMicrosecondPosition() / 1000) / 1000)) + " seconds";
            monitor.setText(s);
            monitor.setProgress((int) (line.getMicrosecondPosition() / 1000));
        }
        monitor.close();
        line.stop();
        line.close();
        line = null;

        try {
            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //Save bytes
        audioBytes = out.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
        audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);
        shutDown(null);
        window.updateView();
    }
}
