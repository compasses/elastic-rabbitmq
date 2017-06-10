package elasticsearch.searchservice.dsl;

import java.util.HashMap;
import java.util.List;

/**
 * Created by i311352 on 5/8/2017.
 */
public class DSLMeta {
    private String key;
    private List<String> value;
    private KeyType keyType;
    private ValType valueType;
    private String  operator;
    private String nestPath;
    private HashMap<String, List<String>> sysAttributes;


    public DSLMeta() {
    }

    public DSLMeta(String key, List<String> value, KeyType keyType, ValType valueType) {
        this.key = key;
        this.value = value;
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public DSLMeta(HashMap<String, List<String>> sysAttributes, KeyType keyType, ValType valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.sysAttributes = sysAttributes;
    }

    public enum KeyType {
        NORMAL, NESTED
    }

    public enum ValType {
        STRING, DATE, NUMERIC, IDLIST, WILDCARD, PREFIX
    }

    public enum OPERATOR {
        EQ, LE, GE
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public ValType getValueType() {
        return valueType;
    }

    public void setValueType(ValType valueType) {
        this.valueType = valueType;
    }

    public String getNestPath() {
        return nestPath;
    }

    public void setNestPath(String nestPath) {
        this.nestPath = nestPath;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public HashMap<String, List<String>> getSysAttributes() {
        return sysAttributes;
    }

    public void setSysAttributes(HashMap<String, List<String>> sysAttributes) {
        this.sysAttributes = sysAttributes;
    }
}
