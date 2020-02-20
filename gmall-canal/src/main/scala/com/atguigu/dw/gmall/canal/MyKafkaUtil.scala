package com.atguigu.dw.gmall.canal

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

//将数据发送到kafka
object MyKafkaUtil {

  private val props: Properties = new Properties()
  props.put("bootstrap.servers", "hadoop102:9092,hadoop103:9092,hadoop104:9092")
  // key的序列化
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  // value序列化
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  private val producer = new KafkaProducer[String,String](props)

  def sendMessage(topic:String,data:String)={
    producer.send(new ProducerRecord[String,String](topic,data))
    //println("写入了")
  }

}
