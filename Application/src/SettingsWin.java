
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Creates a dialog to display the user's settings
 *
 * @author Amir Raminfar
 */
public class SettingsWin extends JDialog {
    private ButtonGroup sysTray;
    private UserOptions userOptions;
    private JTabbedPane tabs;
    private Hashtable<String, JTextField> props = new Hashtable<String, JTextField>();

    /**
     * Displays the Settings Window
     *
     * @param parent Owner
     */
    SettingsWin(JFrame parent) {
        super(parent, "Settings", true);
        setResizable(false);
        userOptions = new UserOptions();
        Enumeration e = userOptions.getDefault().keys();
        //create textfields
        while (e.hasMoreElements()) {
            String key = (String) (e.nextElement());
            props.put(key, new JTextField(userOptions.getProp(key), 15));
        }

        // create tabs pane
        tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.LEFT);
        //add tabs
        tabs.addTab("Library Settings", getLibraryOptions());
        tabs.addTab("System Tray Settings", getSystemTrayOptions());

        getContentPane().add(tabs, BorderLayout.NORTH);
        getContentPane().add(getButtons(), BorderLayout.SOUTH);
        pack();
        //Center window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), Math.max(0, (screenSize.height - windowSize.height) / 2));
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Gets the Library Panel
     */
    private JPanel getLibraryOptions() {
        JPanel library = new JPanel();
        JPanel temp = new JPanel();
        temp.setLayout(new FlowLayout(FlowLayout.LEFT));
        temp.setAlignmentX(Component.LEFT_ALIGNMENT);

        // show library file path
        library.setLayout(new BoxLayout(library, BoxLayout.Y_AXIS));
        temp.add(new JLabel("Library File: "));
        temp.add(props.get("LibraryPath"));
        library.add(temp);

        // wave folder path
        temp = new JPanel();
        temp.setLayout(new FlowLayout(FlowLayout.LEFT));
        temp.setAlignmentX(Component.LEFT_ALIGNMENT);
        temp.add(new JLabel("WAV Folder Location: "));
        temp.add(props.get("WAVFolderPath"));
        library.add(temp);

        // yes or no library path
        temp = new JPanel();
        temp.setLayout(new FlowLayout(FlowLayout.LEFT));
        temp.setAlignmentX(Component.LEFT_ALIGNMENT);
        temp.add(new JLabel("Yes and No Library: "));
        temp.add(props.get("YesNoLibraryPath"));
        library.add(temp);
        library.setBorder(new TitledBorder(new EmptyBorder(10, 10, 100, 200), "Library Options", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
        return library;
    }

    /**
     * Gets the System Tray Panel
     */
    private JPanel getSystemTrayOptions() {
        JPanel library = new JPanel();
        library.setLayout(new BoxLayout(library, BoxLayout.Y_AXIS));
        // let user choose default options
        library.add(new JLabel("Default Action on Left Click:"));
        sysTray = new ButtonGroup();
        JRadioButton show = new JRadioButton("Show Voice Launch Window");
        show.setActionCommand("show");
        JRadioButton recognize = new JRadioButton("Launch Application...");
        recognize.setActionCommand("launch");
        sysTray.add(show);
        sysTray.add(recognize);
        library.add(show);
        library.add(recognize);

        if (userOptions.getProp("DefaultAction").equals("show"))
            show.setSelected(true);
        else
            recognize.setSelected(true);

        library.setBorder(new TitledBorder(new EmptyBorder(10, 10, 100, 200), "System Tray Options", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
        return library;
    }

    /**
     * Gets Buttons Panel
     */
    private JPanel getButtons() {
        JPanel panel = new JPanel();
        JButton OK = new JButton("Apply Changes");
        JButton cancel = new JButton("Cancel");
        OK.addActionListener(new java.awt.event.ActionListener() {
            // update user's profile
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userOptions.setProp("DefaultAction", sysTray.getSelection().getActionCommand());
                userOptions.setProp("LibraryPath", props.get("LibraryPath").getText());
                userOptions.setProp("WAVFolderPath", props.get("WAVFolderPath").getText());
                userOptions.setProp("YesNoLibraryPath", props.get("YesNoLibraryPath").getText());
                userOptions.saveOptions();
                setVisible(false);
            }
        });
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setVisible(false);
            }
        });

        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panel.add(OK);
        panel.add(cancel);
        // allow user to load defaults
        JButton loadDef = new JButton("Load Defaults");
        loadDef.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userOptions.loadDefaults();
                int index = tabs.getSelectedIndex();
                tabs.removeAll();
                props.clear();
                Enumeration e = userOptions.getDefault().keys();
                while (e.hasMoreElements()) {
                    String key = (String) (e.nextElement());
                    props.put(key, new JTextField(userOptions.getProp(key), 15));
                }
                // update tabs again
                tabs.addTab("Library Settings", getLibraryOptions());
                tabs.addTab("System Tray Settings", getSystemTrayOptions());
                tabs.setSelectedIndex(index);
            }
        });
        panel.add(loadDef);
        return panel;

    }
}
