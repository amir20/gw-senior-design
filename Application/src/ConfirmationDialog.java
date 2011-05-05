
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Confirmation Dialog displays a progress bar with yes, no and cancel buttons. The user can say or click yes/no.
 * If the user clicks yes or no before the two seconds is finished it will return immedialty.
 *
 * @author Amir Raminfar
 */
public class ConfirmationDialog extends JWindow implements Runnable {
    static int CANCEL = 2;
    static int YES = 1;
    static int NO = 0;
    private TargetDataLine line;
    private Thread thread;
    private AudioFormat format = null;
    private byte[] audioBytes = null;
    private JProgressBar prog;
    private JLabel label;
    private int confirmation = -1;

    /**
     * creates a new instance of ConfirmationDialog with s being the name of application
     *
     * @param tempWAV WAVFeature which was recognized
     */
    public ConfirmationDialog(WAVFeature tempWAV) {
        //Setup confirmation GUI items
        String s = tempWAV.getApp();
        this.setAlwaysOnTop(true);
        JPanel panel = new JPanel();
        JPanel outp = new JPanel();
        label = new JLabel("Is " + new File(s).getName() + " the correct application to launch? " + "You may click OR say YES/NO...");
        File file = new File(s);
        sun.awt.shell.ShellFolder sf = null;
        //Get Icon
        try {
            sf = sun.awt.shell.ShellFolder.getShellFolder(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Icon icon = new ImageIcon(sf.getIcon(true), sf.getFolderType());
        label.setIcon(icon);
        prog = new JProgressBar(0, 2000);
        prog.setValue(0);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        prog.setAlignmentX(Component.CENTER_ALIGNMENT);
        prog.setPreferredSize(new Dimension(300, 20));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        //Add label and progress bar
        panel.add(label);
        panel.add(prog);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        outp.setBorder(new LineBorder(Color.black));
        outp.add(panel);
        JPanel buttons = new JPanel();
        JButton yes = new JButton("Yes");
        // Setup Yes button
        yes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmation = YES;
            }
        });
        buttons.add(yes);

        //Setup no button
        JButton no = new JButton("No");
        no.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmation = NO;
            }
        });
        buttons.add(no);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmation = CANCEL;
            }
        });
        buttons.add(cancel);
        panel.add(buttons);
        setContentPane(outp);
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), Math.max(0, (screenSize.height - windowSize.height) / 2));
        setAlwaysOnTop(true);
        setVisible(true);
        //Start recording
        try {
            thread = new Thread(this);
            thread.start();
            //Wait until recording is done
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setVisible(false);
    }

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
     * Returns the AudioFormat which the WAV file was recorded in
     */
    public AudioFormat getFormat() {
        return new AudioFormat(16000, 16, 1, true, true);
    }

    /**
     * Proccess sound and recognizes if the user said yes or no
     */
    public void run() {
        format = getFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            shutDown("Line matching " + info + " not supported.");
            return;
        }

        //Setup line
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, line.getBufferSize());
        } catch (LineUnavailableException ex) {
            shutDown("Unable to open the line: " + ex);
            return;
        } catch (SecurityException ex) {
            shutDown(ex.toString());
            return;
        } catch (Exception ex) {
            shutDown(ex.toString());
            return;
        }

        //Setup buffer
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int frameSizeInBytes = format.getFrameSize();
        int bufferLengthInFrames = line.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead;

        line.start();
        while (line.getMicrosecondPosition() / 1000 < 2000) {
            //If no buttons have yet been clicked then record
            if (confirmation != -1)
                return;
            if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                break;
            }
            out.write(data, 0, numBytesRead);
            prog.setValue((int) (line.getMicrosecondPosition() / 1000));
        }

        line.stop();
        line.close();
        line = null;

        try {
            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Conver to bytes
        audioBytes = out.toByteArray();
        shutDown(null);

        //Recognize YES OR NO
        WAVFeature samplef = new WAVFeature(audioBytes, 100);
        Recognize rec = new Recognize(new Library(new UserOptions().getProp("YesNoLibraryPath")), samplef);
        WAVFeature tempWAV = rec.getApplication();

        if (Boolean.valueOf(tempWAV.getApp())) {
            if (confirmation == -1)
                confirmation = YES;
        }

        System.gc();
    }

    /**
     * Returns true if user clicked or said yes otherwise false
     */
    public int getConfirmed() {
        return confirmation;
    }

}
