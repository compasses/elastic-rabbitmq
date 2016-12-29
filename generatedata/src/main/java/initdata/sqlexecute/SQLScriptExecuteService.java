package initdata.sqlexecute;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import initdata.sqlexecute.SQLExecute.MariaDBSQLExecute;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by I311352 on 12/1/2016.
 */
@Service
public class SQLScriptExecuteService {
    private static final Logger logger = Logger.getLogger(SQLScriptExecuteService.class);
    private static String DEFAULT_SQL_SOURCE = "/sql/data.sql";
    private static boolean USER_CONFIG = false;

    private static final String DEFAULT_DELIMITER = ";";
    private static final String COMMENTS = "--";


    private List<String> sqlLine = new ArrayList<>();

    @Autowired
    private MariaDBSQLExecute mariaDBSQLExecute;

    public void runSQL() {
        if (!loadSQLScriptLine()) {
            logger.error("Load file fail...do nothing");
            return;
        }

        mariaDBSQLExecute.executeSQL(sqlLine);
    }

    public void sendMsgToRabbit() throws TimeoutException, IOException {
        mariaDBSQLExecute.publish();
    }

    private boolean loadSQLScriptLine() {
        BufferedReader bufferedReader = null;
        try {
            if (USER_CONFIG == true) {
                bufferedReader = new BufferedReader(new FileReader(DEFAULT_SQL_SOURCE));
            } else {
                bufferedReader = new BufferedReader(new FileReader(this.getClass().getResource(DEFAULT_SQL_SOURCE).getFile()));
            }

            String line;
            StringBuffer stringBuffer = null;
            while ((line = bufferedReader.readLine()) != null) {
                logger.debug("Got sql ..." + line);
                String trimLine = line.trim();
                if (trimLine.isEmpty() || trimLine.startsWith(COMMENTS)) {
                    continue;
                }

                if (stringBuffer == null) {
                    stringBuffer = new StringBuffer(64);
                }

                if (trimLine.endsWith(DEFAULT_DELIMITER)) {
                    stringBuffer.append(" ").append(trimLine);
                    sqlLine.add(stringBuffer.toString());
                    stringBuffer = null;
                } else {
                    stringBuffer.append(" ").append(trimLine);
                }
            }
            return true;
        } catch (IOException e) {
            logger.error("Error reading sql file...");
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                logger.error("close error " + e);
            }
        }

        return false;
    }

    public static void setDefaultSqlSource(String defaultSqlSource) {
        DEFAULT_SQL_SOURCE = defaultSqlSource;
        USER_CONFIG = true;
    }

}
