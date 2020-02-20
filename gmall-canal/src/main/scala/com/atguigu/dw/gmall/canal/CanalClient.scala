package com.atguigu.dw.gmall.canal

import java.net.{InetSocketAddress, SocketAddress}
import java.util

import com.alibaba.otter.canal.client.{CanalConnector, CanalConnectors}
import com.alibaba.otter.canal.protocol.CanalEntry.{EntryType, RowChange}
import com.alibaba.otter.canal.protocol.{CanalEntry, Message}
import com.google.protobuf.ByteString


//1.从canal服务器读取数据
object CanalClient {

  def main(args: Array[String]): Unit = {

    //1.创建一个canal客户端连接器
    val address: SocketAddress = new InetSocketAddress("hadoop102",11111)
    val connector: CanalConnector = CanalConnectors.newSingleConnector(address,"example","","")
    //2.连接到canal实例
    connector.connect()
    //3.订阅数据
    connector.subscribe("gmall0830.*")
    //4.获取数据,客户端主动向canal服务器拉取，canal才会向mysql数据库同步
    while(true){
      //尝试拿batchsize条记录，不会阻塞等待
      val message: Message = connector.get(100)
      val entries: util.List[CanalEntry.Entry] = if (message != null) message.getEntries else null
      //需要将java中的集合转换成scala中的集合
      import scala.collection.JavaConversions._
      if (entries != null && !entries.isEmpty) {
        for(entry <- entries){
          //Entry的类型必须是RowData 不能是事务的开始, 结束等其他类型
          if (entry != null && entry.getEntryType == EntryType.ROWDATA){
            //每个entry封装一个StoreValue
            val storeValue: ByteString = entry.getStoreValue
            //每个StoreValue对应一个RowChange
            val change: RowChange = RowChange.parseFrom(storeValue)
            //每个RowChange中包含多个RowData
            val rowDatasList: util.List[CanalEntry.RowData] = change.getRowDatasList
            //封装一个方法对数据进行解析
            CanalHandler.handle(entry.getHeader.getTableName,rowDatasList,change.getEventType)
          }
        }
      }else{
        println("没有拉取到数据，2s钟之后继续")
        Thread.sleep(2000)
      }

    }




  }

}
