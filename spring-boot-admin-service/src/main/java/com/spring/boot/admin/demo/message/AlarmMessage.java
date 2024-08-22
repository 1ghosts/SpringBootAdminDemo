package com.spring.boot.admin.demo.message;

public interface AlarmMessage {

    /**
     * 发送文本告警
     * @param content
     */
    void sendData(String content);
}
