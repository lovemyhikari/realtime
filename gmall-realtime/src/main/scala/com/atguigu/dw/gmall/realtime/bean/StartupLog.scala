package com.atguigu.dw.gmall.realtime.bean

import java.text.SimpleDateFormat
import java.util.Date

case class StartupLog(mid: String,
                      uid: String,
                      appId: String,
                      area: String,
                      os: String,
                      channel: String,
                      logType: String,
                      version: String,
                      ts: Long,
                      var logDate: String = null,
                      var logHour: String = null){
  //对时间进行赋值
  private val format1 = new SimpleDateFormat("yyyy-MM-dd")
  private val format2 = new SimpleDateFormat("HH")
  private val date = new Date(ts)
  logDate = format1.format(date)
  logHour = format2.format(date)
}

