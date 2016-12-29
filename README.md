# elastic-rabbitmq
load data into elasticsearch from consume rabbitmq, use version control make data consistent.

# data consume flow
![flow](./flow.png)

从RabbitMQ中消费message，并persist到ES中。一个明显的问题就是ES没有
事务一致性的保证，通过乐观锁控制每个Document的版本号。在并发写入的情况下这个是一大挑战。
