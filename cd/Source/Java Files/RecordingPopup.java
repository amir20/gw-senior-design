
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Creates and shows a progress of the recording status
 *
 * @author Amir Raminfar
 */
public class RecordingPopup extends JWindow {
    JProgressBar prog;
    JLabel label;

    /**
     * Display a window with max value for the progress bar
     *
     * @param max
     */
    RecordingPopup(int max) {
        JPanel panel = new JPanel();
        JPanel outp = new JPanel();
        //Add label
        label = new JLabel("Please Wait...");
        //add progress bar
        prog = new JProgressBar(0, max);
        prog.setValue(0);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        // center elements
        prog.setAlignmentX(Component.CENTER_ALIGNMENT);
        prog.setPreferredSize(new Dimension(400, 20));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(prog);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        outp.setBorder(new LineBorder(Color.black));
        outp.add(panel);
        setContentPane(outp);

        pack();
        //Center window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), Math.max(0, (screenSize.height - windowSize.height) / 2));
        setAlwaysOnTop(true);
        setVisible(true);
    }

    /**
     * Closes the window
     */
    public void close() {
        setVisible(false);
    }

    /**
     * Sets the label text with given string
     *
     * @param text String
     */
    public void setText(String text) {
        label.setText(text);
    }

    /**
     * Sets the progress bar value to i
     *
     * @param i progress value
     */
    public void setProgress(int i) {
        prog.setValue(i);
    }

}
