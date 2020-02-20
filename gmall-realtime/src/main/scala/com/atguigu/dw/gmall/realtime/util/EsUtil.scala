package com.atguigu.dw.gmall.realtime.util

import com.atguigu.dw.gmall.realtime.bean.AlertInfo
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.core.{Bulk, Index}
import org.apache.spark.rdd.RDD

object EsUti {

  val clientFactory = new JestClientFactory()
  val serverUri = "http://hadoop103:9200"
  var conf: HttpClientConfig = new HttpClientConfig.Builder(serverUri)
    .maxTotalConnection(100)
    .connTimeout(1000 * 100)
    .readTimeout(1000 * 100)
    .multiThreaded(true)
    .build()
  clientFactory.setHttpClientConfig(conf)
  def getClient: JestClient = clientFactory.getObject

  def main(args: Array[String]): Unit = {

    insertBulk("user",List((User("tom","21"),"aa"),(User("cary","30"),"bb")))

  }

  //批量插入
  def insertBulk(index: String, sources: TraversableOnce[Any]) {
    val client: JestClient = getClient
    val bulkBuilder: Bulk.Builder = new Bulk.Builder()
      .defaultIndex(index)
      .defaultType("_doc")
    sources.foreach {
      case (s, id: String) =>
        val action: Index = new Index.Builder(s).id(id).build()
        bulkBuilder.addAction(action)
      case s =>
        val action: Index = new Index.Builder(s).build()
        bulkBuilder.addAction(action)
    }
    client.execute(bulkBuilder.build())
    client.shutdownClient()
  }

  //插入单条数据
  def insertSingle(index:String,source:Any,id:String = null)={
    val client: JestClient = getClient
    val action: Index = new Index.Builder(source)
      .index(index)
      .`type`("_doc")
      .id(id)  //null的话id会随机生成
      .build()
    client.execute(action)
    client.shutdownClient()
  }

  // RDD转换成 ESFunction
  implicit class ESFunction(rdd: RDD[AlertInfo]) {
    def saveToES(index: String): Unit = {
      rdd.foreachPartition((alertInfoIt: Iterator[AlertInfo]) => {
        val result: Iterator[(AlertInfo, String)] = alertInfoIt.map(info => {
          // 如果不拼接mid, 会导致不同设备的预警信息互相覆盖 实现每分钟产生一个document_id
          (info, info.mid + "_" + info.ts / 1000 / 60) //
        })
        EsUti.insertBulk("gmall_coupon_alert", result)
      })
    }
  }

}

case class User(name:String,age:String)
