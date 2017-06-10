package elasticsearch.searchservice.dsl;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import elasticsearch.searchservice.models.Meta;
import elasticsearch.searchservice.models.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * Created by i311352 on 5/3/2017.
 */
@Service
public class DLSGenerateServiceImpl implements DSLGenerateService {
    private static final Logger logger = LoggerFactory.getLogger(DLSGenerateServiceImpl.class);
    private static String SYSFIELD = "systemField";
    private static String UDFFIELD = "customField";
    private static String VARIANT  = "sku.variantList.variantValueId";
    private static String FULLSEARCH = "keywords";
    private static String PRICEFIELD = "channels.prices.price";
    private static String CATEGORY = "category.order";
    private static String ATTRIBUTES = "attributes";

    private final PebbleEngine engine = new PebbleEngine.Builder().build();

    @Override
    public String fromQueryParam(QueryParam param) {
        try {
            PebbleTemplate compiledTemplate;
            if (param.getFilter().size() == 1 && param.getFilter().get(0).getKey().equals(FULLSEARCH)) {
                // full text search
                compiledTemplate = engine.getTemplate("templates/search.dsl.twig");
                Writer writer = new StringWriter();
                Map<String, Object> context = new HashMap<>();
                context.put("search_string", String.join(" ",param.getFilter().get(0).getValue()));
                context.put("channelId", param.getChannelId());
                context.put("from", param.getOffSet().toString());
                context.put("size", param.getPageSize().toString());
                compiledTemplate.evaluate(writer, context);
                return writer.toString();
            } else {
                if (param.getCount() == true) {
                    compiledTemplate = engine.getTemplate("templates/count.dsl.twig");
                } else {
                    compiledTemplate = engine.getTemplate("templates/query.dsl.twig");
                }
                Writer writer = new StringWriter();
                Map context = buildContext(param);
                compiledTemplate.evaluate(writer, context);
                return writer.toString();
            }
        } catch (PebbleException e) {
            logger.error("Get pebble exception " + e);
        } catch (IOException e) {
            logger.error("Get compile template error " + e);
        }

        return null;
    }

    private Map buildContext(QueryParam param) {
        Map<String, Object> context = new HashMap<>();
        List<DSLMeta> filter = new ArrayList<>();

        if (param.getFilter().size() > 0) {
            fillDSLQueryMeta(param.getFilter(), filter);
        }

        if (param.getCollection() != null && param.getCollection().getConditions().size() > 0) {
            if (param.getCollection().getConditionType().equals("AND")) {
                fillDSLQueryMeta(param.getCollection().getConditions(), filter);
            } else {
                // or condition
                List<DSLMeta> orcondition = new ArrayList<>();
                fillDSLQueryMeta(param.getCollection().getConditions(), orcondition);
                if (orcondition.size() > 0) {
                    context.put("collectionForOr", orcondition);
                }
            }
        }

        context.put("filter", filter);
        String orderFiled = param.getOrderByField();
        if (orderFiled.startsWith("price_")) {
            context.put("orderByField", PRICEFIELD);
        } else {
            // use arrival time to do it
            context.put("orderByField", "updateTime");
        }

        context.put("orderByOrder", param.getOrderByOrder().toLowerCase());
        context.put("channelId", param.getChannelId());
        context.put("from", param.getOffSet().toString());
        context.put("size", param.getPageSize().toString());

        return context;
    }

