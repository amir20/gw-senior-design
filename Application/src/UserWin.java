
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import javax.help.*;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;


/**
 * Creates and displays the application window
 *
 * @author Amir Raminfar
 */

public class UserWin extends JFrame {

    hotkey hk;
    JTable table;
    LibraryModel libraryModel;
    JLabel name;
    JLabel dateCreated;
    JLabel lastUsed;
    JLabel numUsed;
    SystemTray tray = SystemTray.getDefaultSystemTray();
    TrayIcon ti;
    SplashWin splash = null;
    JButton remove, modify, play;
    ContiniousRecording continiousRecording;
    JButton recordingButton;
    AdjustMic adjustMicl;
    JPanel micPanel;

    /**
     * Gets called when a hotkey is recognized
     *
     * @param type the ID of the hotkey
     */
    public void hotkeyEvent(int type) {
        if (type == 120) {
            new RecognitionThread(this);
        }
    }

    /**
     * Creates a new instance of UserWin
     */
    public UserWin() {
        super("Voice Launch");

        //Check for single instance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            new ServerSocket(64000);
            splash = new SplashWin("images/splash.png");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Only one instance of this application can be running.", "Multiple Instances Not Allowed", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } catch (Exception e) {
            //do nothing
        }
        //Setup GUI
        splash.setText("Adding GUI Components...");
        setUpGUI();

        //Setup System Tray Icon
        splash.setText("Starting System Tray Icon...");
        setTrayIcon();
        System.gc();

        // Done With GUI
        splash.setText("Please Wait...");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Close Splash Screen
        splash.close();

