
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

/**
 * Extracts features from audio Byes
 *
 * @author Amir Raminfar
 */
public class WAVFeature implements Serializable {
    private static final long serialVersionUID = 7526471155622776147L;
    private int[] features;
    private String applicationAddress;
    private Hashtable statistics;

    /**
     * Creates features from audio Byes with feature size, application string, and wav file as its properties
     *
     * @param audioBytes   Audio Bytes
     * @param feature_size Number of features that should be extracted
     * @param app          Application Name
     * @param wav          WAV File
     */
    public WAVFeature(byte[] audioBytes, int feature_size, String app, String wav) {
        this(audioBytes, feature_size);
        applicationAddress = app;
        statistics = new Hashtable();
        statistics.put("totalCalls", 0);
        statistics.put("successCalls", 0);
        statistics.put("dateCreated", new SimpleDateFormat("MMMM dd, yyyy h:mm aaa").format(new Date()));
        statistics.put("lastUsed", "n/a");
        setWAVFile(wav);
    }

    /**
     * Creates features for recognizing
     *
     * @param audioBytes   Audio Bytes
     * @param feature_size Number of Features
     */
    public WAVFeature(byte[] audioBytes, int feature_size) {
        int[] audioData = null;
        int nlengthInSamples = audioBytes.length / 2;
        audioData = new int[nlengthInSamples];
        for (int i = 0; i < nlengthInSamples; i++) {
            int MSB = (int) audioBytes[2 * i];
            int LSB = (int) audioBytes[2 * i + 1];
            //convert to 16bit
            audioData[i] = MSB << 8 | 255 & LSB;
        }
        int interval = 32000 / feature_size;
        features = new int[feature_size];
        for (int i = 0; i < features.length; i++)
            features[i] = 0;
        int temp = audioData[0];
        if (audioData.length > 32000) {
            System.arraycopy(audioData, 0, audioData, 0, 31999);
        }

        //remove background noise
        audioData = removeDC(audioData);


        for (int i = 1; i < audioData.length; i++) {
            if (i / interval > feature_size)
                break;
            // find zero crossing
            if ((temp > 0 && audioData[i] < 0) || (temp < 0 && audioData[i] > 0)) {

                features[i / interval]++;
            }
            temp = audioData[i];
        }
    }

    /**
     * Removes background noise
     *
     * @param audioData Removes Audio background noise
     */
    public static int[] removeDC(int[] audioData) {
        int maxDC = 0;
        for (int i = 0; i < 1000; i++)
            if (maxDC < Math.abs(audioData[i]))
                maxDC = Math.abs(audioData[i]);

        for (int i = 0; i < audioData.length; i++)
            if (maxDC * 1.5 > Math.abs(audioData[i]))
                audioData[i] = 0;
            else
                break;

        for (int i = audioData.length - 1; i > audioData.length / 2; i--)
            if (maxDC * 1.5 > Math.abs(audioData[i]))
                audioData[i] = 0;
            else
                break;

        return audioData;
    }

    /**
     * Gets item features
     */
    public int[] getFeatures() {
        return features;
    }

    /**
     * Gets application filename
     */
    public String getApp() {
        return applicationAddress;
    }

    /**
     * Sets s to application string
     *
     * @param s String
     */
    public void setApp(String s) {
        applicationAddress = s;
    }

    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        aInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
    }

    /**
     * Get the number of times that the application has been recognized
     */
    public int getTotal() {
        return ((Integer) statistics.get("totalCalls")).intValue();
    }

    /**
     * Gets the number of times that the application has been recognized and the user has clicked yes
     */
    public int getSuccess() {
        return ((Integer) statistics.get("successCalls")).intValue();
    }

    /**
     * Sets total
     *
     * @param i number
     */
    public void setTotal(int i) {
        statistics.put("totalCalls", i);
    }

    /**
     * Sets success
     *
     * @param i number
     */
    public void setSuccess(int i) {
        statistics.put("successCalls", i);
    }

    /**
     * Gets when this object was created
     */
    public String getDateCreated() {
        return (String) statistics.get("dateCreated");
    }

    /**
     * Gets when this object was last used
     */
    public String getLastUsed() {
        return (String) statistics.get("lastUsed");
    }

    /**
     * Sets last used to now
     */
    public void setLastUsed() {
        statistics.put("lastUsed", new SimpleDateFormat("MMMM dd, yyyy h:mm aaa").format(new Date()));
    }

    /**
     * Sets WAV file to s
     *
     * @param s String
     */
    public void setWAVFile(String s) {
        statistics.put("WAVFile", s);
    }

    /**
     * Gets WAV file string
     */
    public String getWAVFile() {
        return (String) statistics.get("WAVFile");
    }

    /**
     * Checks to see if this object is equal to another object
     *
     * @param obj
     */
    public boolean equals(Object obj) {
        WAVFeature otherObj = (WAVFeature) obj;
        return (otherObj.getApp().compareTo(this.getApp()) == 0 && otherObj.getDateCreated().compareTo(this.getDateCreated()) == 0);
    }
}
