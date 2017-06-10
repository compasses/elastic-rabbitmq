package elasticsearch.searchservice.config;

import elasticsearch.searchservice.ESService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Created by I311352 on 10/18/2016.
 */
@Configuration
public class ElasticServiceConfig extends WebMvcConfigurerAdapter {
    public static final String LOCAL_CONFIG_PATH = "META-INF/resources/es.properties";
    public static final String PRODUCTION_CONFIG_PATH = "/etc/secrets/es/es.properties";
    private static final Logger logger = LoggerFactory.getLogger(ElasticServiceConfig.class);

    @Bean
    public ESConnectionConfig esConnectionConfig(){
        Properties properties = loadProperties(LOCAL_CONFIG_PATH, PRODUCTION_CONFIG_PATH);

        ESConnectionConfig config = new ESConnectionConfig();
        config.setEsDatabase(StringUtils.trimToNull(properties.getProperty("ES_DATABASE")));
        config.setEsHost(StringUtils.trimToNull(properties.getProperty("ES_HOST")));
        config.setEsPort(Integer.parseInt(StringUtils.trimToNull(properties.getProperty("ES_PORT"))));
        return config;
    }

    @Bean(name = {"ESService"})
    @Primary
    public ESService elasticRestClient(ESConnectionConfig config) {

        String host = "http://" + config.getEsHost() + ":" + config.getEsPort();
        ESService esService = new ESService(host);
        return esService;
    }

    public Properties loadProperties(String localPath, String productivePath) {
        Properties props = new Properties();
        File file = new File(productivePath);
        Object inputStream = null;

        try {
            if(!file.exists()) {
                logger.error("Can't find the properties file {}, this will leads to using local properties file {}.", productivePath, localPath);
            } else {
                inputStream = new FileInputStream(file);
            }

            props.load((InputStream)inputStream);
        } catch (Exception e) {
            logger.warn("Load resource properties error.", e);
        } finally {
            if(null != inputStream) {
                try {
                    ((InputStream)inputStream).close();
                } catch (IOException e) {
                    logger.warn("Resource input stream close error.", e);
                }
            }

        }

        return props;
    }
}