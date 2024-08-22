# 说明
# [spring-boot-admin-service](spring-boot-admin-service) 
    服务端
    有个坑必须加spring-boot-starter-parent依赖作为parent，否则SpringBootAdmin有些配置类无法正常加载，具体还确定必须加什么依赖只能这样
    <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>2.7.9</version>
            <relativePath/>
    </parent>

# [spring-boot-admin-client](spring-boot-admin-client)
    客户端
