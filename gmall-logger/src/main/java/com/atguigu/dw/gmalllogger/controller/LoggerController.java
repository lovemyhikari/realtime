package com.atguigu.dw.gmalllogger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dw.gmall.common.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoggerController {

    @PostMapping("/log")
    public String doLog(@RequestParam("log") String log){
        //1.给日志添加时间戳
        log = addTs(log);
        //System.out.println(log);
        //2.给日志落盘
        save2File(log);
        //3.将日志写入到kafka
        send2Kafka(log);
        return "ok";
    }

    //3.将日志信息写入到kafka
    @Autowired
    KafkaTemplate<String,String> kafka;
    private void send2Kafka(String log) {
        String topic = Constant.STARTUP_TOPIC;
        if(log.contains("event")){
            topic = Constant.EVENT_TOPIC;
        }
        kafka.send(topic,log);
    }

    //2.将日志落盘
    Logger logger = LoggerFactory.getLogger(LoggerController.class);
    private void save2File(String log) {
        logger.info(log);
    }

    //1.给日志添加时间戳
    private String addTs(String log) {
        JSONObject json = JSON.parseObject(log);
        json.put("ts",System.currentTimeMillis());
        return json.toJSONString();
    }

}
