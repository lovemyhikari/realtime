package com.atguigu.dw.gmallpublisher.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.dw.gmallpublisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PublisherController {

    @Autowired
    public PublisherService service;

    @GetMapping("/realtime-total")
    public String getTotal(@RequestParam("date") String date){

        List<Map<String, String>> result = new ArrayList<>();

        HashMap<String, String> map1 = new HashMap<>();
        map1.put("id", "dau");
        map1.put("name", "新增日活");
        map1.put("value", service.getDau(date) + "");
        result.add(map1);

        HashMap<String, String> map2 = new HashMap<>();
        map2.put("id", "new_mid");
        map2.put("name", "新增设备");
        map2.put("value", "233");
        result.add(map2);

        HashMap<String, String> map3 = new HashMap<>();
        map3.put("id", "order_amount");
        map3.put("name", "新增交易额");
        map3.put("value", service.getTotalAmount(date) + "");
        result.add(map3);

        return JSON.toJSONString(result);
    }

    /*
    http://localhost:8070/realtime-hour?id=dau&date=2019-09-20
     */
    @GetMapping("/realtime-hour")
    public String getHourTotal(@RequestParam("id") String id , @RequestParam("date") String date){
        //返回的结果类型是Map<String,Map<String,Long>>
        /*
        {"yesterday":{"11":383,"12":123,"17":88,"19":200 },
        "today":{"12":38,"13":1233,"17":123,"19":688 }}
         */
        if("dau".equals(id)){
            Map<String,Map<String,Long>> result = new HashMap<>();
            Map<String, Long> today = service.getHourDau(date);
            Map<String, Long> yesterday = service.getHourDau(getYesterday(date));
            result.put("today",today);
            result.put("yesterday",yesterday);
            return JSON.toJSONString(result);
        }else if ("order_amount".equals(id)) {
            Map<String, Double> today = service.getHourAmount(date);
            Map<String, Double> yesterday = service.getHourAmount(getYesterday(date));

            HashMap<String, Map<String, Double>> result = new HashMap<>();
            result.put("today", today);
            result.put("yesterday", yesterday);

            return JSON.toJSONString(result);
        }
        return null;
    }

    private String getYesterday(String date) {

        return LocalDate.parse(date).minusDays(1).toString();
    }

}
