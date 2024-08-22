package com.spring.boot.admin.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chensihong
 * @version 1.0
 * @date 2024/8/14 下午5:31
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/print")
    public void print(){
        log.warn("print warn");
        log.info("print info");
        log.error("print error");
    }
}
