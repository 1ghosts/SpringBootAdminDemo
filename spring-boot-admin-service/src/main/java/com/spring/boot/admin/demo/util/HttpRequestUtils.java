package com.spring.boot.admin.demo.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * desc
 *
 * @author xutianzhe
 * @Date 2018/8/15
 **/
@Slf4j
public class HttpRequestUtils {


    /**
     * jsonPost
     *
     * @param url    路径
     * @param params 参数
     * @return
     */
    public static JSONObject jsonPost(String url, Map<String, String> params) {
        Map<String, Object> dataMap = new HashMap<>();
        for (String key : params.keySet()) {
            dataMap.put(key, params.get(key));
        }

        return jsonPost(url, new JSONObject(dataMap), false);
    }

    /**
     * jsonPost
     *
     * @param url    路径
     * @param params 参数
     * @return
     */
    public static JSONObject httpPost(String url, Map<String, String> params) {
        return httpPost(url, params, false);
    }


    /**
     * post请求
     *
     * @param url            url地址
     * @param jsonParam      参数
     * @param noNeedResponse 不需要返回结果
     * @return
     */
    public static JSONObject jsonPost(String url, JSONObject jsonParam, boolean noNeedResponse) {
        //post请求返回结果
        HttpClient httpClient = httpClient();

        HttpPost method = new HttpPost(url);
        try {
            if (null != jsonParam) {
                //解决中文乱码问题
                StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                method.setEntity(entity);
            }
            HttpResponse result = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");
            /**请求发送成功，并得到响应**/
            return buildResult(url, result, noNeedResponse);
        } catch (IOException e) {
            log.error("通信异常:" + url, e);
        }
        return null;
    }


