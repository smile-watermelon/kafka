# 1.4 服务端参数配置

$KAFKA_HOME/config/server.properties

## 1、连接zookeeper 集群的地址及端口号，多个用逗号分割eg:
```
#集群
zookeeper.connect=10.211.55.20:2181,10.211.55.21:2181,10.211.55.22:2181/kafka
# 单节点
zookeeper.connect=10.211.55.20:2181/kafka
```

## 2、listeners
```
advertised.listeners=PLAINTEXT://10.211.55.20:9092
```
表示broker 监听客户端连接的地址列表，配置格式为
protocol1://hostname1:port1,protocol2://hostname2:port2

protocl表示协议，支持的协议有：PLAINTEXT,SSL,SASL_SSL等

该参数默认为null，如果不指定主机名，表示绑定默认网卡

advertised.listeners 作用和listeners类似，默认值为null，主要用于 Iaas(Infrastructure as a Service) 环境，
比如公有云上的服务器配有多个网卡，即包含私网网卡和公网网卡，可以设置 advertised.listeners 绑定公网IP供外部使用，
listeners绑定私网网卡供broker间通信使用


## 3、broker.id
指定kafka集群中broker的唯一标识，默认-1，如果没有设置，默认生成，这个参数还和meta.prperties
文件及服务端参数broker.id.generation.enable和reserved.broker.max.id相关，
参考6.5.1。

## 4、log.dir 和 log.dirs
log.dir 配置单个根目录，log.dirs配置多个根目录（逗号分割），两个都可以配置多个根目录，log.dirs优先级更高。

## 5、message.max.bytes
指定broker 能接受消息的最大值，默认为1000012(B)，约等于976.6KB，producer 发送消息大于该值，
RecordTooLargeException异常，修改该参数还要考虑max.request.size(客户端参数)
max.message.bytes(topic 端参数)。
