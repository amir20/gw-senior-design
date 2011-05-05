
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;

/**
 * Static method which writes the audioInputStream to file
 * @author Amir Raminfar      
 */
public class SaveWavFile {
    /**
     * Write the AudioInputStream to a WAV file with given filename
     *
     * @param audioInputStream Stream Containing the WAV File
     * @param fileName         Filename to save
     */
    public static boolean SaveToFile(AudioInputStream audioInputStream, String fileName) {
        if (audioInputStream == null || fileName == null) {
            return false;
        }

        try {
            audioInputStream.reset();
        } catch (Exception ex) {
            System.out.println(ex);
            return false;
        }

        File file = new File(fileName);

        try {
            if (AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file) == -1) {
                throw new IOException("Problems writing to file");
            }
        } catch (Exception ex) {
            System.out.println(ex);
            return false;
        }

        return true;
    }
}
