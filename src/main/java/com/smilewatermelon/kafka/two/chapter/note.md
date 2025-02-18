# 2.1 
```java
public class ProducerRecord<K, V> {
    
    private final String topic;
    private final Integer partition;
    // 设置和应用相关的信息
    private final Headers headers;
    // 同一个key 会被划分到同一个分区，有key还可以支持日志压缩
    private final K key;
    private final V value;
    // 消息时间戳，分两种
    // 1、createTime 消息创建的时间
    // 2、LogAppendTime 消息追加到日志文件中的时间
    private final Long timestamp;
}
```
##  2.1.1、必要参数

- bootstrap.servers，连接kafka的地址，建议配置多个，配置一个也行，客户端会从broker查找
其他broker的信息，配置多个避免broker挂掉时，客户端连接失败。
- key.serializer 和 value.serializer ，broker端接收消息是以字节数组为单位。

## 生产端发生消息的三种模式
- 发后即忘：fire-and-forget，只管发，不保证是不是到达，性能搞，可靠性差
- 同步：sync，具体看 Test的代码
- 异步：async，具体看 Test的代码

## 2.1.4 分区器

- send() 方法发送数据到broKer，会经过拦截器，序列化器，分区器
- 存在key使用murmur2对key进行hash，key 等于null则使用轮询的方式往分区写入数据

## 2.2.1 整体架构
- 拦截器 -》序列化器-》分区器-》消息累加器。
- 消息累加器 RecordAccumulator：主要作用缓存生产者发送的消息，Sender 线程从累加器中拿数据发送给broker
  RecordAccumulator 缓存大小，由生产者的 buffer.memory 设置，默认值为32MB，如果生产者的发送的速度超过
Sender 线程发往broker的速度，会导致生产者阻塞或抛出异常，抛出异常取决于，max.block.ms 参数，
默认时间为60秒。
- 消息在网络中是以字节传递的，在传递之前先要在内存创建一片区域，这片区域用来保存消息数据，
为了不频繁的创建和释放内存，使用 BufferPool 创建一个可复用的内存空间。缓冲池参数为 batch.size，默认大小为16kb

- 消息在 RecordAccumulator 会封装为 ProducerBatch 对象，表示要发送的一批数据，
Sender 发送数据时会进一步分装为 <Node，List<ProducerBatch>> ，再进一步封装为Map<NodeId, Deque<Request>> 对象

Sender 线程发往broker 的数据会保存到 InflightRequests 中 默认参数是：max.in.flight.requests.per.connection=5,
意思是客户端和broker 的每个连接最多缓存5个未响应的请求。在需要保证顺序的场景中，例如读binlog日志，建议将该参数设置为1

- Producer 元数据的更新，通过Sender 线程发送，构造 MetadataRequest 对象，也会被加到InflightRequests中，
元数据的更新时间参数 metadata.max.age.ms=300000，5分钟。

## 2.3、重要的生产者参数
- acks，表示分区中有多少个副本收到这条消息，有3中模式：
  acks=1，默认值，leader副本成功写入就返回成功响应。
  acks=0，不需要等待服务端响应
  acks=-1 或 acks=all，所有副本都成功写入才返回响应，如果 ISR的副本等于1，acks就会退化为 1，此时宕机可能造成数据丢失。
- max.request.size，限制生产者客户端能发送消息的最大值，默认为1M
- retry.backoff.ms，消息发送，重试间隔时间

- compress.type，默认为none，格式有gzip，snappy，lz4
- connections.max.idle.ms，多久之后关闭空闲的连接，默认9分钟
- linger.ms，发送ProducerBatch之前，等待消息加入ProducerBatch 的时间，默认值为0，与TCP协议中的Nagle算法有异曲同工之妙。
- receive.buffer.bytes Socket 连接接收缓冲区的大小，默认32KB。如果Producer 和Fkafka 处在不同的机房可以调大这个值。-1 表示使用操作系统默认值
- send.buffer.bytes  Socket发送消息缓冲区的大小，默认128KB，-1 表示使用操作系统默认值
- request.timeout.ms Producer 等待请求响应的最长时间 默认 30秒，注意这个参数要比broKer端参数
replica.lag.time.max.ms 的值要大，这样可以减少因客户端重试而引起的消息重复的概率。