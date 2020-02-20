package com.atguigu.dw.gmallpublisher.service;

import com.atguigu.dw.gmallpublisher.mapper.DauMapper;
import com.atguigu.dw.gmallpublisher.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublisherServiceImp implements PublisherService {
    @Autowired
    DauMapper dauMapper;
    @Override
    public Long getDau(String date) {
        return dauMapper.getDau(date);
    }

    @Override
    public Map<String, Long> getHourDau(String date) {
        //获取返回的数据
        List<Map> hourDau = dauMapper.getHourDau(date);
        //将数据转换成Map<String,Long>
        Map<String,Long> result = new HashMap<>();
        for(Map map : hourDau){
            String key = (String)map.get("LOGHOUR");
            Long value = (Long)map.get("COUNT");
            //放入到map中
            result.put(key,value);
        }
        return result;
    }

    @Autowired
    OrderMapper orderMapper;

    @Override
    public Double getTotalAmount(String date) {
        Double total = orderMapper.getTotalAmount(date);
        return total == null ? 0 : total;
    }

    @Override
    public Map<String, Double> getHourAmount(String date) {
        HashMap<String, Double> result = new HashMap<>();
        List<Map> mapList = orderMapper.getHourAmount(date);
        for (Map map : mapList) {
            String key = (String)map.get("CREATE_HOUR");
            Double value = ((BigDecimal) map.get("SUM")).doubleValue();
            result.put(key,value);
        }

        return result;
    }
}
