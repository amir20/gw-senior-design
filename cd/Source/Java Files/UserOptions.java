
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class is responsible for retrieving user settings properties
 *
 * @author Amir Raminfar
 */
public class UserOptions {
    private Properties props;

    /**
     * Creates a new instance of this class by reading the user option's file.
     * If no user options file exists then it will create a file from default values
     */
    UserOptions() {
        if (new File("user/_useroptions").exists() == false) {
            //If properties don't exist then use defaults
            props = new Properties(getDefault());
            saveOptions();
        } else {
            try {
                //Other wise read the file
                props = new Properties(getDefault());
                FileInputStream inputfile = new FileInputStream("user/_useroptions");
                props.load(inputfile);
                inputfile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets property
     *
     * @param s Property name
     */
    public String getProp(String s) {
        return props.getProperty(s);
    }

    /**
     * Gets default properties
     */
    public Properties getDefault() {
        Properties dprops = new Properties();
        dprops.setProperty("LibraryPath", "user/_userlibrary");
        dprops.setProperty("WAVFolderPath", "user/WAVs/");
        dprops.setProperty("ShowConfirmation", "true");
        dprops.setProperty("YesNoLibraryPath", "user/_useryesnolibrary");
        dprops.setProperty("ComputerPhrase", "user/_usercomputer");
        dprops.setProperty("DefaultAction", "show");
        return dprops;
    }

    /**
     * Sets property with key k to value v
     *
     * @param k Key
     * @param v Value
     */
    public void setProp(String k, String v) {
        props.setProperty(k, v);
    }

    /**
     * Saves user options to disk
     */
    public void saveOptions() {
        try {
            //save to disk
            FileOutputStream outputfile = new FileOutputStream("user/_useroptions");
            props.store(outputfile, "User Options");
            outputfile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Loads default values by clearing the user library
     */
    public void loadDefaults() {
        props.clear();
    }
}
