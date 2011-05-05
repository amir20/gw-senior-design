
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

/**
 * Library Class is responsible for all communications between the application and the library on disk
 * All features are saved as a vector.
 *
 * @author Amir Raminfar
 */
public class Library implements Serializable {
    private static final long serialVersionUID = 7526471155622776147L;
    private Vector<WAVFeature> libFeatures;
    private String file;

    /**
     * Creates a new library object with default library content
     */
    public Library() {
        this(null);
    }

    /**
     * If file is null then default Library Path is used to create the library content
     *
     * @param file
     */
    public Library(String file) {
        this.file = file;
        if (file == null) {
            if (!(new File(new UserOptions().getProp("LibraryPath")).exists())) {
                libFeatures = new Vector<WAVFeature>();
            } else {
                try {
                    //Read library file
                    FileInputStream fis = new FileInputStream(new UserOptions().getProp("LibraryPath"));
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    Library temp = (Library) ois.readObject();
                    this.libFeatures = temp.libFeatures;
                    ois.close();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (!(new File(file).exists())) {
                libFeatures = new Vector<WAVFeature>();
            } else {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    Library temp = (Library) ois.readObject();
                    this.libFeatures = temp.libFeatures;
                    ois.close();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Removes item at index i and returns the removed item.
     *
     * @param i index
     */
    public WAVFeature remove(int i) {
        return libFeatures.remove(i);
    }

    /**
     * Replaces the application string at index i with String s
     *
     * @param i index
     * @param s Application Address
     */
    public void replaceApplication(int i, String s) {
        WAVFeature temp = libFeatures.get(i);
        temp.setApp(s);
        libFeatures.setElementAt(temp, i);
    }

    /**
     * Saves library to disk
     */
    public void saveLibraryToDisk() {
        if (file == null) {
            try {
                //Save to disk
                FileOutputStream fileOut = new FileOutputStream(new UserOptions().getProp("LibraryPath"));
                ObjectOutputStream output = new ObjectOutputStream(fileOut);
                output.writeObject(this);
                output.flush();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                FileOutputStream fileOut = new FileOutputStream(file);
                ObjectOutputStream output = new ObjectOutputStream(fileOut);
                output.writeObject(this);
                output.flush();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Clears user statistics
     */
    public void removeStats() {
        Vector<WAVFeature> tempLib = (Vector<WAVFeature>) libFeatures.clone();
        libFeatures.clear();
        for (WAVFeature item : tempLib) {
            // Clear stats
            item.setSuccess(0);
            item.setTotal(0);
            libFeatures.add(item);
        }
    }

    /**
     * Returns the library vector.
     */
    public Vector<WAVFeature> getLibrary() {
        return libFeatures;
    }

    /**
     * Adds wf to the library. It does not save it to disk.
     *
     * @param wf WAVFeature
     */
    public void addToLibrary(WAVFeature wf) {
        libFeatures.add(wf);
    }

    /**
     * Gets item at index i
     *
     * @param i index
     */
    public WAVFeature get(int i) {
        return libFeatures.get(i);
    }

    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        aInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
    }
}
