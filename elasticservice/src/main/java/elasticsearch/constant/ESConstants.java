package elasticsearch.constant;

import java.util.regex.Pattern;

/**
 * Created by I311352 on 10/17/2016.
 */
public class ESConstants {
    public      static  final   String STORE_INDEX = "stores";
    public      static  final   String PRODUCT_TYPE = "product";
    public      static  final   String SKU_TYPE = "sku";
    public      static  final   String PRODUCT_PROPERTY = "productproperty";
    public      static  final   String CHANNEL_TYPE = "channels";
    public      static  final   int SNIFFER_INTERVAL = 15000;
    public      static  final   int RESTCLIENT_TIMEOUT = 20000; // 20s


    public      static  final   Pattern ROUTINGKEY_PATTERN = Pattern.compile("([\\w\\.]+)\\.(\\w+)\\.(\\d+)");

}
