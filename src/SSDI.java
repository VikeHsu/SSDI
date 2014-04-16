import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
/*
 * Author: Mengqiu Xu, mxu2013@gmail.com
 * Course: CSE 4051, Fall 2013
 * Project: proj11, Social Security Death Index
 */

@SuppressWarnings("serial")
public class SSDI extends JFrame {

    private JComboBox<String> columnList1;
    private JComboBox<String> columnList2 = new JComboBox<String>();
    private JTextField text;
    private int currentPage = 0;
    private JTable table;
    private JProgressBar progressBar;
    private static TableValues tValues;
    private static DBHandler db = new DBHandler();

    private static final String[] METHOD_LIST = {"Start with", "End with",
            "Contain", "Larger than", "Smaller than", "Equal to", "Unequal to" };
    private static final int ITEM_PER_PAGE = 20;
    private static final int ITEM_NUMBER_FOR_LAYOUT = 4;
    private static final int DB_UPDATE_FLAG = 0;
    private static final int DB_CLOSE_FLAG = 1;
    private final ArrayList<String> filterStatements = new ArrayList<String>();
    private final JPanel filterGroup = new JPanel();
    private final JButton searchButton = new JButton("Search");
    private final JLabel state = new JLabel("");
    private final JButton nextButton = new JButton("Next");
    private final JButton previousButton = new JButton("Previous");

    /*
     * Set Menu Bar for the frame.
     * It contains a exit button for exit the application.
     */
    final void setupMenuBar () {
        final MenuBar menuBar = new MenuBar();
        final Menu fileMenu = new Menu("File");
        final MenuItem fileExit = new MenuItem("Exit");
        fileExit.addActionListener(new MenuItemHandler());
        fileMenu.add(fileExit);
        menuBar.add(fileMenu);
        setMenuBar(menuBar);
    }

    /*
     * This class is a action listener.
     * It handle the action from the menu bar.
     */
    class MenuItemHandler implements ActionListener {
        public void actionPerformed (final ActionEvent ev) {
            final String s = ev.getActionCommand();
            if (s == "Exit") {
                db.flags.set(DB_CLOSE_FLAG, true);
                System.exit(0);
            }
        }
    }

    /*
     * This method is to generate appropriate SQL statement for the database.
     */
    public final String getSqlStatement () {
        final int offset = currentPage * ITEM_PER_PAGE;
        String str = "SELECT * FROM `index` WHERE (";
        str += getFilterStatement();
        for (String st : filterStatements) {
            str += " and " + st;
        }
        str += " ) LIMIT " + offset + "," + ITEM_PER_PAGE;
        return (str);
    }

    /*
     * generate the where syntax of the SQL
     */
    private String getFilterStatement () {
        final String col = (String) columnList1.getSelectedItem();

        String str = col;
        switch ((String) columnList2.getSelectedItem()) {
        case "Start with":
            str += " like \"" + text.getText() + "%\"";
            break;
        case "End with":
            str += " like \"%" + text.getText() + "\"";
            break;
        case "Contain":
            str += " like \"%" + text.getText() + "%\"";
            break;
        case "Larger than":
            str += " > \"" + text.getText() + "\"";
            break;
        case "Smaller than":
            str += " < \"" + text.getText() + "\"";
            break;
        case "Equal to":
            str += " = \"" + text.getText() + "\"";
            break;
        case "Unequal to":
            str += " <> \"" + text.getText() + "\"";
            break;
        default:
            break;
        }
        return str;
    }

    /*
     * update the table from the date we acquire from the database.
     */
    public final void updateTableEnd () {
        tValues.updateTable(db.getNumRow(), db.getEntries());
        state.setText("Done! \\(^0^)/");
        state.setBackground(Color.GREEN);
        searchButton.setEnabled(true);

        if (tValues.getRowCount() < (ITEM_PER_PAGE - 1)) {
            nextButton.setEnabled(false);
        } else {
            nextButton.setEnabled(true);
        }
        if (currentPage == 0) {
            previousButton.setEnabled(false);
        } else {
            previousButton.setEnabled(true);
        }
        progressBar.setIndeterminate(false);
        table.updateUI();
    }

    /*
     * This Action listener handle the action from the button
     * clicked.
     */
    class ExecuteHandler implements ActionListener {
        private SSDI di;

        ExecuteHandler(final SSDI di2) {
            this.di = di2;
        }

        /*
         * different kind of method to update table for the application.
         * flag is to indicate which action is needed.
         * such as get information for next or previous page,
         * or just execute the command from the panel.
         */
        private void updateTableStart (final int flag) {
            state.setText("Running...  (・ω・=)");
            state.setBackground(Color.YELLOW);
            searchButton.setEnabled(false);
            progressBar.setIndeterminate(true);
            nextButton.setEnabled(false);
            previousButton.setEnabled(false);
            switch (flag) {
            case 0:
                break;
            case 1:
                currentPage++;
                break;
            case 2:
                currentPage--;
                break;
            default:
                break;
            }
            db.setStatement(di.getSqlStatement());
            System.out.println(di.getSqlStatement());
            db.flags.set(DB_UPDATE_FLAG, true);
        }

