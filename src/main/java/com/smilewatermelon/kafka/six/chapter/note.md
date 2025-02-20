### 

kafka 中运用了大量的延时操作，基于性能考虑，kafka并没有使用jdk的timer 或delayQueue来实现
而是自己造了一个延时器SystemTimer，SyatemTimer 定层是由时间轮 TimingWheel实现的，具体是一个双向的环形链表
，时间轮分为多层，类似钟表的秒针，分针，时针一样。每层分20格，每格的时间间隔一样，第一次间隔1毫秒，
所以第一层可以处理的延时时间是20ms，第二层每格的时间间隔是20ms，整个时间轮是400ms，后面层数延时时间 = 20 * 前一层的时间间隔

每个延时任务，都由延时管理器管理（DelayedOperationPurgatory)，Purgatory （炼狱），延时管理器会配备一个 SystemTimer，
时间轮负责任务的插入和删除操作，ExpiredOperationReaper 收割机线程负责处理时间轮的轮转，还配备一个监听池监听每个分区的外部事件。

LSO（lastStableOffset) LSO = HW - 未提交事务的offset

## 6.4 控制器
一个集群当中某个broKer会被选举为控制器
控制器的作用：
- 负责leader的选举
- 检测到ISR集合发生变化时，通知所有broKer更新他们的元数据
- kafka-topics.sh 脚本创建分区时也是由控制器负责分配

kafka 控制器依赖于zk, 控制器选举成功后会在zk 的 controller节点写入节点id，如下：
```json
{"version":1,"brokerid":2,"timestamp":"1739785620396"}
```

- controller-epoch 表示控制器发生变更的次数，初始值为1，每个和控制器交互的请求都会携带 controller-epoch ，如何携带的epoch 小于该值，
认为是向已经过期的控制器发出的请求，该请求会被认做无效请求。如果大于说明有新的控制器上任了。

控制器的职责：
- 监听分区变化。为zk中的/admin/reassign_partitions 节点注册 partitionReassignmentHandler ,用来处理分区重分配动作。为zk 中的/isr_change_notification
节点注册 IsrChangeNotificationHandler，用来处理ISR 集合变更动作。为zk中的/admin/preferred-replica-election 节点
添加 PreferredReplicaElectionHandler 用来处理优先副本选举动作。
- 监听主题相关的变化。为zk中的/admin/delete_topics 节点添加 TopicDeletionHandler ，用来处理删除主题的动作
- 