package com.spring.boot.admin.demo.config;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Base64Utils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class MyFilter implements Filter {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private String springBootAdminRequest = "/clientPoint/**";

    private String authorization;


    @Value("${spring.boot.admin.client.instance.metadata.user-name:}")
    private String springBootAdminClientUserName;
    @Value("${spring.boot.admin.client.instance.metadata.user-password:}")
    private String springBootAdminClientPassword;
    @Value("${server.servlet.context-path:}")
    private String contextPath;



    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.springBootAdminRequest = StringUtils.isBlank(contextPath) ? springBootAdminRequest : contextPath + springBootAdminRequest;
        this.authorization = this.encode(springBootAdminClientUserName, springBootAdminClientPassword);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String requestURI = request.getRequestURI();
        boolean isSpringBootAdminRequest = antPathMatcher.match(springBootAdminRequest, requestURI);
        boolean skipLog = !isSpringBootAdminRequest;
        if (this.authorization != null && isSpringBootAdminRequest) {
            if (!this.authorization.equals(request.getHeader(HttpHeaders.AUTHORIZATION))) {
                log.warn("非法请求，host:{}, uri:{}", servletRequest.getRemoteAddr(), requestURI);
                HttpServletResponse resp = (HttpServletResponse) servletResponse;
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        if (skipLog) {
            log.info("First过滤器:firstServlet 执行uri:{}之前", requestURI);
        }
        ModifyHttpServletRequestWrapper mParametersWrapper = new ModifyHttpServletRequestWrapper(request);
        filterChain.doFilter(mParametersWrapper, servletResponse);
        if (skipLog) {
            log.info("First过滤器:firstServlet 执行uri:{} 之后", requestURI);
        }
    }

    /**
     * 修改request的值
     */
    private class ModifyHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private Map<String, String> mapCookies;

        ModifyHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
            this.mapCookies = new HashMap<>();
        }

        void putCookie(String name, String value) {
            this.mapCookies.put(name, value);
        }

        @Override
        public Cookie[] getCookies() {
            HttpServletRequest request = (HttpServletRequest) getRequest();
            Cookie[] cookies = request.getCookies();
            if (mapCookies == null || mapCookies.isEmpty()) {
                return cookies;
            }
            if (cookies == null || cookies.length == 0) {
                List<Cookie> cookieList = new LinkedList<>();
                for (Map.Entry<String, String> entry : mapCookies.entrySet()) {
                    String key = entry.getKey();
                    if (key != null && !"".equals(key)) {
                        cookieList.add(new Cookie(key, entry.getValue()));
                    }
                }
                if (cookieList.isEmpty()) {
                    return cookies;
                }
                return cookieList.toArray(new Cookie[cookieList.size()]);
            } else {
                List<Cookie> cookieList = new ArrayList<>(Arrays.asList(cookies));
                for (Map.Entry<String, String> entry : mapCookies.entrySet()) {
                    String key = entry.getKey();
                    if (key != null && !"".equals(key)) {
                        for (int i = 0; i < cookieList.size(); i++) {
                            if (cookieList.get(i).getName().equals(key)) {
                                cookieList.remove(i);
                            }
                        }

                        cookieList.add(new Cookie(key, entry.getValue()));

                    }
                }
                return cookieList.toArray(new Cookie[cookieList.size()]);
            }
        }
    }


    @Override
    public void destroy() {

    }

    private String encode(String username, String password) {
        String token = Base64Utils.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    public static void main(String[] args) {
        MyFilter myFilter = new MyFilter();
        System.out.println(myFilter.encode("springBootAdminService", "lp@2024!"));
    }
}
