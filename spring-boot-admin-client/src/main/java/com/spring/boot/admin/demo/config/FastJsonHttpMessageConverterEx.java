package com.spring.boot.admin.demo.config;


import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;

/**
 * @author xingtian on 2018/12/28.
 * @version 1.0
 */
@Slf4j
public class FastJsonHttpMessageConverterEx extends FastJsonHttpMessageConverter {

    private final ResourceRegionHttpMessageConverter resourceRegionHttpMessageConverter = new ResourceRegionHttpMessageConverter();

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        // SpringBootAdmin读取日志文件时按段读取文件流，但因为配置了fastjson会被被fastjson序列化为json，所以需要跳过避免被fastjson处理
        // 交给实际处理类org.springframework.http.converter.ResourceRegionHttpMessageConverter
        //如果是想全部按ResourceRegionHttpMessageConverter这个走就将它的方法逻辑copy过来取反即可
        //        return !resourceRegionHttpMessageConverter.canWrite(type, clazz, mediaType);
        if(type instanceof ParameterizedTypeImpl){
            Type[] actualTypeArguments = ((ParameterizedTypeImpl) type).getActualTypeArguments();
            if(actualTypeArguments!=null){
                return !actualTypeArguments[0].equals(ResourceRegion.class);
            }
        }
        return super.canWrite(type, clazz, mediaType);
    }
}