        /*
         * get act for different action.
         */
        public void actionPerformed (final ActionEvent ev) {
            final String s = ev.getActionCommand();
            switch (s) {
            case "Search":
                updateTableStart(0);
                break;
            case "Next":
                updateTableStart(1);
                break;
            case "Previous":
                updateTableStart(2);
                break;
            case "Add":
                if (di.text.getText().length() == 0) {
                    JOptionPane.showMessageDialog(di,
                            "Please type text. ′(*>_<*)′", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    di.updateFilterGroup();
                    di.pack();
                }
                break;
            default:
                break;
            }
        }
    }

    /*
     * This method generate the filter selection panel.
     * it can select data from different column and set restrains
     * for those columns.
     */
    private JPanel initFilterItem () {
        final JPanel filterItem = new JPanel();
        filterItem.setLayout(new GridLayout(1, ITEM_NUMBER_FOR_LAYOUT));

        columnList1 = new JComboBox<String>(db.getColumnNames());
        columnList1.setAlignmentY(Component.TOP_ALIGNMENT);
        columnList2 = new JComboBox<String>(METHOD_LIST);
        columnList2.setAlignmentY(Component.TOP_ALIGNMENT);
        text = new JTextField("");
        text.setAlignmentY(Component.TOP_ALIGNMENT);
        text.updateUI();
        final JButton addButton = new JButton("Add");

        addButton.addActionListener(new ExecuteHandler(this));
        addButton.setAlignmentY(Component.TOP_ALIGNMENT);

        filterItem.add(columnList1);
        filterItem.add(columnList2);
        filterItem.add(text);
        filterItem.add(addButton);

        filterItem.setAlignmentY(Component.TOP_ALIGNMENT);
        return filterItem;
    }

    /*
     * This is to update the filter group we can add
     * more constrains for SQL.
     */
    public final void updateFilterGroup () {
        final JPanel filterSets = new JPanel();
        filterSets.setLayout(new GridLayout(1, ITEM_NUMBER_FOR_LAYOUT - 1));
        filterStatements.add(getFilterStatement());
        filterSets.add(new JLabel((String) columnList1.getSelectedItem()));
        filterSets.add(new JLabel((String) columnList2.getSelectedItem()));
        filterSets.add(new JLabel(text.getText()));
        filterGroup.add(filterSets);

    }

    /*
     * Initialize the filter panel.
     */
    private JPanel initFilter () {
        final JPanel filterPanel = new JPanel();

        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));

        filterGroup.setLayout(new BoxLayout(filterGroup, BoxLayout.Y_AXIS));
        filterGroup.setAlignmentY(Component.TOP_ALIGNMENT);

        filterPanel.add(initFilterItem());

        filterPanel.add(filterGroup);
        searchButton.addActionListener(new ExecuteHandler(this));

        filterPanel.add(searchButton);
        filterPanel.add(new JPanel());
        filterPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        return filterPanel;

    }

    /*
     * Initialize the status panel.
     */
    private JPanel initStatus () {
        final JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));

        progressBar = new JProgressBar();
        southPanel.add(progressBar);
        state.setOpaque(true);

        if (db.init(this)) {
            state.setText("Connection is OK. \\(^0^)/");
            state.setBackground(Color.GREEN);
        } else {
            searchButton.setEnabled(false);
            state.setText("Connection is lost. ::>_<::");
            state.setBackground(Color.red);
        }

        final JPanel statePanel = new JPanel();
        final JLabel st = new JLabel("State:");
        statePanel.add(st);
        statePanel.add(state);
        previousButton.setEnabled(false);
        previousButton.addActionListener(new ExecuteHandler(this));
        statePanel.add(previousButton);

        nextButton.setEnabled(false);
        nextButton.addActionListener(new ExecuteHandler(this));
        statePanel.add(nextButton);

        southPanel.add(statePanel);

        return southPanel;
    }

    /*
     * Initialize the result table panel.
     */
    private JScrollPane initResultTable () {

        tValues = new TableValues(db.getNumColumns(), db.getColumnNames());

        table = new JTable(tValues);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        final JScrollPane jsp = new JScrollPane(table);

        return jsp;
    }

    /*
     * Setup application with database connected.
     */
    private void setup () {
        setupMenuBar();

        final Container pane = getContentPane();

        pane.setLayout(new BorderLayout());

        pane.add(initStatus(), BorderLayout.SOUTH);
        pane.add(initFilter(), BorderLayout.NORTH);
        pane.add(initResultTable(), BorderLayout.CENTER);
        setTitle("Mengqiu Xu -- Language Database");
        pack();
        setScreenMiddle();
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        db.start();
    }

    /*
     * Constructor for this class.
     * setup it and show.
     */
    SSDI() {
        this.setup();
        this.setVisible(true);
    }

    /*
     * make the frame of the application in the middle of the screen.
     */
    private void setScreenMiddle () {
        final Dimension screenSize = Toolkit.getDefaultToolkit()
                .getScreenSize();
        this.setLocation((screenSize.width - this.getWidth()) / 2,
                (screenSize.height - this.getHeight()) / 2);
    }

    @SuppressWarnings("unused")
    public static void main (final String[] args) {
        final SSDI di = new SSDI();
    }
}
