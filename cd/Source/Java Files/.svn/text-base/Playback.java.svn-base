
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Plays the given audio file or inputstream
 *
 * @author Amir Raminfar
 */
public class Playback implements Runnable {
    private AudioInputStream audioInputStream;
    private SourceDataLine line;
    private Thread thread;
    private AudioFormat format;


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
     * Plays a wav file
     *
     * @param wavFile File to play
     */
    public Playback(File wavFile) {
        AudioInputStream ais = null;
        try {
            ais = AudioSystem.getAudioInputStream(wavFile);
            new Playback(ais, ais.getFormat());
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, wavFile.getAbsolutePath() + " does not exist!", "WAV File Not Found", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays an AudioInputStream with given format
     *
     * @param audioStream AudioInputStream to play
     * @param af          Audioformat of stream
     */
    public Playback(AudioInputStream audioStream, AudioFormat af) {
        this.format = af;
        this.audioInputStream = audioStream;
        if (thread == null && audioStream != null) {
            thread = new Thread(this, "Playback Thread");
            thread.start();
        }
    }

    /**
     * Starts playing the wav file
     */
    public void run() {

        try {
            audioInputStream.reset();
        } catch (Exception e) {

        }

        //Open audio stream
        AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream(format, audioInputStream);

        if (playbackInputStream == null) {
            shutDown("Unable to convert stream of format " + audioInputStream + " to format " + format);
            return;
        }

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            shutDown("Line matching " + info + " not supported.");
            return;
        }

        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, 16384);
        } catch (LineUnavailableException ex) {
            shutDown("Unable to open the line: " + ex);
            return;
        }

        //Setup buffer
        int frameSizeInBytes = format.getFrameSize();
        int bufferLengthInFrames = line.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead = 0;
        line.start();

        while (thread != null) {
            try {
                if ((numBytesRead = playbackInputStream.read(data)) == -1) {
                    break;
                }
                int numBytesRemaining = numBytesRead;
                //Starting playing
                while (numBytesRemaining > 0) {
                    numBytesRemaining -= line.write(data, 0, numBytesRemaining);
                }
            } catch (Exception e) {
                shutDown("Error during playback: " + e);
                break;
            }
        }

        if (thread != null) {
            line.drain();
        }
        line.stop();
        line.close();
        line = null;

        shutDown(null);

    }

}