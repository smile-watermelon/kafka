## 4.1.1 创建主题

- auto.create.topic.enable 是否自动创建主题，默认true
- default.replication.factor 默认创建分区的副本数，默认为1
- num.partitions 创建的分区数，默认为1

### 创建主题
```shell
./bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic topic-create --partitions 4 --replication-factor 2
```

### 查看主题
```shell
./bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic topic-create
```

### 手动指定分配方案
```shell
./bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic topic-create-same --replica-assignment 2:0,0:1,1:2,2:1
```

### 设置配置参数
```shell
./bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic topic-config --replication-factor 1 --partitions 1 --config cleanup.policy=compact --config max.message.bytes=10000
./bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic topic-config
```

### 查看所有可用主题
```shell
./bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### 查看多个主题
```shell
./bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic topic1,topic2
```

- --topics-with-overrides 查看覆盖了默认配置的主题
- --under-replicated-partitions 找包含失效副本的主题
- --unavailable-partitions 查看没有leader副本的分区，分区不可用

### 修改分区数
```shell
./bin/kafka-topics.sh --bootstrap-server localhost:9092 --alter --topic topic-config --partitions 3
```

### 修改配置
```shell
./bin/kafka-configs.sh --bootstrap-server localhost:9092 --alter --topic topic-config --config max.message.bytes=20000
```

## 4.1.6 主题端参数
- cleanup.policy  对应broker端参数log.cleanup.policy  日志压缩策略，默认delete，还有 compact，也可以两者结合,格式为：delete,compact
- compression.type  对应broker端参数compression.type 压缩类型，默认为producer，还有uncompressed，snappy,lz4,gzip
- delete.retention.ms  对应broker端参数 log.cleaner.retention.ms 被标识删除的数据保留多长时间，默认1天
- file.delete.delay.ms 对应broker端参数 log.segment.delete.delay.ms 清理文件之前等待多长时间，默认1分钟
- flush.messages 对应broker端参数 log.flush.interval.messages 收集多少消息才会强制刷新到磁盘，默认值为 Long.MAX_VALUE，由系统决定
- flush.ms 对应broker端参数 log.flush.interval.ms 等待多久将消息强制刷新到磁盘，由系统决定，默认值Long.MAX_VALUE
- follower.replication.throttled 对应broker端参数 follower.replication.throttled.replicas 配置被限制速率的主题对应的follower 副本列表
- index.interval.ms 对应broker端参数 log.index.interval.ms 控制添加索引的频率，超过该参数可以添加一个新的索引，默认为 4096
- leader.replication.throttled.replicas 对应broKer段参数 leader.replication.throttled.replicas 限制leader副本速率
- max.message.bytes 对应broker端参数 max.message.bytes 消息最大字节数，默认值 1000012
- message.format.version 对应broker端参数 log.message.format.version 消息格式的版本，默认2.0IV1


## 4.3.1、优先副本的选举
- 优先副本是AR集合中的第一个副本，也就是leader副本
- 分区自动平衡，broker端参数 auto.leader.rebalance.enable 默认为true，会开启一个定时任务，定时任务会轮询每个节点的不平衡率，计算公式 非优先leader副本总数 / 分区总数 
是否超过了leader.imbalance.per.broker.percentage 参数值，默认为10%，超过该值会自动执行优先副本的选举来达到分区平衡。
- leader.imbalance.check.interval.seconds 执行分区自平衡周期，默认5分钟

note：生产环境不建议开启 自平衡功能

### leader副本重新平衡脚本
```shell
# 这个脚本在3.x 版本被弃用，替代脚本为 kafka-reassign-partitions.sh
./bin/kafka-preferred-replica-election.sh 
```
### 1、生产优先副本选举的json 文件
```shell
./bin/kafka-reassign-partitions.sh --bootstrap-server localhost:9092 --generate --topics-to-move-json-file topic-to-move.json --broker-list "0,1,2" reassignment.json 
```
- topic-to-move-json-file 参数指明想要进行优先副本选举的主题eg：
```json
{
  "topics": [
    {
      "topic": "topic-create"
    }
  ],
  "version": 1
}
```
### 生产的json 数据
```json
{
  "version":1,
  "partitions":[
    {
      "topic":"topic-create",
      "partition":0,
      "replicas":[0,1],
      "log_dirs":["any","any"]},
    {
      "topic":"topic-create",
      "partition":1,
      "replicas":[1,2],
      "log_dirs":["any","any"]
    },
    {
      "topic":"topic-create",
      "partition":2,
      "replicas":[2,0],
      "log_dirs":["any","any"]
    },
    {
      "topic":"topic-create",
      "partition":3,
      "replicas":[0,2],
      "log_dirs":["any","any"]
    }
  ]
}
```

### 执行上面生产的json 文件
```shell
./bin/kafka-reassign-partitions.sh --bootstrap-server localhost:9092 --reassignment-json-file reassignment.json --execute
```

### 分区重新分配
上面的是针对主题级别的，kafka-reassign-partitions.sh 脚本还支持分区级别的再均衡
```json
{
    "version": 1,
    "partitions": [
        {
            "topic": "topic-create",
            "partition": 3,
            "replicas": [2, 1]
        }
    ]
}
```
```shell
./bin/kafka-reassign-partitions.sh --bootstrap-server localhost:9092 --reassignment-json-file reassignment-partition.json --execute
```

```
1、消费者再均衡，有新的消费者上线，把原来的分区分配给新的消费者
2、分区重分配指的是，有节点下线，leader副本经过选举后落在了其他节点上，造成该节点的负载不平衡
当该节点上线后，将主题或分区重新手动或自动的重新分配到新的节点，让leader副本落在新的节点上，来达到leader副本的负载均衡。

分区重分配是通过控制器为每个分区添加新副本（增加副本因子），新副本从leader同步完数据后，将原来的旧副本删除，同时修改原来的副本元数据。
```

### 修改副本因子，举例
- 对一个分区
```json
{
  "topic": "topic-factor",
  "partition": 1,
  "replicas": [
    2,1,0
  ],
  "log_dirs": [
    "any",
    "any",
    "any"
  ]
}
```
- 对主题
```json
{
  "version": 1,
  "partitions": [
    {
      "topic": "topic-factor",
      "partition": 0,
      "replicas": [
        0,1
      ],
      "log_dirs": [
        "any",
        "any"
      ]
    }
  ]
  
}
```
- 执行上面的json 文件
```shell
./bin/kafka-reassign-partitions.sh --bootstrap-server localhost:9092 --reassignment-json-file reassignment-partition.json --execute
```

### 复制限流
有两种放式：
- kafka-config.sh
- kafka-reassign-partitions.sh 

kafka-config.sh 通过设置 分区级别的限流参数：follower.replication.throttled.rate 和 leader.replication.throttled.rate 
前者用于控制follower的复制速度，后者用于控制leader的传输速度
主题级别的限流参数：leader.replication.throttled.replicas,follower.replication.throttled.rate

进行手动副本复制时，kafka-reassign-partitions.sh 脚本可以指定 --throttle 参数 单位为B

leader 副本的选举由控制器完成

