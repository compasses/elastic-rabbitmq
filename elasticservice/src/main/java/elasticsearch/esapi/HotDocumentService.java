package elasticsearch.esapi;

/**
 * Created by I311352 on 10/25/2016.
 */

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import elasticsearch.esapi.resp.ESGetByIdResponse;
import elasticsearch.esapi.resp.ESSaveResponse;
import elasticsearch.exception.ElasticVersionConflictException;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * Some document maybe processed by multi-thread, need take care for update conflict
 * or override
 */
@Component
public class HotDocumentService {
    private final static Logger logger = Logger.getLogger(HotDocumentService.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private CounterService counterService;

    @Autowired
    private GaugeService gaugeService;

    /**
     * @param index
     * @param type
     * @param sourceId
     * @param param    depends on have the key of version
     * @param newObj
     * @param f        ConflictHandleFunction
     * @return
     */
    public ESSaveResponse update(String index, String type, Long sourceId, HashMap<String, String> param,
                                 JsonObject newObj, BiFunction<JsonObject, JsonObject, JsonObject> f) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Status successful = Status.SUCCESS;

        try {
            //In case the arguments pollute
            HashMap<String, String> localParam = new HashMap<>();
            if (param != null) {
                localParam.putAll(param);
            }
            if (localParam.get("version") != null && localParam.get("version").equals("-1")) {
                localParam.remove("version");
                return createSource(index, type, sourceId, localParam, newObj);
            } else if (localParam.get("version") != null) {
                logger.debug("do update with version" + localParam + " source=" + newObj.toString());
                return updateOnInsert(index, type, sourceId, localParam, newObj);
            } else {
                ESGetByIdResponse esResponse = documentService.loadSourceById(index, type, sourceId, localParam);
                if (esResponse == null || esResponse.getFound() == false) {
                    return createSource(index, type, sourceId, localParam, newObj);
                } else {
                    logger.debug("Retrieval source " + esResponse.toString());
                    localParam.put("version", esResponse.getVersion().toString());
                    return updateOnInsert(index, type, sourceId, localParam, newObj);
                }
            }
        } catch (ElasticVersionConflictException e) {
            try {
                successful = Status.CONFILICT;
                //conflict
                //In case the arguments pollute
                HashMap<String, String> localParam = new HashMap<>();
                if (param != null) {
                    localParam.putAll(param);
                }
                logger.warn("Conflict happen! type = " + type + " sourceId = " + sourceId);
                TimeUnit.MICROSECONDS.sleep(300);

                //need retrieve new version
                localParam.remove("version");
                ESGetByIdResponse esResponse = documentService.loadSourceById(index, type, sourceId, localParam);
                if (esResponse == null) {
                    logger.error("impossible is happen! need check data " + " type:" + type + " id" + sourceId);
                    throw new IllegalStateException();
                }

                JsonObject changedObj = f.apply(esResponse.getObject(), newObj);
                localParam.put("version", esResponse.getVersion().toString());
                return updateOnInsert(index, type, sourceId, localParam, changedObj);
            } catch (InterruptedException ex) {
                successful = Status.FAIL;
                throw new IllegalStateException(ex);
            } finally {
                stopWatch.stop();
                String metricName = this.getClass().getSimpleName() + "." + type + "." + sourceId;

                if (successful.equals(Status.SUCCESS)) {
                    counterService.increment(metricName + "." + ".success");
                } else if (successful.equals(Status.CONFILICT)) {
                    counterService.increment(metricName + "." + ".conflict");
                } else {
                    counterService.increment(metricName + "." + ".fail");
                }
                gaugeService.submit(metricName, stopWatch.getTime());
                logger.info("HotDocumentService sync end on " + newObj.toString() + ", execTime[" + stopWatch.getTime()+ "]");
            }
        }
    }

    public ESSaveResponse updateOnInsert(String index, String type, Long sourceId, Map<String, String> params, JsonObject obj) {
        JsonObject updateObj = new JsonObject();
        updateObj.add("doc", obj);
        updateObj.add("doc_as_upsert", new JsonPrimitive(true));

        HttpEntity requestBody = new StringEntity(updateObj.toString(), Charset.defaultCharset());
        return documentService.update(index, type, sourceId, params, requestBody);
    }

    private ESSaveResponse createSource(String index, String type, Long sourceId, Map<String, String> params, JsonObject obj) {
        params.put("op_type", "create");
        HttpEntity requestBody = new StringEntity(obj.toString(), Charset.defaultCharset());
        return documentService.Store(index, type, sourceId, params, requestBody);
    }

    public enum Status {
        SUCCESS, CONFILICT, FAIL
    }
}
