package com.spring.boot.admin.demo.config;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.Charset;
import java.util.List;

@Configuration
public class WebMvcConfigurerAdapter implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //将null字段返回为空字符串
        converters.add(0, fastJsonHttpMessageConverterEx());
    }



    public FastJsonHttpMessageConverterEx fastJsonHttpMessageConverterEx() {
        FastJsonHttpMessageConverterEx fastJsonHttpMessageConverter=new FastJsonHttpMessageConverterEx();
        FastJsonConfig fastJsonConfig =new FastJsonConfig();
        fastJsonConfig.setCharset(Charset.defaultCharset());
        //SerializerFeature.DisableCircularReferenceDetect解决引用没有正常序列化问题，如：$ref": "$.value[3].tagList[0]
        fastJsonConfig.setSerializerFeatures( SerializerFeature.WriteNullStringAsEmpty,SerializerFeature.WriteMapNullValue, SerializerFeature.DisableCircularReferenceDetect);
        fastJsonConfig.setFeatures(Feature.OrderedField);
        SerializeConfig serializeConfig = SerializeConfig.globalInstance;
        serializeConfig.put(Long.class, ToStringSerializer.instance);
        serializeConfig.put(Long.TYPE, ToStringSerializer.instance);
        fastJsonConfig.setSerializeConfig(serializeConfig);

        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        return fastJsonHttpMessageConverter;
    }
}