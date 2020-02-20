#!/bin/bash
for i in hadoop102 hadoop103 hadoop104 ; do 
	ssh $i "rm -rf /opt/module/zookeeper-3.4.10/datas/version-2/"
	echo "$i的zookeeper的version目录已经删除！"
	ssh $i "cd /opt/module/kafka/logs; rm -rf *"
	echo "$i的kafka的log目录已经被清空！"
done
