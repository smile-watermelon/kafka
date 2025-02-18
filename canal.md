# cannal 安装

修改MySQL配置
```shell
[mysqld]
log-bin=mysql-bin # 开启 binlog
binlog-format=ROW # 选择 ROW 模式
server_id=1 # 配置 MySQL replaction 需要定义，不要和 canal 的 slaveId 重复
```

- 创建MySQL canal 用户
```shell
CREATE USER canal IDENTIFIED BY 'canal';  
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%';
-- GRANT ALL PRIVILEGES ON *.* TO 'canal'@'%' ;
FLUSH PRIVILEGES;
```

docker run -d --restart always --name mysql5.7.42-master \
-p 3306:3306 \
-v /opt/docker/mysql/conf/my.cnf:/opt/bitnami/mysql/conf/my.cnf \
-v /opt/docker/mysql/data:/bitnami/mysql/data \
-v /opt/docker/mysql/logs:/opt/bitnami/mysql/logs \
-e MYSQL_ROOT_PASSWORD=16351018 \
bitnami/mysql:5.7.42 

docker run -d --restart always --name mysql5.7.42-master \ 
-p 3306:3306 \
-v /opt/docker/mysql/conf/my.cnf:/opt/bitnami/mysql/conf/my.cnf \ 
-e MYSQL_ROOT_PASSWORD=16351018 \
-e ALLOW_EMPTY_PASSWORD=yes
bitnami/mysql:5.7.42

docker run -d --rm --name mysql5.7.42-master -p 3306:3306 -v /opt/docker/mysql/conf/my.cnf:/opt/bitnami/mysql/conf/my.cnf -e MYSQL_ROOT_PASSWORD=16351018 -e ALLOW_EMPTY_PASSWORD=yes bitnami/mysql:5.7.42