package com.atguigu.dw.gmall.realtime.app

import java.util

import com.alibaba.fastjson.JSON
import com.atguigu.dw.gmall.common.Constant
import com.atguigu.dw.gmall.realtime.bean.{AlertInfo, EventLog}
import com.atguigu.dw.gmall.realtime.util.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}

object AlertApp {

  def main(args: Array[String]): Unit = {
    
    val conf: SparkConf = new SparkConf().setAppName("alert").setMaster("local[4]")
    val ssc: StreamingContext = new StreamingContext(conf,Seconds(5))
    //1.从kafka中读取数据
    val rawStream: DStream[(String, String)] = MyKafkaUtil.getKafkaStream(ssc,Constant.EVENT_TOPIC).window(Minutes(5))
    val eventStream: DStream[(String, EventLog)] = rawStream.map {
      case (_, json) =>
        val log: EventLog = JSON.parseObject(json, classOf[EventLog])
        (log.mid,log)
    }

    //2.对数据进行分析处理
    /*
    需求：同一设备，5分钟内  三次及以上  用不同账号登录  领取优惠劵  没有浏览商品。
     */
    //将行为日志信息按照mid进行分组，得到的是（mid，五分钟内所有的event信息集合）
    val midLogGrouped: DStream[(String, Iterable[EventLog])] = eventStream.groupByKey()
    val alertInfoStream = midLogGrouped.map {
      case (mid, logIt) =>
        // logIt 在这最近的5分钟内, 在mid这个设备上的所有事件日志
        // 记录: 1. 5分钟内当前设备有几个用户登录   2. 有没有点击(浏览商品)
        // 返回: 预警信息
        // 1. 记录领取过优惠券的用户  (java的set集合, scala的集合到es中看不到数据)
        val uidSet = new util.HashSet[String]()
        // 2. 记录5分钟内所有的事件
        val eventList: util.List[String] = new util.ArrayList[String]()
        // 3. 记录领取的优惠券对应的商品的id
        val itemSet = new util.HashSet[String]()

        var isClickItem = false // 表示5分钟内有没有点击商品
        import scala.util.control.Breaks._
        breakable {
          logIt.foreach(log => {
            eventList.add(log.eventId) // 把事件保存下来
            // 如果事件类型是优惠券, 表示有一个用户领取了优惠券, 把这个用户保存下来
            log.eventId match {
              case "coupon" =>
                uidSet.add(log.uid) // 把领取优惠券的用户存储
                itemSet.add(log.itemId) // 把优惠券对应的商品id存储

              case "clickItem" =>
                isClickItem = true // 表示该用户点击了商品
                break
              case _ =>
            }
          })
        }

        (uidSet.size() >= 3 && !isClickItem, AlertInfo(mid, uidSet, itemSet, eventList, System.currentTimeMillis()))
    }

    //写入到ES中
    import com.atguigu.dw.gmall.realtime.util.EsUti._
    alertInfoStream
      .filter(_._1)
      .map(_._2)
      .foreachRDD(_.saveToES("gmall_coupon_alert"))

    alertInfoStream.print(1000)
    
    ssc.start()
    ssc.awaitTermination()
    
  }

}
