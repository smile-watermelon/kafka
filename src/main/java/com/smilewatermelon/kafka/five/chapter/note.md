# 5.1、文件目录布局
每个分区副本在磁盘上是由日志文件(.log)准确的说是日志段LogSegment，索引文件(.index)，时间索引文件构成(.timeindex)
分为日志段是为避免一个日志文件过大，进行切分。只有最后一个日志段可以被写数据
每个LogSegment 都有一个基准偏移量baseOffset 表示当前日志段的第一条消息的 offset
日志文件的命名都是基于baseOffset 命名的。

- 00000000000000000224.log 224表示当前日志段总共有224条数据，偏移量从0-223

- __consumer_offset 该主题是消费者第一次消费数据时创建
- 消息集是kafka生产发送，fetch拉取，及压缩的基本单元

日志切割满足下面条件之一时就会触发
- log.segment.bytes broker端参数，设置日志分段文件的大小默认1G，会进行日志滚动/切割
- log.roll.ms，log.roll.hours 当前系统时间大于这两个配置的其中之一时，进行切割
log.roll.ms 优先级大于log.roll.hours，默认值配置了log.roll.hours 默认为7天
- log.index.size.max.bytes broker端参数 索引文件的大小达到这个值，默认为10MB
- 追加消息的偏移量大于Integer.MAX_VALUE的值
- 索引文件采用的是稀疏索引，没有存储所有的消息的offset，通过二分查找的方式进行先定位<= offset 的索引行，然后根据 position（当前索引在日志分段中的位置）
再从该位置遍历找到对应的数据。
- 数据文件先被写到快照文件，达到触发的机制例如服务下线，会被写入log文件。
- log.retention.check.interval.ms 删除日志周期 默认5分钟
- log.retention.bytes 是所有日志的总大小，而不是日志分段的大小
- log.cleaner.min.compaction.lag.ms 消息在被清理前最小保留的时间 默认为0
- log.cleaner.thread 日志清理线程数量，默认为1
- log.cleaner.min.cleanable.ratio 清理操作的最小污浊率，默认为50%
- 
日志分段的保留策略：1、基于时间，2、基于日志大小，基于起始偏移量。

1. 基于时间：
2. 基于日志：log.retention.bytes 默认为-1，
3. 基于起始偏移量

__consumer_offset 的清理策略是：compact

## kafka 的速度为什么很快
1、顺序写
2、页缓存
3、零拷贝
