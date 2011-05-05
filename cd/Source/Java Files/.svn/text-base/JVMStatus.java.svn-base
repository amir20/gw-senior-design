
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Displays a small dialog which shows the status of the Java Virtual Machine
 *
 * @author Amir Raminfar
 */
public class JVMStatus extends JFrame {
    public JProgressBar bar;
    public JLabel total;
    public JLabel max;

    /**
     * Displays JVM's Satus
     */
    JVMStatus() {
        super("JVC Status");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            //do nothing
        }
        // add progress bar
        bar = new JProgressBar();
        bar.setStringPainted(true);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(200, 150));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(bar);
        //add empty box
        panel.add(Box.createVerticalStrut(10));
        total = new JLabel();
        total.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(total);
        // add max label
        max = new JLabel();
        max.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(max);
        JButton gc = new JButton("Garbage Collector");
        gc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // run garbage collector
                System.gc();
            }
        });
        panel.add(Box.createVerticalStrut(20));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(gc);
        //set size
        right.setPreferredSize(new Dimension(20, 15));
        right.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(right);
        setContentPane(panel);
        new UpdateInfo(this).start();
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pack();
        setLocation(10, 10);
        setVisible(true);
    }
}

/**
 * Thread which updates the status every 1 second
 *
 * @author Amir Raminfar
 */
class UpdateInfo extends Thread {
    private boolean run;
    private JVMStatus jvm;

    /**
     * Creates a new JVM Thread which conitinuosly updates
     *
     * @param jvm Status Window
     */
    UpdateInfo(JVMStatus jvm) {
        this.setDaemon(true);
        run = true;
        this.jvm = jvm;
    }

    /**
     * Updates status and sleeps for one second
     */
    public void run() {
        jvm.bar.setMaximum(100);
        while (run) {
            float f = ((float) Runtime.getRuntime().totalMemory()) / ((float) Runtime.getRuntime().maxMemory());
            int pos = (int) (100.0 * f);
            jvm.bar.setValue(pos);
            jvm.total.setText(Runtime.getRuntime().totalMemory() / 1024 + " KB Used");
            jvm.max.setText(Runtime.getRuntime().maxMemory() / 1024 + " KB Max allocated to JVM");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}