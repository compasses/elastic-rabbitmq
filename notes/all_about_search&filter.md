## ElasticSearch 深入理解 四：Search & Query DSL的生成
ES最核心的功能就是提供了全文检索能力，並还有很强大的分析功能。项目中已经使用ES作为产品主数据，当然还有个MySQL的ID服务器。ES将负责完成所有的产品检索、精确匹配、同义词检索、搜索推荐等功能。ES天生支持那些功能，但是需要数据存储结构的支持，那么数据结构如何定义呢？答案就是index的mapping定义。所以说做好ES的search功能必须要很好的理解你的数据存储结构。

### index流程
Mapping结构规定了字段存储方式，但是ES的存储流程是伴随着文本流处理的，大致有三个阶段：

![tokenizer](./tokenizer.png)

其中每个阶段都有很多选择，都可以通过mapping的setting设置。具体可以参照[官网介绍](https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-analyzers.html)。最终把预处理、分词好的值存储到倒排索引中，供后面的Search使用。理解这个流程对Search的理解也有好处，例如term query的时候为啥会是区分大小写查询的，而match query不区分。

### mapping结构
ES支持多种应用场景，autosuggest，fuzzy Search，同义词等。这些功能的实现都是通过index的mapping结构定义支持的。所以这块需要多看看ES的文档，并结合index的mapping进行一定的验证实验。

下面具体说下项目中需要解决的问题。

搜索推荐的功能理解起来比较简单，例如用户在Search输入框输入的过程中，给一定的feedback，推荐出一个list供用户选择。关于这个功能，ES是可以支持的很好的。例如定义一个字段，type指定为“completion”。

```json
{"properties": {
"name_suggest" : {
  "type" :      "completion",
  "analyzer" :  "suggest_name_synonyms",
  "search_analyzer": "standard"
}}}
```

例如上面properties中定义了一个name_suggest字段，这个字段作为一个“虚拟字段”存在，即不存在doc value中。需要注意的是analyzer，这个对推荐效果有关键的影响作用。例如：

```json
{"analysis": {
  "analyzer": {
    "suggest_name_synonyms": {
      "type":      "custom",
      "tokenizer": "standard",
      "filter":    [ "name_synonyms","lowercase","myNGramFilter" ]
}}}}
```

tokenizer为分词器定义，filter中指定了所用的filter，有同义词的filter，转换大小写，边界词filter。可以参照ES的官方文档进行进一步理解。同义词filter支持定义对应的同义词，但是同义词的更新需要做reindex操作。
有时我们需要做跨字段的推荐或者搜索，可以利用field的copy_to属性：

```json
{"systemField": {
  "properties": {
    "name":{
      "type":"keyword",
      "copy_to": ["name_suggest"]
    }
  }
}}
```

这样类似实现了字段的聚合。将多个字段聚合到一个可以支持complete、同义词搜索的字段了。
至于fuzzy Search可以在获取suggest的时候指定：

```
GET /storessearch/_suggest
{
  "product": {
    "text": "myname",
    "completion": {
      "field": "name_suggest",
      "fuzzy": {
        "fuzziness": 2
      }
    }
  }
}
```

### Query DSL生成
ES的[Java Client](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-compound-queries.html)支持创建Query DSL:

```
QueryBuilder qb = boolQuery()
    .must(termQuery("content", "test1"))    
    .must(termQuery("content", "test4"))    
    .mustNot(termQuery("content", "test2")) 
    .should(termQuery("content", "test3"))  
    .filter(termQuery("content", "test5"));
```

但是这种创建复杂些的DSL就显得力不从心了，比如文档结构复杂点的，有nest path，也有child、parent关系的，如果用Java Client来写代码会非常复杂难懂，也不容易维护。
考虑到DSL也是json结构，可以写些helper class用来创建这种json结构，代码大概类似这种：

```java
public static class SimpleQueryDSLBuilder {
    JsonObject match = new JsonObject();
    JsonObject query = new JsonObject();
    JsonObject matchMeta = new JsonObject();
    JsonArray source = new JsonArray();

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
```

这种生成DSL的方式跟Java Client的方式，大同小异，当查询非常复杂的时候，代码量也就上去了。
由此看来生成通过代码生成DSL的方式不太可行。有什么好的解决办法呢？答案就是使用模板引擎的方式。twig模板的语法非常适合做这件事，[这篇文章](https://amsterdam.luminis.eu/2016/07/25/the-new-elasticsearch-java-rest-client-part-2/)也给出了些例子。

实际使用的是[pebble](http://www.mitchellbosecke.com/pebble/home)模板引擎，语法是受twig启发。这样修改query语句时需要修改模板即可，代码只需要用这些模板生成对应的Query DSL即可。写任复杂的Query DSL也毫无压力。
例如定义一些base query：

```
{% macro terms_query(meta, isStr="false") %}
{"terms":{
    "{{meta.key}}": [
        {% for value in meta.value %}
            {% if meta.valueType == "STRING" or meta.valueType == "DATE" or isStr == "true" %} "{{value}}" {% else %} {{value}} {% endif %} {% if loop.index != loop.length-1 %}, {% endif %}
        {% endfor %}
    ]
}}
{% endmacro %}

{% macro wildcard_query(meta) %}
{"wildcard":{
    "{{meta.key}}": {
      "value": "{{meta.value|first}}"
    }
}}
{% endmacro %}

{% macro prefix_query(meta) %}
{"prefix":{
    "{{meta.key}}": {
      "value": "{{meta.value|first}}"
    }
}}
{% endmacro %}

{% macro channel(channelId) %}
"must": [{"nested": {
    "path": "channels",
    "query": {
      "bool": {
        "must": [
          {
            "match": {
              "channels.id": {{ channelId }}
            }
          }
        ]
      }
    }
  }
}]
{% endmacro %}
```

这些base的query都被定义成了macro，可以被任意组合复用，从而为组成更复杂的query提供了基础，另外也变得非常灵活。只需要Java代码组织好所用的query data，当然这些都是基于业务需要。
    
参考：
1. https://www.oreilly.com/ideas/10-elasticsearch-metrics-to-watch
2. http://dogdogfish.com/guide/building-a-search-engine-for-e-commerce-with-elasticsearch/
3. http://rea.tech/implementing-autosuggest-in-elasticsearch/