    private void fillDSLQueryMeta(List<Meta> metas, List<DSLMeta> dslMetas) {
        for(Meta meta : metas) {
            if (meta.getKey().startsWith("sysfield_")) {
                String[] keys = meta.getKey().split("_");
                if (keys.length < 2) continue;

                if (keys[1].startsWith("attribute")) {
                    amendAttributesMeta(dslMetas, keys[1], meta.getValue());
                } else {
                    DSLMeta m = new DSLMeta(SYSFIELD+"."+keys[1], meta.getValue(), DSLMeta.KeyType.NORMAL,
                            DSLMeta.ValType.STRING);
                     m = amendMetaForTextSearch(m, meta.getOperator());
                    dslMetas.add(m);
                }

            } else if (meta.getKey().startsWith("udf_ext_default_UDF")) {
                DSLMeta m  = new DSLMeta(UDFFIELD+"."+meta.getKey().substring(4), meta.getValue(), DSLMeta.KeyType.NORMAL,
                        DSLMeta.ValType.STRING);
                dslMetas.add(m);
            } else if (meta.getKey().startsWith("variant_")) {
                DSLMeta m  = new DSLMeta(VARIANT, meta.getValue(), DSLMeta.KeyType.NESTED,
                        DSLMeta.ValType.IDLIST);
                m.setNestPath("sku");
                dslMetas.add(m);
            } else if (meta.getKey().startsWith("price_")) {
                DSLMeta m  = new DSLMeta(PRICEFIELD, meta.getValue(), DSLMeta.KeyType.NESTED,
                        DSLMeta.ValType.NUMERIC);
                m.setNestPath("channels");
                m.setOperator(getNumericOperator(meta.getOperator()));
                dslMetas.add(m);
            } else if (meta.getKey().equals("post_date")) {
                String date = meta.getValue().get(0);
                date = date.replaceAll(" ", "T");
                date += ".000Z";

                DSLMeta m  = new DSLMeta("updateTime", Arrays.asList(date), DSLMeta.KeyType.NORMAL,
                        DSLMeta.ValType.DATE);
                m.setOperator(getNumericOperator(meta.getOperator()));
                dslMetas.add(m);
            } else if (meta.getKey().equals("post_title")) {
                DSLMeta m  = new DSLMeta(SYSFIELD+".name",meta.getValue(), DSLMeta.KeyType.NORMAL,
                        DSLMeta.ValType.STRING);
                m = amendMetaForTextSearch(m, meta.getOperator());
                dslMetas.add(m);
            } else if (meta.getKey().equals("category")) {
                DSLMeta m  = new DSLMeta(CATEGORY, meta.getValue(), DSLMeta.KeyType.NORMAL,
                        DSLMeta.ValType.PREFIX);
                m = amendMetaForTextSearch(m, meta.getOperator());
                dslMetas.add(m);
            } else {
                logger.warn("Not recognize key  " + meta);
            }
        }
    }

    private DSLMeta amendMetaForTextSearch(DSLMeta m, String oper) {
        switch (oper.toLowerCase()) {
            case "starts_with":
                m.setValue(Arrays.asList(m.getValue().get(0).toLowerCase()+"*"));
                m.setValueType(DSLMeta.ValType.WILDCARD);
                return m;
            case "ends_with":
                m.setValue(Arrays.asList("*" + m.getValue().get(0).toLowerCase()));
                m.setValueType(DSLMeta.ValType.WILDCARD);
                return m;
            case "like":
                m.setValue(Arrays.asList("*" + m.getValue().get(0).toLowerCase() +"*"));
                m.setValueType(DSLMeta.ValType.WILDCARD);
                return m;
            default:
                return m;
        }
    }

    private void amendAttributesMeta(List<DSLMeta> metas, String key, List<String> value) {
        DSLMeta attributeMeta = null;
        for (DSLMeta meta: metas) {
            if (meta.getNestPath().equals(ATTRIBUTES)) {
                attributeMeta = meta;
                break;
            }
        }
        // metas.stream().filter(m -> m.getNestPath().equals(ATTRIBUTES)).findFirst().get();
        if (attributeMeta == null) {
            HashMap<String, List<String>> values = new HashMap<>();
            values.put(ATTRIBUTES+"." + key, value);
            attributeMeta = new DSLMeta(values, DSLMeta.KeyType.NESTED,
                    DSLMeta.ValType.STRING);
            attributeMeta.setNestPath(ATTRIBUTES);
            metas.add(attributeMeta);
        } else {
            HashMap<String, List<String>> values = attributeMeta.getSysAttributes();
            String k = ATTRIBUTES+"." + key;
            if (values.containsKey(k)) {
                List<String> v = values.get(k);
                v.addAll(value);
                values.put(k, v);
            } else {
                values.put(k, value);
            }
            attributeMeta.setSysAttributes(values);
        }
    }

    private DSLMeta.ValType getValueTypeByOperator(String oper) {
        switch (oper) {

        }

        return DSLMeta.ValType.STRING;
    }

    private String getNumericOperator(String oper) {
        switch (oper) {
            case "<":
                return "lt";
            case ">":
                return "gt";
            case ">=":
                return "gte";
            case "<=":
                return "lte";
            default:
                return "=";
        }
    }

    private void fillESQueryMeta(List<Meta> metas, Map<String, List<String>> filter) {
        for(Meta meta : metas) {
            if (meta.getKey().startsWith("sysfield_")) {
                String[] keys = meta.getKey().split("_");
                if (keys.length < 2) continue;
                filter.put(SYSFIELD+"."+keys[1], meta.getValue());
            } else if (meta.getKey().startsWith("udf_ext_default_UDF")) {
                filter.put(UDFFIELD+"."+meta.getKey(), meta.getValue());
            } else if (meta.getKey().startsWith("variant_")) {
                filter.put(VARIANT, meta.getValue());
            } else {
                logger.warn("Not recognize key  " + meta);
            }
        }
    }
}
