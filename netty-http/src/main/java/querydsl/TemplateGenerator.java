package querydsl;

import http.elasticaction.SearchDSL;
import http.message.QueryMeta;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.DefaultEnvironmentConfiguration;
import org.jtwig.environment.Environment;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentFactory;
import org.jtwig.resource.reference.ResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by I311352 on 3/8/2017.
 */
public class TemplateGenerator implements SearchDSL<String> {
    private final static Logger logger = LoggerFactory.getLogger(TemplateGenerator.class);
    private QueryMeta meta;


    public TemplateGenerator(QueryMeta meta) {
        this.meta = meta;
    }

    @Override
    public String getDSL() {
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/filters.twig");
        JtwigModel model = JtwigModel.newModel().with("var", "World");
        String result = template.render(model);
        logger.info("get search dsl " + result);
        return result;
    }
}
