# 3.1

- partition.assignment.strategy 消费者分区分配策略
- subscribe 订阅方式有3种：1、AUTO_TOPICS，AUTO_PATTERN，USER_ASSIGNED
- assign 订阅指定的分区，没有分区再均衡的功能
- 消费位移，保存在_consumer_offsets 主题中
- enable.auto.commit 自动提交位移，默认为true
- auto.commit.interval.ms 定期提交，默认为5s，自动提交位移不是消费完立马就提交位移，而是定期提交。

避免重复消费的方法：
1、关闭自动提交，进行手动提交
    消费完所有数据后，可以按照当前拉取的批次提交位移。
    也可以进行单个数据消费完后，进行单条数据的唯一提交。实际生产环境很少使用这种
2、进行幂等性操作，确保即使发生重复消费，依然保证数据的正确性
3、设置一个递增的序号每次，初始值为当前拉取批次的的位移值最后一个数据的唯一值，在位移提交后，
当前值+1
4、进行兜底的操作，如下
```java
try {
    while(true) {
        
        consumer.commitAsync();
    }            
} catch (Exception e) {
    e.printStackTrace();
} finally {
    consumer.commitSync();    
}
```
## 3.2.7 指定消费位移

- auto.offset.reset 指定从什么位置拉取数据，默认是 latest，earliest 是从队列头开始消费，也就是offset=0
还有一个值是none，表示几部从最新的位置消费也不从头消费，会抛出NotOffsetPartitionException异常

## 3.2.8 再均衡
- 再均衡是指分区从一个消费者转移到另一个消费者的行为，再均衡期间组内的消费者无法消费数据，
再均衡可能会发重复消费的问题。

## 3.2.11 重要的消费者参数

- fetch.min.bytes 调用poll方法从kafka中拉取的最小数据量，默认为1B。
- fetch.max.bytes 一次拉取的最大数据量，默认50M。
- max.message.bytes （服务端 主题topic）所能接收的最大消息的大小
- fetch.max.wait.ms 指定拉取的等待时间，默认500ms，和 fetch.min.bytes 配合使用
- max.partition.fetch.bytes 配置每个分区返回给consumer的最大数据量，默认为1MB
- max.poll.records 一次拉取的消息数量的最大数，默认500条。
- connections.max.idle.ms 多久之后关闭空闲的连接，默认9分钟
- exclude.internal.topics kafka kafka中内部主题，__consumer_offsets，__transcation_state。
该参数表名，内部主题是否向消费者公开。默认为true
- receive.buffer.bytes 设置Socket接收缓冲区的大小，默认值为64KB。如果consumer和kafka处于不同的机房可以调大点
- send.buffer.bytes Socket发送缓冲区的大小。
- request.timeout.ms 消费者等待请求响应的最大时间，默认30s
- metadata.max.age.ms 配置元数据的过期时间，默认5分钟，超过该值进行强制更新
- reconnect.backoff.ms 重新连接服务之前等待的时间，默认50ms
- retry.backoff.ms 重新发送失败的请求到主题分区之前的等待，默认100Ms
- isolate.level 设置消费者事务的隔离级别，有效值有：read_uncommitted，
read_committed ，表示消费者所消费到的位置，如果设置为read_committed 会忽略事务未提交的数据，即只能消费到
LSO（lastStableOffset）默认为 read_uncommitted 可以消费到HW 位置。
- heartbeat.interval.ms 分组管理时，心跳到消费者组协调器的时间
- session.timeout.ms 默认值 1000 ，检测消费者是都失效的超时时间
- max.poll.interval.ms 表示超过这个时间的消费者没有拉取数据，认为消费者离开了消费者组，会进程分区在均衡，默认5分钟，
- auto.offset.reset 参考3.2.7
- enable.auto.commit 是否开启自动提交，默认为true
- auto.commit.interval 自动提交位移的间隔时间 默认5秒
- partition.assignment.strategy 消费者分区的分配策略，参考7.1 节