import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;
import rabbitmq.ListenerJobBuilder;
import rabbitmq.MQListenerAdmin;

/**
 * Created by I311352 on 12/28/2016.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan({ "elasticsearch","rabbitmq", "sync" })
public class ElasticRabbitApp implements CommandLineRunner {
    @Autowired
    private ConfigurableApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(ElasticRabbitApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        MQListenerAdmin listenerAdmin = ListenerJobBuilder.buildMQListenerAdmin(context);
        listenerAdmin.start();
    }
}
