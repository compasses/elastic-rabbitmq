package elasticsearch.exportimport;

import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by I311352 on 4/24/2017.
 */
@SpringBootApplication
public class mainCrtl {
    private static final Logger logger = Logger.getLogger(mainCrtl.class);

    public static void main(String args[]) {
        SpringApplication.run(mainCrtl.class, args);

    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            if (System.getProperty("config.properties") != null) {
                logger.info("Use user config " + System.getProperty("config.properties"));
                try {
                    File file = new File(System.getProperty("config.properties"));
                    if (file.exists()) {
                        FileInputStream inputStream = new FileInputStream(file);
                        Properties props = new Properties();
                        props.load(inputStream);

                        String fileName = props.getProperty("SAVE_FILE");
                        String eshost = props.getProperty("ES_HOST");
                        String buket = props.getProperty("S3_BUKET");
                        Long tenantId = Long.parseLong(props.getProperty("TENANTID"));

                        logger.info("going to export data fileName=" + fileName +" ESHOST="+eshost + " buket="+buket
                        +" tenantId=" +tenantId);

                        LoadData loadData = new LoadData();
                        loadData.saveData(fileName, tenantId, buket, eshost);
                    }
                } catch (IOException e) {
                    logger.error("file load fail "+e);
                }
            }

            System.exit(0);
        };
    }
}
