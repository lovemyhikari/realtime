package com.atguigu.dw.gmallpublisher.service;

import java.util.Map;

public interface PublisherService {
    Long getDau(String date);
    Map<String,Long> getHourDau(String date);
    // 获取销售额的总和
    Double getTotalAmount(String date);
    // 获取每小时的销售额明细
    Map<String, Double> getHourAmount(String date);
}
