
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.io.File;

/**
 * Creates a new dialog so that the user can record a new entry
 *
 * @author Amir Raminfar
 */
public class InsertNewApplication extends JDialog implements RecordingInterface {
    private JPanel wf;
    private JButton play;
    private CaptureSound captureSound;
    private JLabel appname;

    /**
     * Create the audio waveform and paints to the JPanel
     */
    public void updateView() {
        wf.repaint();
        repaint();
        play.setEnabled(true);
    }

    /**
     * Creates a dialog with parent
     *
     * @param parent Parent Window
     */
    public InsertNewApplication(final UserWin parent) {
        super(parent, "Insert New Item", true);
        setResizable(false);
        captureSound = new CaptureSound(this);
        captureSound.clearAudioBytes();
        JPanel panel = new JPanel();
        wf = new JPanel();
        wf.setPreferredSize(new Dimension(400, 100));
        wf.setBackground(Color.white);
        wf.setBorder(new LineBorder(Color.black));
        panel.setLayout(new BorderLayout());
        //Record button
        JButton b1 = new JButton("Record", new ImageIcon("images/record.png"));
        b1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                captureSound.start();
            }
        });
        //Preview button
        play = new JButton("Preview", new ImageIcon("images/play.png"));
        play.setEnabled(false);
        play.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new Playback(captureSound.getInputStream(), captureSound.getFormat());
            }
        });

        JPanel np = new JPanel();
        np.setBorder(new TitledBorder(new EtchedBorder(), "Record a Phrase", TitledBorder.RIGHT, TitledBorder.DEFAULT_POSITION));
        np.setLayout(new BoxLayout(np, BoxLayout.Y_AXIS));
        np.add(wf);
        JPanel bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.X_AXIS));
        bp.add(b1);
        bp.add(Box.createRigidArea(new Dimension(10, 0)));
        bp.add(play);
        np.add(Box.createRigidArea(new Dimension(0, 10)));
        np.add(bp);
        panel.add(np, BorderLayout.NORTH);
        JPanel sp = new JPanel();
        sp.setLayout(new BorderLayout());
        //Browse for application
        sp.setBorder(new TitledBorder(new EtchedBorder(), "Select an Application", TitledBorder.RIGHT, TitledBorder.DEFAULT_POSITION));
        appname = new JLabel("No application selected               ");
        appname.setPreferredSize(new Dimension(300, appname.getSize().height));
        appname.setBackground(Color.WHITE);
        appname.setOpaque(true);
        appname.setBorder(new CompoundBorder(new LineBorder(Color.black), new EmptyBorder(3, 3, 3, 3)));
        sp.add(appname, BorderLayout.WEST);
        JButton browse = new JButton("Browse...");
        //Show file chooser
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JFileChooser fchooser = new JFileChooser();
                fchooser.setFileFilter(new AppFileFilter());
                if (JFileChooser.APPROVE_OPTION == fchooser.showOpenDialog(null)) {
                    appname.setText(fchooser.getSelectedFile().toString());
                    //set icon
                    if (new File(appname.getText()).exists())
                        appname.setIcon(fchooser.getIcon(new File(appname.getText())));
                }

            }
        });
        sp.add(browse, BorderLayout.EAST);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(sp, BorderLayout.NORTH);
        JButton done = new JButton("Insert Into Library");
        done.addActionListener(new java.awt.event.ActionListener() {
            //Check for correct data
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (!play.isEnabled()) {
                    JOptionPane.showMessageDialog(null, "Please record a sample.", "No Sample Recorded", JOptionPane.ERROR_MESSAGE);
                    return;
                } else if (!appname.getText().toLowerCase().contains(".exe") || !new File(appname.getText().toLowerCase()).isFile()) {
                    JOptionPane.showMessageDialog(null, "Please choose an executable file.", "No Application Selected", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Library lib = new Library();
                String wavFile = new File(appname.getText()).getName();
                if (wavFile.toLowerCase().indexOf(".exe") != -1) {
                    wavFile = wavFile.toLowerCase().substring(0, wavFile.toLowerCase().lastIndexOf(".exe"));
                }
                wavFile = new UserOptions().getProp("WAVFolderPath").concat(wavFile).concat(".wav");
                lib.addToLibrary(new WAVFeature(captureSound.getAudioBytes(), 100, appname.getText(), wavFile));
                lib.saveLibraryToDisk();
                SaveWavFile.SaveToFile(captureSound.getInputStream(), wavFile);
                setVisible(false);
                parent.libraryModel.updateData();
                dispose();
            }
        });
        JPanel temp = new JPanel();
        temp.add(Box.createRigidArea(new Dimension(0, 30)));
        temp.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setVisible(false);
                dispose();
            }
        });
        temp.add(done);
        temp.add(cancel);

        bottomPanel.add(temp, BorderLayout.SOUTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        JPanel fpanel = new JPanel();
        fpanel.add(panel);
        fpanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(fpanel);
        pack();
        //Center Window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), Math.max(0, (screenSize.height - windowSize.height) / 2));
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Paints the window
     *
     * @param g Color of the monitor
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (captureSound.getAudioBytes() != null)
            createWaveForm();
    }

    /**
     * Creates the waveform for the audio which was captured
     */
    private void createWaveForm() {
        byte[] audioBytes = captureSound.getAudioBytes();

        if (audioBytes != null && wf.getGraphics() != null) {
            Graphics2D g2 = (Graphics2D) wf.getGraphics();
            g2.setColor(Color.red);
            int[] audioData = null;
            int nlengthInSamples = audioBytes.length / 2;
            audioData = new int[nlengthInSamples];
            int maxAudio = 0;

            for (int i = 0; i < nlengthInSamples; i++) {
                int MSB = (int) audioBytes[2 * i];
                int LSB = (int) audioBytes[2 * i + 1];
                audioData[i] = MSB << 8 | 255 & LSB;
                if (Math.abs(audioData[i]) > maxAudio)
                    maxAudio = Math.abs(audioData[i]);
            }
            audioData = WAVFeature.removeDC(audioData);

            maxAudio = (int) ((float) maxAudio * 1.1);
            Dimension d = wf.getSize();
            int w = d.width;
            int h = d.height;
            int inc = audioData.length / w;
            int halfofheight = (int) h / 2;
            int y = halfofheight;
            //Graph data
            for (int i = 1; i < w - 2; i++) {
                g2.drawLine(i, y, i + 1, (int) (halfofheight - halfofheight * (float) audioData[i * inc] / (float) maxAudio));
                y = (int) (halfofheight - halfofheight * (float) audioData[i * inc] / (float) maxAudio);
            }
        }
    }
}
