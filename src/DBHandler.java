import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
/*
 * Author: Mengqiu Xu, mxu2013@gmail.com
 * Course: CSE 4051, Fall 2013
 * Project: proj11, Social Security Death Index
 */

public class DBHandler extends Thread {

    private static final int DB_UPDATE_FLAG = 0;
    private static final int DB_CLOSE_FLAG = 1;
    private static boolean isConnected = false;
    private static final String JDBC = "com.mysql.jdbc.Driver";
    private static final String SERVER =
       "jdbc:mysql://andrew.cs.fit.edu:3306/cse4051_ssdi?user=cse4051&password=jdbc2013";
    private Connection connector = null;
    private String statement = "";
    private int numRow = 0;
    private int numColumns = 0;
    private String[] columnNames = {"" };
    private final ArrayList<String> entries = new ArrayList<String>();
    private SSDI di;
    public ArrayList<Boolean> flags = new ArrayList<Boolean>();

    public final ArrayList<String> getEntries () {
        final ArrayList<String> entriesC = new ArrayList<String>();
        entriesC.addAll(entries);
        return entriesC;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#run()
     * Run the thread, if update flag change get table.
     * and update table for application.
     */
    @Override
    public final void run () {
        while (true) {
            if (flags.get(DB_UPDATE_FLAG)) {
                getTable(statement);
                flags.set(DB_UPDATE_FLAG, false);
                di.updateTableEnd();
            }
            if (flags.get(DB_CLOSE_FLAG)) {
                this.closeServer();
                flags.set(DB_CLOSE_FLAG, false);
                // di.updateTable();
                break;
            }

        }
    }

    public final void setStatement (final String statement1) {
        this.statement = statement1;
    }

    public final int getNumRow () {
        numRow = (entries.size() / numColumns);
        return numRow;
    }

    public final String[] getColumnNames () {
        return columnNames;
    }

    /*
     * Initialize the database and acquire the table column.
     */
    public final boolean init (final SSDI di1) {
        flags.add(false);
        flags.add(false);
        final boolean conn = connectServer(JDBC, SERVER);
        di = di1;
        return (conn && initTableColumn());

    }

    /*
     * get the table column
     */
    private boolean initTableColumn () {
        boolean isInit = false;
        try {
            final ResultSet results1 = excuter("SELECT * FROM `index` LIMIT 0");
            final ResultSetMetaData rmeta = results1.getMetaData();
            numColumns = rmeta.getColumnCount();
            columnNames = new String[numColumns];
            for (int i = 1; i <= numColumns; i++) {
                columnNames[i - 1] = rmeta.getColumnName(i);
            }

            isInit = true;
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return isInit;
    }

    public final int getNumColumns () {
        return numColumns;
    }

    /*
     * Run the statement on the Database server and acquire the data table.
     */
    public final void getTable (final String statement1) {
        entries.clear();
        try {
            final ResultSet results = excuter(statement1);
            final ResultSetMetaData rmeta = results.getMetaData();
            numColumns = rmeta.getColumnCount();
            columnNames = new String[numColumns];

            for (int i = 1; i <= numColumns; i++) {
                columnNames[i - 1] = rmeta.getColumnName(i);
            }

            while (results.next()) {
                for (int i = 1; i <= numColumns; ++i) {
                    final String s = results.getString(i);
                    if (s == null) {
                        entries.add("Null");
                    } else {
                        entries.add(s.trim());
                    }
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public final void closeServer () {
        try {
            if (!connector.isClosed()) {
                connector.close();
                isConnected = false;
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }

    }

    /*
     * Connect the server.
     */
    public final boolean connectServer (final String jdbc, final String server) {
        boolean isConn = false;
        try {
            Class.forName(jdbc);
            connector = DriverManager.getConnection(server);
            isConn = true;
        } catch (final ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (final SQLException e) {
            return isConnected;
        }
        System.setProperty ("jdbc.drivers", "com.mysql.jdbc.Driver");
        isConnected = isConn;
        return isConnected;
    }

    /*
     * Run the statement and return the result set.
     */
    private ResultSet excuter (final String statement1) {
        Statement stmt = null;
        ResultSet res = null;
        try {
            stmt = connector.createStatement();
            res = stmt.executeQuery(statement1);
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
}
