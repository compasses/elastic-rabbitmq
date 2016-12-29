## ElasticSearch 深入理解 一：基础概念&源码启动

Elasticsearch 是一个开源的全文检索、分析引擎，具有高伸缩性。使用广泛：Wikipedia、Stack Overflow、GitHub 等都基于 Elasticsearch 来构建他们的搜索引擎。
项目组用了将近两个月的时间将其做上线，包括大半个月的原型验证，中间经历了不少的挑战，这里打算一并总结归档下。开篇就从基础的几个概念开始，并准备好环境，
看看如何源码启动，从Idea IDE里面直接启动ES，方便调试分析。没有特别说明ElasticSearch的版本都是基于5.0.0。

### 几个基础概念

#### Inverted index
如下图所示
![invertindex](./invertindex.PNG)

三个文档经过一系列的处理生成的倒排索引。
Inverted index 就是建立单词到文档的映射关系，通过单词检索能很快的找到对应的文档，类似于词典的功能。
单词即terms是检索的基本单位，可以任意的组装，进行文档的全文检索或者精确匹配，ElasticSearch有对应的配置支持。
例如需要全文检索的字段可以将type定义为text，精确匹配的字段type定义为keyword即可。

#### Index
Index 在ElasticSearch里面既是动词也是名词。指具有相关性Document的集合；ES将数据存储于一个或多个索引中，
类比传统的关系型数据库领域来说，索引相当于SQL中的一个数据库，或者一个schema。
索引由其名称(必须为全小写字符)进行标识，并通过引用此名称完成文档的创建、搜索、更新及删除操作。

#### Type
类型是索引内部的逻辑分区(category/partition)，然而其意义完全取决于用户需求。因此，一个索引内部可定义一个或多个类型(type)。一般来说，类型就是为那些拥有相同的域的文档做的预定义。
类比传统的关系型数据库领域来说，类型相当于“表”。

#### Document
Document是索引和检索的基本单位，JSON 格式存储。Document存储时必须赋予一个特定的type。
也就是API路径是index/type/document

#### Mapping
规定了字段类型，需要在创建index 的时候指定。Document在索引时，首先会进行字段分析，不同的字段对后续的Search&Filter 影响较大；
比如是否参与全文检索，或者只是精确匹配，对应分别是text和keyword类型。

### 集群相关

#### Cluster
集群里的每个服务器则被称为一个节点（node）。可以通过索引分片将海量数据进行分割并分布到不同节点。
通过副本可以实现更强的可用性和更高的性能。这些服务器被统称为一个集群（cluster）。

#### Node
运行了单个实例的ES主机称为节点，它是集群的一个成员，可以存储数据、参与集群索引及搜索操作。节点通过为其配置的ES集群名称确定其所要加入的集群。
Node本身也有自己的名字和类型，默认集群中Node之间是对等的。

### Shard & Replica
当需要存储大规模文档时，由于RAM空间、硬盘容量等的限制，仅使用一个节点是不够的。另一个问题是一个节点的计算能力达不到所期望的复杂功能的要求。
在这些情况下，可以将数据切分，每部分是一个单独的Apache Lucene索引，称为分片（shard）。每个分片可以存储在集群的不同节点上。 
当需要查询一个由多个分片构成的索引时，ElasticSearch将该查询发送到每个相关的分片，并将结果合并。这些过程对具体应用而言是透明的，无须知道分片的存在。

index 是一个逻辑命名空间，shard 是具体的物理概念，建索引、查询等都是具体的shard在工作。shard是ElasticSearch基本的
调度单位，shard包括primary shard和replica shard，写数据时，先写到primary shard，然后，
同步到replica shard，查询时，primary和replica充当相同的作用。
replica shard可以有多份，也可以没有，replica shard的存在有两个作用，一是容灾，如果primary shard挂了，
数据也不会丢失，集群仍然能正常工作，ElasticSearch会把存活的replica shard提升为primary shard；
二是提高性能，因为replica和 primary shard都能处理查询，**需要注意的是index创建在多个节点之间是传播的**。
shard数只能在建立index时设置，后期不能更改，但是，replica数可以随时通过ElasticSearch提供的API更改。

### 源码启动
这里使用intellij idea IDE，ElasticSearch源码使用[gradle](https://gradle.org/)构建，所以需要本地开发环境安装gradle。
1. checkout ElasticSearch的源码：https://github.com/elastic/elasticsearch
2. 切换到5.0.0版本：切换到源码目录里面执行： ``` git checkout v5.0.0 ```
3. 执行gradle任务导入idea IDE：``` gradle idea ```
4. 打开idea新建project from exist source，导入即可。然后通过命令行进行构建也行idea里面进行也可以：``` gradle build ```
5. main函数入口在Elasticsearch.java里面；idea运行前需要设置个JVM参数，例如 ``` -Des.path.home=C:\elasticsearch-5.0.0\elasticsearch-5.0.0 ```。
这个目录下需要有ElasticSearch运行的一些配置文件：包括elasticsearch.yml，jvm.options，log4j2.properties。初始没有可以从安装包里面取一份；
也可以从build结果里面取一份。

接下来直接启动运行即可。需要注意的可能会报权限问题导致启动失败，索性直接在java.policy文件中添加一行即可。
``` 
grant {
        permission java.security.AllPermission "", "";
        ...
```

