## ElasticSearch 深入理解 四：Search & Query DSL的生成
ES最核心的功能就是提供了全文检索能力，並还有很强大的分析功能。项目中已经使用ES作为产品主数据，当然还有个MySQL的ID服务器。ES将负责完成所有的产品检索、精确匹配、同义词检索、搜索推荐等功能。ES天生支持那些功能，但是需要数据存储结构的支持，那么数据结构如何定义呢？答案就是index的mapping定义。所以说做好ES的search功能必须要很好的理解你的数据存储结构。

### index流程
Mapping结构规定了字段存储方式，但是ES的存储流程是伴随着文本流处理的，大致有三个阶段：

![tokenizer](./tokenizer.png)

其中每个阶段都有很多选择，都可以通过mapping的setting设置。具体可以参照[官网介绍](https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-analyzers.html)。最终把预处理、分词好的值存储到倒排索引中，供后面的Search使用。

### mapping结构
ES支持多种应用场景，autosuggest，fuzzy Search，同义词等。这些功能的实现都是通过index的mapping结构定义支持的。所以这块需要多看看ES的文档，并结合index的mapping进行一定的验证实验。

下面具体说下项目中需要解决的问题。

#### 搜索推荐
搜索推荐的功能简单易懂


    
参考：
1. https://www.oreilly.com/ideas/10-elasticsearch-metrics-to-watch
2. http://dogdogfish.com/guide/building-a-search-engine-for-e-commerce-with-elasticsearch/
3. http://rea.tech/implementing-autosuggest-in-elasticsearch/

