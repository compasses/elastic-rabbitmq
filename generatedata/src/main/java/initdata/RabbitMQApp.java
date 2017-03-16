package initdata;

/**
 * Created by I311352 on 11/23/2016.
 */
import initdata.publish.MessagePublishService;
import initdata.sqlexecute.SQLScriptExecuteService;
import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RabbitMQApp {
    private static final Logger logger = Logger.getLogger(RabbitMQApp.class);

    public static void main(String[] args) {
        SpringApplication.run(RabbitMQApp.class, args);
    }

    private void publishMessageTask(ApplicationContext ctx) {
        logger.info("Start publish message...");
        MessagePublishService publishService = ctx.getBean(MessagePublishService.class);
        if (System.getProperty("rabbit.cfg") != null) {
            logger.info("Use user config rabbit: " + System.getProperty("rabbit.cfg"));
            publishService.setRabbitProperties(System.getProperty("rabbit.cfg"));
        }
        if (System.getProperty("message.src") != null) {
            logger.info("Use user message source: " + System.getProperty("message.src"));
            publishService.setMessageSource(System.getProperty("message.src"));
        }
        publishService.publish();
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            publishMessageTask(ctx);
//            SQLScriptExecuteService sqlScriptExecuteService = ctx.getBean(SQLScriptExecuteService.class);
//            if (System.getProperty("sql.source") != null) {
//                sqlScriptExecuteService.setDefaultSqlSource(System.getProperty("sql.source"));
//            }
//
//            //sqlScriptExecuteService.runSQL();
//            sqlScriptExecuteService.sendMsgToRabbit();
            System.exit(0);
        };
    }
}
