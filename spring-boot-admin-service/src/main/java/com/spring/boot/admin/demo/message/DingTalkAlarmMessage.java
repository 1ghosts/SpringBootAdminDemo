package com.spring.boot.admin.demo.message;

import com.alibaba.fastjson.JSON;
import com.spring.boot.admin.demo.util.HttpRequestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author chensihong
 * @version 1.0
 * @date 2023/4/7 5:16 PM
 */
@Component
public class DingTalkAlarmMessage implements AlarmMessage{

    @Value("${dingtalk.webhookUrl}")
    private String dingTalkUrl;

    @Override
    public void sendData(String content) {
        simpleNotify(content);
    }

    public void simpleNotify(String content){
        HashMap<String, String> params = new HashMap<>();
        params.put("msgtype", "text");
        params.put("text", String.format("{\"content\":\"[机器指标告警] %s\"}", content));
        params.put("at", "{\"isAtAll\": true}");
        HttpRequestUtils.jsonPost(dingTalkUrl, JSON.toJSONString(params), true);
    }
}
