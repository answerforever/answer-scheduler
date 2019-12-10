package com.answer.scheduler;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.answer.scheduler.utils.RedisUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class SchedulerApplicationTests {

    @Value("classpath:json/scheduler.json")
    private Resource schedulerRes;

    @Test
    void contextLoads() {
    }

    @Test
    public void jsonTest() {
        try {
            String jsonData = IOUtils.toString(schedulerRes.getInputStream(), Charset.forName("UTF-8"));
//                List<String> districtNames = JsonPath.read(jsonData, "$.districts[?(@.id == " + i + ")].name");
//                String district = districtNames.get(0);


            JSONObject jsonObject = JSONUtil.parseObj(jsonData);

            System.out.println("json串：" + jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void redisTest() {
        Map<String, Integer> MAP_OFFSET_FIELD = new HashMap<>();
        MAP_OFFSET_FIELD.put("Li", 0);
        MAP_OFFSET_FIELD.put("Liang", 1);
        MAP_OFFSET_FIELD.put("Zhao", 2);
        String schedulerOffsetKey = "scheduler:offset";
        RedisUtils.hsset(schedulerOffsetKey, MAP_OFFSET_FIELD);

        Map<String,String> mapData = RedisUtils.hgetall(schedulerOffsetKey);
    }

}
