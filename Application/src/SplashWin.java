
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Creates a Splash screen
 *
 * @author Amir Raminfar
 */
public class SplashWin extends JWindow {
    private JProgressBar prog = new JProgressBar();

    /**
     * Creates and display the splash screen with given filename as its image
     *
     * @param filename filename containing the image
     */
    public SplashWin(String filename) {
        // Add picture to window
        JLabel l = new JLabel(new ImageIcon(filename));
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        prog.setString("Please Wait...");
        prog.setStringPainted(true);
        p.add(l, BorderLayout.CENTER);
        //set intermediate to true
        prog.setIndeterminate(true);
        p.add(prog, BorderLayout.PAGE_END);
        getContentPane().add(p);
        pack();
        // center
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = l.getPreferredSize();
        setLocation(screenSize.width / 2 - (labelSize.width / 2), screenSize.height / 2 - (labelSize.height / 2));
        setVisible(true);
    }

    /**
     * Closes the window
     */
    public void close() {
        setVisible(false);
        dispose();
    }

    /**
     * Changes the progressbar text to s
     *
     * @param s
     */
    public void setText(String s) {
        prog.setString(s);
    }
}
