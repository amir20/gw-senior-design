
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
 * Records the word computer and inserts it to the library
 *
 * @author Amir Raminfar
 */
public class RecordComputer extends JDialog implements RecordingInterface {
    JPanel wf;
    JButton play;
    CaptureSound captureSound;

    /**
     * Create the audio waveform and paints to the JPanel
     */
    public void updateView() {
        wf.repaint();
        repaint();
        play.setEnabled(true);
    }

    /**
     * Create the dialog with parent
     *
     * @param parent
     */
    public RecordComputer(final UserWin parent) {
        super(parent, "Record Computer Phrase", true);
        setResizable(false);
        captureSound = new CaptureSound(this);
        captureSound.clearAudioBytes();
        JPanel panel = new JPanel();
        wf = new JPanel();
        wf.setPreferredSize(new Dimension(400, 100));
        wf.setBackground(Color.white);
        wf.setBorder(new LineBorder(Color.black));
        panel.setLayout(new BorderLayout());
        //Setup buttons for recording computer
        JButton b1 = new JButton("Record", new ImageIcon("images/record.png"));
        b1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                captureSound.start();
            }
        });
        //Play button
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

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        JButton done = new JButton("Insert Into Library");
        done.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (!play.isEnabled()) {
                    JOptionPane.showMessageDialog(null, "Please record a sample.", "No Sample Recorded", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                //Update library
                new File(new UserOptions().getProp("ComputerPhrase")).delete();
                Library lib = new Library(new UserOptions().getProp("ComputerPhrase"));
                lib.addToLibrary(new WAVFeature(captureSound.getAudioBytes(), 100, "computer", "computer.wav"));
                lib.saveLibraryToDisk();
                SaveWavFile.SaveToFile(captureSound.getInputStream(), "computer.wav");
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
        //Add buttons
        temp.add(done);
        temp.add(cancel);

        bottomPanel.add(temp, BorderLayout.SOUTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        JPanel fpanel = new JPanel();
        //add panels
        fpanel.add(panel);
        fpanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(fpanel);
        pack();
        //Center window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), Math.max(0, (screenSize.height - windowSize.height) / 2));
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Paints the window
     *
     * @param g
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (captureSound.getAudioBytes() != null)
            createWaveForm();
    }

    /**
     * Draws the WAV file on the frame
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
            for (int i = 1; i < w - 2; i++) {
                g2.drawLine(i, y, i + 1, (int) (halfofheight - halfofheight * (float) audioData[i * inc] / (float) maxAudio));
                y = (int) (halfofheight - halfofheight * (float) audioData[i * inc] / (float) maxAudio);
            }
        }
    }
}
