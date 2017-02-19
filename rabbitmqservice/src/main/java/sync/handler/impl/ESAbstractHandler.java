package sync.handler.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elasticsearch.ESQueryBodyBuilder;
import elasticsearch.constant.ESConstants;
import elasticsearch.esapi.DocumentService;
import elasticsearch.esapi.HotDocumentService;
import elasticsearch.esapi.resp.ESGetByIdResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import sync.common.ESHandleMessage;
import sync.common.OperateAction;
import sync.handler.ESHandler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by I311352 on 10/18/2016.
 */
public abstract class ESAbstractHandler implements ESHandler {
    public static final Logger logger = Logger.getLogger(ESAbstractHandler.class);
    protected JsonParser jsonParser = new JsonParser();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").
            withZone(ZoneId.systemDefault());

    private final String Light_Product_Routing_KEY = "LightProduct.SYNC";

    public DocumentService documentService;
    // another choice, if your data is hot.
    public HotDocumentService hotDocumentService;

    public void onMessage(ESHandleMessage message) {
        logger.info("Going to handle message type:" + message.getType() + " searchcommand:" + message.getAction());
        boolean execResult = preHandleMessage(message);
        if (execResult != true) {
            logger.warn("PreHandleMessage failed: " + message.toString());
            //
            throw new IllegalStateException("PreHandleMessage failed in ESAbstractHandler");
        }

        switch (message.getAction()) {
            case UPDATE:
                execResult = handleUpdate(message);
                break;
            case CREATE:
                execResult = handleCreate(message);
                break;
            case DELETE:
                execResult = handleDelete(message);
                break;
            case ASSOCIATE:
                execResult = handleAssociate(message);
                break;
            case DISASSOCIATE:
                execResult = handleDisAssociate(message);
                break;
            case LIST:
                execResult = handleList(message);
                break;
            case UNLIST:
                execResult = handleUnlist(message);
                break;
            default:
                throw new IllegalArgumentException("cannot  support this searchcommand " + message.getAction());
        }

        // should do some searchcommand, now just throw exception
        if (execResult != true) {
            throw new IllegalArgumentException("process failed for type and searchcommand " + message.getType() +
                    message.getAction());
        }

        // left behind process
        postHandleMessage(message);
    }

    public boolean preHandleMessage(ESHandleMessage message) {
        // delete message not use the data
        if (message.getAction() == OperateAction.DELETE) {
            return true;
        }
        JsonElement element = jsonParser.parse(new String(message.getMsgBody()));
        message.setJsonElement(element);

        // main BO can handle version number
        if (!element.isJsonObject()) {
            return true;
        }

        try {
            JsonObject jsonObject = element.getAsJsonObject();
            // the object has version number
            if (jsonObject.get("version") != null) {
                return true;
            }

            String timeStr = "";
            if (jsonObject.get("updateTime") != null) {
                timeStr = jsonObject.get("updateTime").getAsString();
            } else if (jsonObject.get("time") != null) {
                timeStr = jsonObject.get("time").getAsString();
            } else {
                // special case, need handle in handler
                return true;
            }
            LocalDateTime localDateTime = LocalDateTime.parse(timeStr, formatter);
            long version = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            jsonObject.addProperty("version", version);
            message.setJsonElement(jsonObject);
            return true;
        } catch (NumberFormatException ix) {
            logger.error("Invalid dateTime");
        }
        // need handler do some special handle on message body miss update time or not a json object.
        return true;
    }

    public boolean postHandleMessage(ESHandleMessage message) {

        return true;
    }

    protected BiFunction<JsonObject, JsonObject, JsonObject> conflictFunction() {
        return (JsonObject currentObj, JsonObject newObj) -> {
            if (currentObj.get("version").getAsLong() > newObj.get("version").getAsLong()) {
                return currentObj;
            } else {
                return newObj;
            }
        };
    }

    protected ESGetByIdResponse loadDocumentWithVersion(Long sourceId, String type) {
        String fields = new ESQueryBodyBuilder.SourceFieldsListBuilder().addField("version").build();
        return documentService.loadSourceSpecifyField(ESConstants.STORE_INDEX, type,
                sourceId, null, fields);
    }

    public boolean handleUpdate(ESHandleMessage message) {
        return false;
    }

    public boolean handleCreate(ESHandleMessage message) {
        return false;
    }

    public boolean handleDelete(ESHandleMessage message) {
        return false;
    }

    public boolean handleAssociate(ESHandleMessage message) {
        return false;
    }

    public boolean handleDisAssociate(ESHandleMessage message) {
        return  false;
    }

    public boolean handleList(ESHandleMessage message) {
        return false;
    }

    public boolean handleUnlist(ESHandleMessage message) {
        return false;
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public HotDocumentService getHotDocumentService() {
        return hotDocumentService;
    }

    public void setHotDocumentService(HotDocumentService hotDocumentService) {
        this.hotDocumentService = hotDocumentService;
    }
}
