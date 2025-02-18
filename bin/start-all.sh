#!/bin/bash

startCmd="/opt/kafka_2.13-3.2.1/bin/kafka-server-start.sh /opt/kafka_2.13-3.2.1/config/server.properties"
stopCmd="/opt/kafka_2.13-3.2.1/bin/kafka-server-stop.sh"

case $1 in
start)
    for host in kafka-master kafka-node1 kafka-node2; do
      echo "-------start ${host}--------"
      ssh $host "${startCmd} &"
    done
  ;;
stop)
 for host in kafka-master kafka-node1 kafka-node2; do
      echo "-------stop ${host}--------"
      ssh $host "${stopCmd} &"
    done
  ;;
esac
