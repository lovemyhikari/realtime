package com.atguigu.dw.gmall.realtime.app

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.alibaba.fastjson.JSON
import com.atguigu.dw.gmall.common.Constant
import com.atguigu.dw.gmall.realtime.bean.StartupLog
import com.atguigu.dw.gmall.realtime.util.{MyKafkaUtil, RedisUtil}
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis
import org.apache.phoenix.spark._

object DauApp {

  def main(args: Array[String]): Unit = {
    
    //1.从kafka消费数据
    val conf: SparkConf = new SparkConf().setAppName("DauApp").setMaster("local[2]")
    val ssc = new StreamingContext(conf,Seconds(3))
    val source: InputDStream[(String, String)] = MyKafkaUtil.getKafkaStream(ssc,Constant.STARTUP_TOPIC)

    //2.将读取到的JSON数据封装到样例类中
    val startupLogStream: DStream[StartupLog] = source.map {
      case (_, json) => JSON.parseObject(json, classOf[StartupLog])
    }

    //3.去重
    val filteredStream: DStream[StartupLog] = startupLogStream.transform(rdd => {
      //3.1 连接Redis客户端
      val client: Jedis = RedisUtil.getJedisClient
      //println("client connect ok")
      //3.2 读取Set中已经启动的mid
      val midSet: util.Set[String] = client.smembers(Constant.STARTUP_TOPIC + ":" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
      client.close()
      //3.3 由于rdd此时是在Driver端，所以需要广播到所有的Executor上
      val bd: Broadcast[util.Set[String]] = ssc.sparkContext.broadcast(midSet)
      //3.4 进行过滤:除了解决不同设备的去重之外，海奥解决同一个设备在某个时间段内多次启动的问题
      rdd
        .filter(log => {
        !bd.value.contains(log.mid)
      })
        .map(log => (log.mid,log))
        .groupByKey
        .map{
          case (_,logIt) => logIt.toList.minBy(_.ts)
        }
    })

    //3.5 将第一次启动的设备写入到redis
    filteredStream.foreachRDD(rdd => {
      rdd.foreachPartition(logIt =>{
        //每个分区创建一个连接
        val client: Jedis = RedisUtil.getJedisClient
        //把每个数据写入
        logIt.foreach(log => {
          client.sadd(Constant.STARTUP_TOPIC+":"+log.logDate,log.mid)
        })
        //关闭连接
        client.close()
      })
    })
    filteredStream.print

    //4.将数据写入到phoenix（HBASe）
    filteredStream.foreachRDD(rdd => {
      rdd.saveToPhoenix(
        "GMALL_DAU",
        Seq("MID", "UID", "APPID", "AREA", "OS", "CHANNEL", "LOGTYPE", "VERSION", "TS", "LOGDATE", "LOGHOUR"),
        zkUrl = Some("hadoop102,hadoop103,hadoop104:2181")
      )
    })

    ssc.start()
    ssc.awaitTermination()
    
  }

}