    public static JSONObject jsonPost(String url, String json, boolean noNeedResponse) {
        //post请求返回结果
        HttpClient httpClient = httpClient();

        HttpPost method = new HttpPost(url);
        try {
            if (null != json) {
                //解决中文乱码问题
                StringEntity entity = new StringEntity(json, "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                method.setEntity(entity);
            }
            HttpResponse result = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");
            /**请求发送成功，并得到响应**/
            return buildResult(url, result, noNeedResponse);
        } catch (IOException e) {
            log.error("通信异常:" + url, e);
        }
        return null;
    }


    public static JSONObject jsonPost(String url, String json, Map<String, String> headers, boolean noNeedResponse) {
        //post请求返回结果
        HttpClient httpClient = httpClient();

        HttpPost method = new HttpPost(url);
        try {
            if (headers != null) {
                for (String header : headers.keySet()) {
                    method.addHeader(header, headers.get(header));
                }
            }
            if (null != json) {
                //解决中文乱码问题
                StringEntity entity = new StringEntity(json, "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                method.setEntity(entity);
            }
            HttpResponse result = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");
            /**请求发送成功，并得到响应**/
            return buildResult(url, result, noNeedResponse);
        } catch (IOException e) {
            log.error("通信异常:" + url, e);
        }
        return null;
    }

    public static String jsonPost(String url, String json, Map<String, String> headers) {
        //post请求返回结果
        HttpClient httpClient = httpClient();

        HttpPost method = new HttpPost(url);
        try {
            if (headers != null) {
                for (String header : headers.keySet()) {
                    method.addHeader(header, headers.get(header));
                }
            }
            if (null != json) {
                //解决中文乱码问题
                StringEntity entity = new StringEntity(json, "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                method.setEntity(entity);
            }
            long begin = System.currentTimeMillis();
            HttpResponse result = httpClient.execute(method);
            long end = System.currentTimeMillis();
            long sed = end - begin;
            url = URLDecoder.decode(url, "UTF-8");
            /**请求发送成功，并得到响应**/
            int statusCode = result.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                if (statusCode == 500) {
                    JSONObject jsonObject = JSON.parseObject(EntityUtils.toString(result.getEntity()));
                    if ("com.startdt.startdtboot.exceptionframework.commons.BizException".equals(jsonObject.getString("exception"))) {
                        String[] split = jsonObject.getString("message").split(" ");
                        return "{\"success\":\"false\",\"codeNum\":\"" + split[0] + "\",\"codeDesc\":\"" + split[1] + "\"}";
                    }
                }
                log.error("通信异常:code=" + statusCode + "," + url + "," + result.getStatusLine());
                throw new RuntimeException("通信异常");
            }
            /**读取服务器返回过来的json字符串数据**/
            String str;
            try {
                str = EntityUtils.toString(result.getEntity());
                log.debug("recv:response:{}", str);
                return str;
            } catch (IOException e) {
                log.error("获取返回数据异常", e);
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            log.error("通信异常:" + url, e);
        }
        return null;
    }

    /**
     * post请求
     *
     * @param url            url地址
     * @param postMap        参数
     * @param noNeedResponse 不需要返回结果
     * @return
     */
    public static JSONObject httpPost(String url, Map<String, String> postMap, boolean noNeedResponse) {
        //post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost method = new HttpPost(url);
        try {
            if (null != postMap) {
                //解决中文乱码问题
                List<NameValuePair> params = new ArrayList<>();
                for (String key : postMap.keySet()) {
                    params.add(new BasicNameValuePair(key, postMap.get(key)));
                }
                method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            }
            HttpResponse result = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");

            return buildResult(url, result, noNeedResponse);
        } catch (IOException e) {
            log.error("通信异常:" + url, e);
        }
        return null;
    }


    private static JSONObject buildResult(String url, HttpResponse result, boolean noNeedResponse) {
        int statusCode = result.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            log.error("通信异常:code=" + statusCode + "," + url + "," + result.getStatusLine());
            throw new RuntimeException("通信异常");
        }


        /**读取服务器返回过来的json字符串数据**/
        String str;
        try {
            str = EntityUtils.toString(result.getEntity());
            log.debug("recv:response:{}", str);
        } catch (IOException e) {
            log.error("获取返回数据异常", e);
            throw new RuntimeException(e);
        }

        if (noNeedResponse) {
            return null;
        }

        /**把json字符串转换成json对象**/
        try {
            return JSONObject.parseObject(str);
        } catch (Exception e) {
            log.error("JSON转换异常,无法将字符串转换成JSON对象\n" + str, e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 发送get请求
     *
     * @param url 路径
     * @return
     */
    public static JSONObject requestGet(String url) {
        //get请求返回结果
        JSONObject jsonResult = null;
        try {
            HttpClient httpClient = httpClient();
            //发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity(), "utf-8");
                /**把json字符串转换成json对象**/
                jsonResult = JSONObject.parseObject(strResult);
                url = URLDecoder.decode(url, "UTF-8");
            } else {
                log.error("通信异常:" + url);
            }
        } catch (IOException e) {
            log.error("通信异常:" + url, e);
        }
        return jsonResult;
    }


    public static void requestGet(String url, HttpServletRequest httprequest, HttpServletResponse response) {
        //get请求返回结果
        JSONObject jsonResult = null;
        OutputStream os = null;
        try {
            HttpClient httpClient = httpClient();
            //发送get请求
            HttpGet request = new HttpGet(url);
            if (StringUtils.isNotBlank(httprequest.getHeader("Origin"))) {
                request.setHeader("Origin", httprequest.getHeader("Origin"));
            }
            HttpResponse httpResponse = httpClient.execute(request);
            Header[] headers = httpResponse.getAllHeaders();
            response.reset();
            String ContentType = "";
            for (Header header : headers) {
                if ("Content-Type".equals(header.getName())) {
                    ContentType = header.getValue();
                } else if ("Server".equals(header.getName())) {
                    continue;
                } else {
                    response.setHeader(header.getName(), header.getValue());
                }
            }
            response.setContentType(ContentType);
            os = response.getOutputStream();
            os.write(EntityUtils.toByteArray(httpResponse.getEntity()));
            os.flush();
            os.close();
        } catch (IOException e) {
            log.error("通信异常:" + url, e);
        } finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    log.error("", e);
                }

            }
        }
    }

    public static JSONObject requestGet(String url, Map<String, String> parameters) {
        //get请求返回结果
        JSONObject jsonResult = null;
        try {

            if (parameters != null) {
                StringBuffer param = new StringBuffer();
                int i = 0;
                for (String key : parameters.keySet()) {
                    if (i == 0) {
                        param.append("?");
                    } else {
                        param.append("&");
                    }
                    param.append(key).append("=").append(parameters.get(key));
                    i++;
                }
                url += param;
            }

            CloseableHttpClient httpClient = HttpClients.createDefault();
            //发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity(), "utf-8");
                /**把json字符串转换成json对象**/
                jsonResult = JSONObject.parseObject(strResult);
                url = URLDecoder.decode(url, "UTF-8");
            } else {
                log.error("通信异常:" + url);
            }
        } catch (IOException e) {
            log.error("通信异常:" + url, e);
        }
        return jsonResult;
    }

