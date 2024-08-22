package com.spring.boot.admin.demo.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class MyFilterConfig {


    @Bean
    public Filter filter1() {
        return new MyFilter();
    }

    @Bean
    public FilterRegistrationBean setFilter1() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(filter1());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(-10);   //order的数值越小，在所有的filter中优先级越高
        return filterRegistrationBean;
    }

}