        //Show Window
        setVisible(true);
    }

    /**
     * Creates system tray icon
     */
    private void setTrayIcon() {
        //Create System Tray Menu
        JPopupMenu menu = new JPopupMenu();
        UserOptions userOptions = new UserOptions();
        JMenuItem menuItem;
        menuItem = new JMenuItem("Open Voice Launch");
        //Add default event for double clicking
        if (userOptions.getProp("DefaultAction").equals("show"))
            menuItem.setFont(new Font(menuItem.getFont().getName(), Font.BOLD, menuItem.getFont().getSize()));
        menuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UserWin.this.setVisible(true);
                libraryModel.updateData();
            }
        });
        menu.add(menuItem);
        menuItem = new JMenuItem("Launch Application...");
        if (userOptions.getProp("DefaultAction").equals("launch"))
            menuItem.setFont(new Font(menuItem.getFont().getName(), Font.BOLD, menuItem.getFont().getSize()));
        menuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new RecognitionThread(UserWin.this);
                libraryModel.updateData();
            }
        });
        menu.add(menuItem);
        menu.addSeparator();
        menuItem = new JMenuItem("Quit");
        menuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.exit(0);
            }
        });
        menu.add(menuItem);
        //Add icon
        ti = new TrayIcon(new ImageIcon("images/mic.png"), "Voice Launch", menu);
        ti.setIconAutoSize(true);
        ti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UserOptions userOptions = new UserOptions();
                if (userOptions.getProp("DefaultAction").equals("show"))
                    UserWin.this.setVisible(true);
                else
                    new RecognitionThread(UserWin.this);
                libraryModel.updateData();
            }
        });
        //Add tray icon
        tray.addTrayIcon(ti);
    }

    /**
     * Initializes library components
     */
    private void setUpGUI() {
        // Setup Hotkey
        this.setIconImage(new ImageIcon("images/mic.png").getImage());
        this.setResizable(false);
        splash.setText("Starting Hotkey Thread...");
        hk = new hotkey(this);
        //Start hotkey listener
        hk.start();
        splash.setText("Setting Up Menu...");
        //Setup menu items
        setUpMenu();
        getContentPane().setLayout(new BorderLayout());
        //Add library panel
        splash.setText("Setting Up Library Panel...");
        getContentPane().add(getLibraryPanel(), BorderLayout.CENTER);
        //Add JToolbar
        splash.setText("Setting Up Toolbar...");
        getContentPane().add(getToolbar(), BorderLayout.PAGE_START);
        pack();
        //Center Window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), Math.max(0, (screenSize.height - windowSize.height) / 2));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        micPanel = getAdjustMicPanel();
    }

    /**
     * Creates, populates, and returns the Library Panel
     */
    private JPanel getLibraryPanel() {
        // Create library panel
        JPanel panel = new JPanel();
        libraryModel = new LibraryModel();
        splash.setText("Loading Library...");
        table = new JTable(libraryModel) {
            //Set alternate coloring
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (!isCellSelected(rowIndex, vColIndex)) {
                    if (rowIndex % 2 == 0) {
                        c.setBackground(new Color(0, 0, 255, 25));
                    } else {
                        c.setBackground(getBackground());
                    }
                }
                return c;
            }
//Set tooltip
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                WAVFeature object = new Library().getLibrary().get(rowIndex);
                int colIndex = columnAtPoint(p);
                if (colIndex == 1) {
                    return object.getApp();
                }
                return null;
            }
        };
        //Add Update Listener
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                updateLibrary();
            }
        });
        //Setup look and feel
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setRowHeight(20);
        table.setPreferredScrollableViewportSize(new Dimension(450, 200));
        table.getColumnModel().getColumn(0).setMaxWidth(20);
        table.getColumnModel().getColumn(3).setMaxWidth(65);

        DefaultTableCellRenderer cellren = new DefaultTableCellRenderer();
        cellren.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(cellren);

        //Set selection mode
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //Allow user to launch applications by double clicking
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = table.rowAtPoint(e.getPoint());
                    new LaunchApplication(new Library().get(index).getApp());
                }
            }
        });

        //Load library view
        splash.setText("Loading Library Viewer...");
        JPanel topPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        topPanel.add(scrollPane);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        //Properties
        name = new JLabel("Nothing Selected...");
        dateCreated = new JLabel(" ");
        lastUsed = new JLabel(" ");
        numUsed = new JLabel(" ");
        bottomPanel.add(name);
        bottomPanel.add(dateCreated);
        bottomPanel.add(lastUsed);
        bottomPanel.add(numUsed);

        //Add buttons
        splash.setText("Loading Library Buttons...");
        JToolBar buttonPanel = new JToolBar();
        buttonPanel.setFloatable(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        // Remove button
        remove = new JButton("Remove", new ImageIcon("images/remove.png"));
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (table.getSelectedRow() == -1)
                    return;
                Library lib = new Library();
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(UserWin.this, "Are you sure you want to remove " + lib.get(table.getSelectedRow()).getApp() + "?", " ", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)) {
                    WAVFeature tempWAV = lib.remove(table.getSelectedRow());
                    lib.saveLibraryToDisk();
                    File WAVfile = new File(tempWAV.getWAVFile());
                    if (WAVfile.exists()) {
                        WAVfile.delete();
                    }
                    libraryModel.updateData();
                }
            }
        });
        buttonPanel.add(remove);
        // Add modify
        modify = new JButton("Modify Application", new ImageIcon("images/modify.png"));
        modify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (table.getSelectedRow() == -1)
                    return;
                Library lib = new Library();
                JFileChooser fchooser = new JFileChooser(lib.get(table.getSelectedRow()).getApp());
                fchooser.setFileFilter(new AppFileFilter());
                if (JFileChooser.APPROVE_OPTION == fchooser.showOpenDialog(null)) {
                    lib.replaceApplication(table.getSelectedRow(), fchooser.getSelectedFile().toString());
                    lib.saveLibraryToDisk();
                    libraryModel.updateData();
                }
            }
        });
        buttonPanel.add(modify);
        // Add Play buttons
        play = new JButton("Play", new ImageIcon("images/play.png"));
        play.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (table.getSelectedRow() == -1)
                    return;
                Library lib = new Library();
                new Playback(new File(lib.get(table.getSelectedRow()).getWAVFile()));

            }
        });
        buttonPanel.add(play);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomPanel.add(buttonPanel);
        panel.setLayout(new BorderLayout());
        panel.add(topPanel, BorderLayout.CENTER);
        JPanel temp = new JPanel();
        temp.setBorder(new TitledBorder(new EtchedBorder(), "Summary for Selected Item"));
        temp.add(bottomPanel);
        temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
        panel.add(temp, BorderLayout.SOUTH);
        // Disable buttons
        play.setEnabled(false);
        modify.setEnabled(false);
        remove.setEnabled(false);
        splash.setText("Library Loaded Successfully...");

        return panel;
    }

    /**
     * Creates and returns the Adjustig Mic JPanel
     */
    private JPanel getAdjustMicPanel() {
        //Setup mic panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(adjustMicl = new AdjustMic(this.getBackground()), BorderLayout.CENTER);
        panel.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), new LineBorder(Color.black)));
        return panel;
    }

    /**
     * Updates the library items
     */
    private void updateLibrary() {
        // Update library when user clicks on items
        if (table.getSelectedRow() != -1) {
            modify.setEnabled(true);
            play.setEnabled(true);
            remove.setEnabled(true);
            Library lib = new Library();
            WAVFeature object = lib.getLibrary().get(table.getSelectedRow());
            name.setText("  Application: " + object.getApp());
            dateCreated.setText("  Date Created: " + object.getDateCreated());
            lastUsed.setText("  Last Used: " + object.getLastUsed());
            numUsed.setText("  Recognized: " + object.getTotal() + " times   (" + (object.getTotal() > 0 ? (100 * object.getSuccess() / object.getTotal()) : 0) + "% Successful)");
        } else {
            // if nothing is selected then disable buttons
            modify.setEnabled(false);
            play.setEnabled(false);
            remove.setEnabled(false);
            name.setText("  Nothing selected...");
            dateCreated.setText(" ");
            lastUsed.setText(" ");
            numUsed.setText(" ");
        }
    }

    /**
     * Creates GUI's toolbar and returns a panel containing the toolbar
     */
    private JPanel getToolbar() {
        //Create Toolbar
        splash.setText("Adding Toolbar Buttons...");
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JToolBar toolBar = new JToolBar();
        panel.add(toolBar);
        JButton b = new JButton(new ImageIcon("images/newapp.png"));
        // Inser new application button
        b.setToolTipText("Insert New Application to Library");
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new InsertNewApplication(UserWin.this);
            }
        });
        toolBar.add(b);
        //Settings button
        b = new JButton(new ImageIcon("images/settings.png"));
        b.setToolTipText("Change Settings");
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new SettingsWin(UserWin.this);
            }
        });
        toolBar.add(b);
        toolBar.add(new JToolBar.Separator());

        //Refresh button
        b = new JButton(new ImageIcon("images/refresh.png"));
        b.setToolTipText("Refresh Library Content");
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                libraryModel.updateData();
            }
        });
        toolBar.add(b);
        //Live listener button
        recordingButton = new JButton("Start Live Listener");
        JPanel border = new JPanel();
        border.setToolTipText("Live Listener Monitor");
        recordingButton.setToolTipText("Starts or stops the live listener. (This feature is still beta)");
        recordingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (continiousRecording.isRunning()) {
                    continiousRecording.shutDown(null);
                    recordingButton.setText("Start Live Listener");
                } else {
                    continiousRecording.start();
                    recordingButton.setText("Stop Live Listener");
                }
            }
        });

        splash.setText("Creating Continious Panel...");

        //Set Border
        border.setBorder(new LineBorder(Color.black));
        continiousRecording = new ContiniousRecording(this);
        toolBar.addSeparator(new Dimension(this.getPreferredSize().width - continiousRecording.getPreferredSize().width - 190, 0));
        toolBar.add(recordingButton);
        toolBar.addSeparator(new Dimension(10, 0));
        border.setLayout(new BorderLayout());
        border.add(continiousRecording, BorderLayout.CENTER);
        toolBar.add(border);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        return panel;
    }

    /**
     * Sets up menu for the GUI
     */
    private void setUpMenu() {
        //Setup file menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem menuItem = new JMenuItem("User Preferences");
        menu.add(menuItem);
        menu.addSeparator();
        menuBar.add(menu);
        menuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new SettingsWin(UserWin.this);
            }
        });

        final JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("Hide Window on Exit");
        cbMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (cbMenuItem.getState()) {
                    UserWin.this.setTitle("Voice Launch (Hide on Exit)");
                    UserWin.this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                } else {
                    UserWin.this.setTitle("Voice Launch");
                    UserWin.this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                }
            }
        });
        menu.add(cbMenuItem);
        menuItem = new JMenuItem("Exit");
        menu.add(menuItem);
        menuBar.add(menu);
        menuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (UserWin.this.getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE) {
                    System.exit(0);
                } else {
                    UserWin.this.setVisible(false);
                }
            }
        });

        //Setup view menu
        menu = new JMenu("View");
        final JCheckBoxMenuItem cbAdjust = new JCheckBoxMenuItem("Show Microphone Monitor");
        cbAdjust.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (cbAdjust.getState()) {
                    getContentPane().add(micPanel, BorderLayout.EAST);
                    UserWin.this.pack();
                    adjustMicl.start();
                } else {
                    getContentPane().remove(micPanel);
                    adjustMicl.shutDown(null);
                    UserWin.this.pack();
                }
            }
        });
        menu.add(cbAdjust);
        menuBar.add(menu);

        //Setup library menu
        menu = new JMenu("Library");
        menuItem = new JMenuItem("Insert New Application to Library");
        menuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new InsertNewApplication(UserWin.this);
            }
        });
        menu.add(menuItem);
        menuBar.add(menu);

        menuItem = new JMenuItem("Record Yes and No Phrases");
        menuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new YesNoLibraryWin(UserWin.this);
            }
        });
        menu.add(menuItem);
        menuBar.add(menu);
        menuItem = new JMenuItem("Record Computer Phrase");
        menuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new RecordComputer(UserWin.this);
            }
        });
        menu.add(menuItem);
        menu.addSeparator();
        menuItem = new JMenuItem("Clear Library Statistics");
        menuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Library lib = new Library();
                lib.removeStats();
                lib.saveLibraryToDisk();
                libraryModel.updateData();
            }
        });
        menu.add(menuItem);


        //Setup Help menu
        menu = new JMenu("Help");
        menuItem = new JMenuItem("Help");

        //Setup helpset,
        //Helpset is a class provided by Java which allows for viewing HTML files
        HelpSet hs;
        // Find the HelpSet file and create the HelpSet object
        String helpHS = "IdeHelp.hs";
        ClassLoader cl = UserWin.class.getClassLoader();
        try {
            URL hsURL = HelpSet.findHelpSet(cl, helpHS);

            hs = new HelpSet(null, hsURL);
        } catch (Exception ee) {
            System.out.println("HelpSet " + ee.getMessage());
            System.out.println("HelpSet " + helpHS + " not found");
            return;
        }
        // Create a HelpBroker object:
        HelpBroker hb = hs.createHelpBroker();

        menuItem.addActionListener(new CSH.DisplayHelpFromSource(hb));

        menu.add(menuItem);
        menu.addSeparator();
        //About box
        menuItem = new JMenuItem("About Voice Launch...");
        menuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new AboutBox(UserWin.this);
            }
        });
        menu.add(menuItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);
    }
}

/**
 * File Filter when browsing for application
 *
 * @author Amir Raminfar
 */
class AppFileFilter extends FileFilter {
    /**
     * Returns true if the f is an executable file
     *
     * @param f file to accept
     */
    public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = getExtension(f);
            if (extension != null && extension.compareToIgnoreCase("exe") == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the extension of a file
     *
     * @param f
     */
    private String getExtension(File f) {
        if (f != null) {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if (i > 0 && i < filename.length() - 1) {
                return filename.substring(i + 1).toLowerCase();
            }
            ;
        }
        return null;
    }

    /**
     * Returns description of this class
     */
    public String getDescription() {
        return "Application Files";
    }
}
