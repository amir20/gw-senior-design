
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.Vector;

/**
 * A Table Model which is responsible for populating the library table
 *
 * @author Amir Raminfar
 */
public class LibraryModel extends AbstractTableModel {
    private Vector<String> columns;
    private Vector rows;

    /**
     * Creates the table
     */
    public LibraryModel() {
        columns = new Vector<String>();
        columns.add(" ");
        columns.add("Application");
        columns.add("Date Created");
        columns.add("Accuracy");

        update(new Library().getLibrary());
    }

    /**
     * Reads wavs and updates the table
     *
     * @param wavs Vector of WAVFeatures
     */
    private void update(Vector<WAVFeature> wavs) {
        rows = new Vector();
        JFileChooser chooser = new JFileChooser();
        for (int i = 0; i < wavs.size(); i++) {
            Vector row = new Vector();
            File file = new File(wavs.get(i).getApp());
            Icon icon;
            if (file.exists()) {
                icon = chooser.getIcon(file);
            } else {
                icon = new ImageIcon("images/icon.png");
            }

            row.add(icon);
            float ratio;
            if (wavs.get(i).getTotal() > 0)
            //Compute acurracy percentage
                ratio = (float) (wavs.get(i).getSuccess() * 100 / wavs.get(i).getTotal());
            else
                ratio = 0;
            //Add file name
            row.add(chooser.getDescription(file).toUpperCase());
            // Add date
            row.add(wavs.get(i).getDateCreated());
            //Add accuracy
            row.add(ratio + "%");

            //Add the row to other rows
            rows.add(row);
        }
    }

    /**
     * Gets column name of table
     *
     * @param column index
     */
    public String getColumnName(int column) {
        return columns.get(column);
    }

    /**
     * returns size of columns
     */
    public synchronized int getColumnCount() {
        return columns.size();
    }

    /**
     * returns number of rows
     */
    public synchronized int getRowCount() {
        return rows.size();
    }

    /**
     * returns class tupe of specified column
     *
     * @param c index
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /**
     * Retruns value at specified row and column
     *
     * @param row
     * @param column
     */
    public synchronized Object getValueAt(int row, int column) {
        Vector temp = (Vector) rows.get(row);
        switch (column) {
            case 0:
                return (Icon) temp.get(column);
            default:
                return (String) temp.get(column);
        }

    }

    /**
     * Updates the table
     */
    public synchronized void updateData() {
        update(new Library().getLibrary());
        fireTableDataChanged();
    }

    /**
     * Returns true if cell is editable, otherwise false
     *
     * @param row
     * @param col
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}

