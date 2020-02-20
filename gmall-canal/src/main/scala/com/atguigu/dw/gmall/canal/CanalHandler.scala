package com.atguigu.dw.gmall.canal

import java.util

import com.alibaba.fastjson.JSONObject
import com.alibaba.otter.canal.protocol.CanalEntry
import com.alibaba.otter.canal.protocol.CanalEntry.EventType
import com.atguigu.dw.gmall.common.Constant

object CanalHandler {
    import scala.collection.JavaConversions._
    //对从canal获取的数据进行解析
    def handle(tableName:String,rowDatas: util.List[CanalEntry.RowData],eventType: EventType) ={
      //判断是否是order_info中的表
      if("order_info" == tableName && rowDatas != null && !rowDatas.isEmpty && eventType == EventType.INSERT ){
        sendRowDataToKafka(rowDatas,Constant.ORDER_TOPIC)
      }else if ("order_detail" == tableName && rowDatas != null && !rowDatas.isEmpty && eventType == EventType.INSERT){
        sendRowDataToKafka(rowDatas,Constant.DETAIL_TOPIC)
      }

    }

    private def sendRowDataToKafka(rowDatas: util.List[CanalEntry.RowData],topic:String)={
      for(rowData <- rowDatas){
        val jsonObj = new JSONObject()
        val columnsList: util.List[CanalEntry.Column] = rowData.getAfterColumnsList
        for(column <- columnsList){
          //将数据进行封转
          val key: String = column.getName
          val value: String = column.getValue
          jsonObj.put(key,value)
        }
        //将数据发送到kafka
        //println(jsonObj.toJSONString)
        MyKafkaUtil.sendMessage(topic,jsonObj.toJSONString)
      }
    }

}