    public static <T> T requestGetT(String url, Map<String, String> parameters, TypeReference<T> type) {
        //get请求返回结果
        try {
            T t = null;
            if (parameters != null) {
                StringBuffer param = new StringBuffer();
                int i = 0;
                for (String key : parameters.keySet()) {
                    if (i == 0) {
                        param.append("?");
                    } else {
                        param.append("&");
                    }
                    param.append(key).append("=").append(parameters.get(key));
                    i++;
                }
                url += param;
            }
            CloseableHttpClient httpClient = HttpClients.createDefault();
            //发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);
            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity(), "utf-8");
                System.out.println(strResult);
                /**把json字符串转换成json对象**/
                t = JSONObject.parseObject(strResult, type);
            } else {
                throw new Exception("Http返回非200" + url);
            }
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 发送get请求
     *
     * @param url 路径
     * @return
     */
    public static String getJSon(String url) {
        //get请求返回结果
        String jsonResult = null;
        try {
            HttpClient httpClient = httpClient();
            //发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity(), "utf-8");
                /**把json字符串转换成json对象**/
                return strResult;
            } else {
                log.error("通信异常:" + url);
            }
        } catch (IOException e) {
            log.error("通信异常:" + url, e);
        }
        return jsonResult;
    }

    /**
     * 发送get请求
     *
     * @param url 路径
     * @return
     */
    public static InputStream getInputStream(String url) {
        try {
            HttpClient httpClient = httpClient();
            //发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的数据流**/
                return response.getEntity().getContent();
            } else {
                log.error("通信异常:" + url);
            }
        } catch (IOException e) {
            log.error("通信异常:" + url, e);
        }
        return null;
    }


    /**
     * 发送get请求
     *
     * @param url 路径
     * @return
     */
    public static JSONArray requestGetArray(String url) {
        //get请求返回结果
        JSONArray jsonArray = null;
        try {
            HttpClient httpClient = httpClient();
            //发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity(), "utf-8");
                /**把json字符串转换成json对象**/
                jsonArray = JSONObject.parseArray(strResult);
                url = URLDecoder.decode(url, "UTF-8");
            } else {
                log.error("通信异常:" + url);
            }
        } catch (IOException e) {
            log.error("通信异常:" + url, e);
        }
        return jsonArray;
    }

    public static String postMap(String url, Map<String, String> map) {
        return postMap(url, map, null);
    }

    /**
     * 发送get请求
     *
     * @param url 路径
     * @return
     */
    public static String postMap(String url, Map<String, String> map, Map<String, String> headers) {
        //get请求返回结果
        try {
            HttpClient httpClient = httpClient();
            //发送get请求
            HttpPost request = new HttpPost(url);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).setConnectionRequestTimeout(3000).build();
            request.setConfig(requestConfig);
            List<BasicNameValuePair> urlParameters = new ArrayList<>();

            if (map != null) {
                for (String key : map.keySet()) {
                    urlParameters.add(new BasicNameValuePair(key, map.get(key)));
                }
            }
            HttpEntity postParams = new UrlEncodedFormEntity(urlParameters, "UTF-8");
            request.setEntity(postParams);
            if (headers != null) {
                for (String header : headers.keySet()) {
                    request.addHeader(header, headers.get(header));
                }
            }
            HttpResponse response = httpClient.execute(request);
            if (log.isDebugEnabled()) {
                log.debug("recv responsecode :{}", response.getStatusLine());
            }


            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (log.isDebugEnabled()) {
                    log.debug("recv response :{}", strResult);
                }
                return strResult;
            } else {
                String strResult = EntityUtils.toString(response.getEntity(), "UTF-8");
                log.error("返回结果:" + strResult);
                log.error("通信异常:" + url);
            }
        } catch (ConnectTimeoutException e) {

        } catch (IOException e) {
            log.error("通信异常:" + url, e);
        }
        return null;
    }


    /**
     * 发送delete请求
     *
     * @param url
     * @param parameters
     * @return
     */
    public static String requestDelete(String url, Map<String, String> parameters) {
        //get请求返回结果
        JSONObject jsonResult = null;
        try {

            if (parameters != null) {
                StringBuffer param = new StringBuffer();
                int i = 0;
                for (String key : parameters.keySet()) {
                    if (i == 0) {
                        param.append("?");
                    } else {
                        param.append("&");
                    }
                    param.append(key).append("=").append(parameters.get(key));
                    i++;
                }
                url += param;
            }

            HttpClient httpClient = httpClient();
            //发送get请求
            HttpDelete request = new HttpDelete(url);
            HttpResponse response = httpClient.execute(request);

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity(), "utf-8");

                return strResult;
            } else {
                log.error("通信异常:" + url);
            }
        } catch (IOException e) {
            log.error("通信异常:" + url, e);
        }
        return null;
    }

    private static HttpClient httpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(120000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnPerRoute(1000)
                .setMaxConnTotal(2000)
                .build();
    }
}
