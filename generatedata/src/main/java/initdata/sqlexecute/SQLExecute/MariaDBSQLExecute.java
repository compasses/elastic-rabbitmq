package initdata.sqlexecute.SQLExecute;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by I311352 on 12/1/2016.
 */
@Service
public class MariaDBSQLExecute {
    private static final Logger logger = Logger.getLogger(MariaDBSQLExecute.class);
    private String DEFAULT_CONNECTION = "/sql/db.properties";
    private String PRODUCTION_CONFIG = "/etc/secrets/mariadb/mariadb.properties";
    private boolean USER_CONFIG = false;

    private Connection connection = null;

    public boolean initConnect() {
        InputStream inputStream = null;
        try {
            File mariaDBconfig = new File(PRODUCTION_CONFIG);
            if (mariaDBconfig.exists()) {
                inputStream = new FileInputStream(mariaDBconfig);
            } else if (USER_CONFIG) {
                inputStream = new FileInputStream(DEFAULT_CONNECTION);
            } else {
                inputStream = this.getClass().getResourceAsStream(DEFAULT_CONNECTION);
            }

            Properties props = new Properties();
            props.load(inputStream);

            String dbHost = props.get("MYSQL_HOST").toString();
//            String dbPort = props.get("MYSQL_PORT").toString();
            String dbSchema = props.get("MYSQL_SCHEMA").toString();

            Class.forName("org.mariadb.jdbc.Driver");
            String connectStr = "jdbc:mariadb://" + dbHost + "/" + dbSchema;

            this.connection = DriverManager.getConnection(connectStr, "root", "Initial0");
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException " + e);
        } catch (SQLException e) {
            logger.error("SQLException " + e);
        } catch (IOException e) {
            logger.error("Load MariaDB connection failed..." + e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error("close inputstream error " + e);
            }
        }

        return false;
    }

    public void executeSQL(List<String> sqlLine) {
        if (this.connection == null) {
            initConnect();
        }

        logger.info("Going to run SQLs " + sqlLine.toString());
        if (sqlLine.size() == 0) {
            return;
        }

        for (String command : sqlLine) {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = connection.createStatement();
                boolean hasResults = false;
                try {
                    hasResults = stmt.execute(command.toString());
                } catch (final SQLException e) {
                    e.fillInStackTrace();
                    logger.error("Error executing SQL Command: \"" +
                            command + "\"" + e);
                    continue;
                }

                rs = stmt.getResultSet();
                if (hasResults && rs != null) {
                    List<String> headerRow = new ArrayList<String>();
                    List<List<String>> toupleList = new ArrayList<List<String>>();
                    // Print & Store result column names
                    final ResultSetMetaData md = rs.getMetaData();
                    final int cols = md.getColumnCount();
                    StringBuffer out = new StringBuffer(128);
                    for (int i = 0; i < cols; i++) {
                        final String name = md.getColumnLabel(i + 1);
                        out.append(name + "\t");
                        headerRow.add(name);
                    }
                    // Print & Store result rows
                    while (rs.next()) {
                        List<String> touple = new ArrayList<String>();
                        for (int i = 1; i <= cols; i++) {
                            final String value = rs.getString(i);
                            out.append(value + "\t");
                            touple.add(value);
                        }
                        out.append("");
                        toupleList.add(touple);
                    }
                    logger.info("\r\tout put for command:" + command);
                    logger.info(headerRow);
                    logger.info(out.toString());
                    logger.info(toupleList);
                } else {
                    logger.info(stmt.getUpdateCount() + " row(s) affected.");
                }
            } catch (SQLException e) {
               logger.error("Execute SQL error ..." + e);
            }
        }
    }

    public String getDEFAULT_CONNECTION() {
        return DEFAULT_CONNECTION;
    }

    public void setDEFAULT_CONNECTION(String DEFAULT_CONNECTION) {
        this.DEFAULT_CONNECTION = DEFAULT_CONNECTION;
        USER_CONFIG = true;
    }
}
