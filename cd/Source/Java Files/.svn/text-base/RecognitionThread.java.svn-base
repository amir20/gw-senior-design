
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * This class initilizes a thread to record the users phrase and then recoginizing it
 *
 * @author Amir Raminfar
 */
public class RecognitionThread implements Runnable {
    private TargetDataLine line;
    private Thread thread;
    private AudioFormat format = null;
    private byte[] audioBytes = null;
    private UserWin parent;


    /**
     * Creates a new instance of CaptureSound
     *
     * @param parent owner
     */
    public RecognitionThread(UserWin parent) {
        this.parent = parent;
        line = null;
        thread = null;
        start();
    }


    /**
     * Start the thread for recording
     */
    public void start() {
        if (thread == null) {
            audioBytes = null;
            thread = new Thread(this, "Recognition Thread");
            thread.start();
        }
    }


    /**
     * Returns an array of bytes
     */
    public byte[] getAudioBytes() {
        return audioBytes;
    }

    /**
     * Shutdowns the thread
     *
     * @param wasrunning if true then starts the live monitor
     * @param msg        message to console
     */
    public void shutDown(boolean wasrunning, String msg) {
        if (thread != null) {
            if (msg != null) {
                System.out.println(msg);
            }
        }
        thread = null;
        if (wasrunning)
            parent.continiousRecording.start();
        parent.ti.setIcon(new ImageIcon("images/mic.png"));
    }

    /**
     * Returns the AudioFormat which the WAV file was recorded in
     */
    public AudioFormat getFormat() {
        return new AudioFormat(16000, 16, 1, true, true);
    }

    /**
     * Starts recording the users voice.
     */
    public void run() {
        parent.ti.setIcon(new ImageIcon("images/animatedmic.gif"));
        //Save the state of live monitor
        boolean wasrunning = parent.continiousRecording.isRunning();
        if (wasrunning)
            parent.continiousRecording.shutDown(null);
        format = getFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            JOptionPane.showMessageDialog(null, "Line matching " + info + " not supported.", "Microphone not found", JOptionPane.ERROR_MESSAGE);
            shutDown(wasrunning, "Line matching " + info + " not supported.");
            return;
        }
        //Display monitor for recording
        RecordingPopup monitor = new RecordingPopup(2000);

        //Open line
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, line.getBufferSize());
        } catch (LineUnavailableException ex) {
            shutDown(wasrunning, "Unable to open the line: " + ex);
            return;
        } catch (SecurityException ex) {
            shutDown(wasrunning, ex.toString());
            return;
        } catch (Exception ex) {
            shutDown(wasrunning, ex.toString());
            return;
        }

        //set up stream
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
            //Record
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

        //Convert to bytes
        audioBytes = out.toByteArray();

        WAVFeature samplef = new WAVFeature(this.getAudioBytes(), 100);
        Library tempLib = new Library();

        WAVFeature tempWAV;
        tempWAV = new Recognize(tempLib, samplef).getApplication();
        int i = 0;

        //Find top 3 matches
        while (i < 3 && tempWAV != null) {
            int reply = new ConfirmationDialog(tempWAV).getConfirmed();
            if (reply == ConfirmationDialog.CANCEL) {
                shutDown(wasrunning, null);
                return;
            } else if (reply == ConfirmationDialog.YES) {
                break;
            }
            // if not a match then remove this application and search again
            tempLib.remove(tempLib.getLibrary().indexOf(tempWAV));
            tempWAV = new Recognize(tempLib, samplef).getApplication();
            i++;
        }
        if (!(i < 3) || tempWAV == null) {
            shutDown(wasrunning, null);
            return;
        }
        // Launch the application
        new LaunchApplication(tempWAV.getApp());
        Library lib = new Library();
        int index = lib.getLibrary().indexOf(tempWAV);
        tempWAV.setLastUsed();
        tempWAV.setSuccess(tempWAV.getSuccess() + 1);
        lib.getLibrary().setElementAt(tempWAV, index);
        lib.saveLibraryToDisk();
        shutDown(wasrunning, null);
    }
}
