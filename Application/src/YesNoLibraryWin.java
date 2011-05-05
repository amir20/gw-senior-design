
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Allows user to record yes and no and then insert the new responses to library
 *
 * @author Amir Raminfar
 */
public class YesNoLibraryWin extends JDialog implements RecordingInterface, ActionListener {
    private CaptureSound yes;
    private CaptureSound no;
    private JPanel wf;
    private JButton b;
    private JButton play;
    private JLabel label;

    public void updateView() {
        repaint();
        play.setEnabled(true);
    }

    /**
     * Paints the window
     *
     * @param g
     */
    public void paint(Graphics g) {
        super.paint(g);
        createWaveForm();
    }

    /**
     * Create new dialog with parent
     *
     * @param parent Owner
     */
    public YesNoLibraryWin(JFrame parent) {
        super(parent, "Record Yes and No Phrases", true);
        // ADD gui
        setResizable(false);
        yes = new CaptureSound(this);
        no = new CaptureSound(this);
        wf = new JPanel();
        wf.setPreferredSize(new Dimension(400, 100));
        wf.setBackground(Color.white);
        wf.setBorder(new LineBorder(Color.black));
        b = new JButton("Next");
        b.addActionListener(this);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        JButton record = new JButton("Record", new ImageIcon("images/record.png"));
        play = new JButton("Play", new ImageIcon("images/play.png"));
        record.addActionListener(this);
        play.addActionListener(this);
        JPanel buttons = new JPanel();
        buttons.add(record);
        buttons.add(play);
        top.add(wf, BorderLayout.NORTH);
        top.add(buttons, BorderLayout.SOUTH);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(top, BorderLayout.NORTH);
        JPanel fPanel = new JPanel();
        fPanel.setLayout(new BorderLayout());
        setContentPane(fPanel);
        JPanel bottom = new JPanel();
        fPanel.add(label = new JLabel("First record the word YES and then click Next to record the word NO"), BorderLayout.NORTH);
        label.setFont(new Font(label.getFont().getName(), Font.BOLD, label.getFont().getSize()));
        fPanel.add(panel, BorderLayout.SOUTH);
        bottom.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(b);
        bottom.add(cancel);
        panel.add(bottom, BorderLayout.SOUTH);
        fPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), Math.max(0, (screenSize.height - windowSize.height) / 2));
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        play.setEnabled(false);
        setVisible(true);
    }

    /**
     * Processes all actions
     *
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        //user can record no
        if ("Next".equals(e.getActionCommand())) {
            if (!play.isEnabled())
                return;
            b.setText("Finish");
            play.setEnabled(false);
            label.setText("Now record the word NO and click Finished when you are done");
            repaint();
//write to disk
        } else if ("Finish".equals(e.getActionCommand())) {
            if (!play.isEnabled())
                return;
            new File(new UserOptions().getProp("YesNoLibraryPath")).delete();
            Library lib = new Library(new UserOptions().getProp("YesNoLibraryPath"));
            String wavFolder = new UserOptions().getProp("WAVFolderPath");
            SaveWavFile.SaveToFile(yes.getInputStream(), wavFolder.concat("yes.wav"));
            SaveWavFile.SaveToFile(no.getInputStream(), wavFolder.concat("no.wav"));
            lib.addToLibrary(new WAVFeature(yes.getAudioBytes(), 100, "true", wavFolder.concat("yes.wav")));
            lib.addToLibrary(new WAVFeature(no.getAudioBytes(), 100, "false", wavFolder.concat("no.wav")));
            lib.saveLibraryToDisk();
            setVisible(false);
            dispose();
            // record yes or no
        } else if ("Record".equals(e.getActionCommand())) {
            if (b.getText().equals("Next")) {
                yes.start();
            } else if (b.getText().equals("Finish")) {
                no.start();
            }
            //play yes or no
        } else if ("Play".equals(e.getActionCommand())) {
            if (b.getText().equals("Next")) {
                new Playback(yes.getInputStream(), yes.getFormat());
            } else if (b.getText().equals("Finish")) {
                new Playback(no.getInputStream(), no.getFormat());
            }

        } else if ("Cancel".equals(e.getActionCommand())) {
            setVisible(false);
            dispose();
        }
    }

    /**
     * Creates graph of WAV
     */
    private void createWaveForm() {
        byte[] audioBytes = null;
        if (b.getText().equals("Next")) {
            audioBytes = yes.getAudioBytes();
        } else if (b.getText().equals("Finish")) {
            audioBytes = no.getAudioBytes();
        }

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
