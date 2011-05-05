
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;

/**
 * Displays the About Box Dialog
 *
 * @author Amir Raminfar
 */
public class AboutBox extends JDialog {
    /**
     * Display the JDialog with parent
     *
     * @param parent the owner of this dialog
     */
    AboutBox(JFrame parent) {
        super(parent, "About This Application...", true);
        this.setResizable(false);
        //Add about box content
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("   ");
        label.setIcon(new ImageIcon("images/bigmic.png"));
        JTextPane text = new JTextPane();
        text.setText("\n\nThis program was designed and implemented by Amir Raminfar as a Senior Design Project for The" + "George Washington University. \nThe author can be contacted by e-mail at amirxoxo@gwu.edu. \n\n" + "Version 1.0\nApril of 2005\n\nCopyright © 2005 Voice Launch,  All rights reserved.");
        text.setPreferredSize(new Dimension(370, 130));
        text.setEditable(false);
        text.setOpaque(true);
        text.setEnabled(false);
        text.setBackground(label.getBackground());
        text.setDisabledTextColor(Color.black);
        panel.add(new JLabel(new ImageIcon("images/splash.png")), BorderLayout.PAGE_START);
        panel.add(text, BorderLayout.CENTER);
        setContentPane(panel);
        JButton exit = new JButton("Done");
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setVisible(false);
                dispose();
            }
        });
        // Add button for JVM
        JButton jvc = new JButton("Show JVM Status");
        bp.add(jvc);
        jvc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new JVMStatus();
            }
        });
        // Exit button that closes the popup
        bp.add(exit);
        panel.add(bp, BorderLayout.SOUTH);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        pack();
        // Center
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), Math.max(0, (screenSize.height - windowSize.height) / 2));
        setVisible(true);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }
}
