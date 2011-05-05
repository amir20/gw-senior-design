
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;

/**
 * Launches applications
 *
 * @author Amir Raminfar
 */
public class LaunchApplication {
    /**
     * Launches application with given filename
     *
     * @param str Application to launch
     */
    LaunchApplication(String str) {
        File app = new File(str);
        if (app.exists()) {
            try {
                Runtime.getRuntime().exec(app.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            // Application no longer exists
            JFrame f = new JFrame();
            f.setAlwaysOnTop(true);
            JOptionPane.showMessageDialog(f, str + " does not exist.", "", JOptionPane.ERROR_MESSAGE);
        }
    }
}
