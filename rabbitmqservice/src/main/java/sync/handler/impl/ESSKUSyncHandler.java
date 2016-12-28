package sync.handler.impl;

import com.google.gson.JsonObject;
import elasticsearch.constant.ESConstants;
import elasticsearch.esapi.resp.ESGetByIdResponse;
import sync.common.ESHandleMessage;
import sync.common.OperateAction;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by I311352 on 10/17/2016.
 */
public class ESSKUSyncHandler extends ESAbstractHandler{

    @Override
    public boolean handleCreate(ESHandleMessage message) {
        JsonObject object = message.getJsonElement().getAsJsonObject();
        Long skuId = object.get("id").getAsLong();

        //sku body still contains product info, need delete
        if (object.get("product") == null) {
            // not valid message just pass
            logger.error("Invalid message for SKU Sync");
            message.setNeedSendEvent(false);
            return true;
        }

        JsonObject product = object.get("product").getAsJsonObject();
        Long productId = product.get("id").getAsLong();
        object.addProperty("parentId", productId);
        object.remove("product");

        HashMap<String, String> params = new HashMap<>();
        params.put("parent", productId.toString());

        // check the version
        Long newVersion = object.get("version").getAsLong();
        ESGetByIdResponse response = loadDocumentWithVersion(skuId, ESConstants.SKU_TYPE);
        if (response == null || response.getFound() != true) {
            logger.info("The first come message body");
            params.put("version", "-1");
        } else {
            // check the version
            Long currentVersion = response.getObject().get("version") == null ? -1 :
                    response.getObject().get("version").getAsLong();
            if (currentVersion >= newVersion) {
                // need ignore it
                logger.info("Older Version come in last.");
                message.setNeedSendEvent(false);
                return true;
            } else {
                params.put("version", response.getVersion().toString());
            }
        }

        hotDocumentService.update(ESConstants.STORE_INDEX, ESConstants.SKU_TYPE,
                skuId, params, object, this.conflictFunction());

        message.setNeedSendEvent(true);
        return true;
    }

    @Override
    public boolean handleUpdate(ESHandleMessage message) {
        return handleCreate(message);
    }

    @Override
    public boolean handleDelete(ESHandleMessage message) {
        Long skuId = jsonParser.parse(new String(message.getMsgBody())).getAsLong();
        logger.info("Handle Delete SKU msg id is ." + skuId);

        documentService.Delete(ESConstants.STORE_INDEX, ESConstants.SKU_TYPE,
                skuId, null);

        return true;
    }

    @Override
    public boolean postHandleMessage(ESHandleMessage message) {
        if (!message.isNeedSendEvent()) {
            return true;
        }

        Long  productId;
        if (message.getAction() == OperateAction.DELETE) {
            productId = jsonParser.parse(new String(message.getMsgBody())).getAsLong();
            logger.info("Handle Delete id is ." + productId);
        } else {
            productId = message.getJsonElement()
                             .getAsJsonObject()
                             .get("parentId").getAsLong();
        }

        ArrayList<Long> productIds = new ArrayList<>();
        productIds.add(productId);
        message.setProductIds(productIds);
        return super.postHandleMessage(message);
    }

}
