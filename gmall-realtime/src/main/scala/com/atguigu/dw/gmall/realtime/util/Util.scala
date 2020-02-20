package com.atguigu.dw.gmall.realtime.util

import java.io.InputStream
import java.util.Properties

//读取配置文件中的数据
object Util {

  private val is: InputStream = Util.getClass.getClassLoader.getResourceAsStream("config.properties")
  private val prop = new Properties()
  prop.load(is)
  def getProperty(propName:String):String ={
    prop.getProperty(propName)
  }

  //测试
//  def main(args: Array[String]): Unit = {
//    println(getProperty("kafka.broker.list"))
//  }

}
