package elasticsearch;

import com.google.gson.*;
import org.elasticsearch.index.query.*;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by I311352 on 10/18/2016.
 */
public final class ESQueryBodyBuilder {
    private static final Logger log = Logger.getLogger(ESQueryBodyBuilder.class);

    public static class SimpleUpdateBodyBuilder {
        JsonObject updateDoc = new JsonObject();
        JsonObject updateBody = new JsonObject();

        public SimpleUpdateBodyBuilder addField(String fieldName, JsonElement element) {
            this.updateDoc.add(fieldName, element);
            return this;
        }
        public JsonObject build() {
            updateBody.add("doc", updateDoc);
            return updateBody;
        }
    }

    public static class SourceFieldsListBuilder {
        String fields = "";

        public SourceFieldsListBuilder addField(String fieldName) {
            if (fields.isEmpty()) {
                fields = fieldName;
            } else {
                fields += "," + fieldName;
            }
            return this;
        }

        public String build() {
            return this.fields;
        }

    }

    /**
     * example query body: {
     "query": {
     "bool": {
     "must": [
     { "term": { "version": 1}},
     { "term": { "id": 77445388189696}}
     ]
     }
     }
     }
     */
    public static class MultiFieldFilterDSLBuilder {
        JsonObject queryObj = new JsonObject();
        JsonObject boolField = new JsonObject();
        JsonArray mustArray = new JsonArray();
        JsonObject termFilter = new JsonObject();

        public MultiFieldFilterDSLBuilder addTermFiled(String fieldName, JsonPrimitive val) {
            JsonObject newTerm = new JsonObject();
            newTerm.add(fieldName, val);
            JsonObject termFilter = new JsonObject();
            termFilter.add("term", newTerm);

            mustArray.add(termFilter);
            return this;
        }

        public JsonObject build() {
            boolField.add("must", mustArray);
            queryObj.add("bool", boolField);
            termFilter.add("query", queryObj);
            return termFilter;
        }
    }

    public static class SimpleQueryDSLBuilder {
        JsonObject match = new JsonObject();
        JsonObject query = new JsonObject();
        JsonObject matchMeta = new JsonObject();
        JsonArray  source = new JsonArray();

        public SimpleQueryDSLBuilder addMatch(String fieldName, JsonPrimitive primitive) {
            this.matchMeta.add(fieldName, primitive);
            return this;
        }

        public SimpleQueryDSLBuilder addSource(JsonPrimitive primitive) {
            this.source.add(primitive);
            return this;
        }

        public JsonObject build() {
            query.add("_source", source);
            match.add("match", matchMeta);
            query.add("query", match);
            log.debug("query json: " + query.toString());
            return query;
        }
    }

    public static class SimpleQueryMatchBuilder {
        JsonObject match = new JsonObject();
        JsonObject query = new JsonObject();
        JsonObject matchMeta = new JsonObject();

        public SimpleQueryMatchBuilder addMatch(String fieldName, JsonPrimitive primitive) {
            this.matchMeta.add(fieldName, primitive);
            return this;
        }

        public JsonObject build() {
            match.add("match", matchMeta);
            query.add("query", match);
            log.debug("query json: " + query.toString());
            return query;
        }
    }

    public static JsonObject getIdsQueryJsonObject(String type, JsonArray idList) {
        IdsQueryBuilder queryBuilder = QueryBuilders.idsQuery(type);

        idList.forEach(id->{
            queryBuilder.addIds(id.toString());
        });

        String queryBody = queryBuilder.toString();
        Gson gson = new Gson();
        JsonObject queryJsonObject = gson.fromJson(queryBody.toString(), JsonObject.class);

        JsonObject query = new JsonObject();
        query.add("query", queryJsonObject);
        log.debug("query json: " + query.toString());
        return query;
    }

    public static JsonObject getIdsQueryJsonObject(String type, List<Long> idList) {
        IdsQueryBuilder queryBuilder = QueryBuilders.idsQuery(type);

        for (Long id : idList) {
            queryBuilder.addIds(id.toString());
        }

        String queryBody = queryBuilder.toString();
        Gson gson = new Gson();
        JsonObject queryJsonObject = gson.fromJson(queryBody.toString(), JsonObject.class);

        JsonObject query = new JsonObject();
        query.add("query", queryJsonObject);
        log.debug("query json: " + query.toString());
        return query;
    }

    public static JsonObject getHasParentQueryJsonObject(String type, List<Long> idList) {

        IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery(type);

        for (Long id : idList) {
            idsQueryBuilder.addIds(id.toString());
        }

        HasParentQueryBuilder queryBuilder = QueryBuilders.hasParentQuery(type, idsQueryBuilder, false);

        String queryBody = queryBuilder.toString();
        Gson gson = new Gson();
        JsonObject queryJsonObject = gson.fromJson(queryBody.toString(), JsonObject.class);

        JsonObject query = new JsonObject();
        query.add("query", queryJsonObject);
        log.debug("query json: " + query.toString());
        return query;
    }

    public static JsonObject getHasChildQueryJsonObject(String type, List<Long> idList) {
        JsonObject query = new JsonObject();
        JsonObject hasChildQuery = new JsonObject();
        JsonObject idsQuery = getIdsQuery(idList);
        hasChildQuery.add("has_child", idsQuery);
        hasChildQuery.get("has_child").getAsJsonObject().add("type",new JsonPrimitive(type));

        query.add("query",hasChildQuery);
        return query;
    }

    public static JsonObject getIdsQuery(List<Long> idList){
        JsonObject query = new JsonObject();
        JsonObject idsQuery =new JsonObject();
        JsonObject valueQuery = new JsonObject();
        JsonArray idsArray = new JsonArray();
        idList.forEach( id ->{
           idsArray.add(new JsonPrimitive(id));
        });
        valueQuery.add("values",idsArray);
        idsQuery.add("ids",valueQuery);
        query.add("query",idsQuery);
        return query;
    }

}
