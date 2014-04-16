import java.util.ArrayList;


import javax.swing.table.AbstractTableModel;

/*
 * Author: Mengqiu Xu, mxu2013@gmail.com
 * Course: CSE 4051, Fall 2013
 * Project: proj11, Social Security Death Index
 */

@SuppressWarnings("serial")
public class TableValues extends AbstractTableModel {

    private ArrayList<String> entries;
    private int numColumns = 0;
    private int numRow = 0;
    private String[] columnNames;

    /*
     * update the data sets and Row number for the result data.
     */
    public final void updateTable (final int numRow1,
            final ArrayList<String> entries1) {
        entries = entries1;
        this.numRow = numRow1;
    }

    /*
     * Initialize the table value.
     * set default column number and column names.
     */
    TableValues(final int numColumns1, final String[] columnNames1) {
        this.numColumns = numColumns1;
        this.columnNames = columnNames1;
        entries = new ArrayList<String>();
    }

    public final int getRowCount () {
        return numRow;
    }

    public final int getColumnCount () {
        return numColumns;
    }

    /*
     * this method returns the table value at specific row and column. 
     */
    public final Object getValueAt (final int row, final int column) {
        try {

            return entries.get((row * numColumns) + column);
        } catch (final IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return "false";
    }

    public final String getColumnName (final int column) {
        return columnNames[column];
    }
}
