package com.spring.boot.admin.demo.alarm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spring.boot.admin.demo.message.AlarmMessage;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JvmAlarm {

    private final RestTemplate restTemplate = new RestTemplate();

    private Scheduler scheduler;

    private Disposable subscription;

    private InstanceRepository repository;


    private AlarmMessage alarmMessage;

    /**
     * jvm 阈值
     */
    private double threshold = 0.95;

    /**
     * 累计告警次数
     */
    private int alarmCountThreshold = 3;

    /**
     * 检测频率,秒
     */
    private long interval = 10;

    /**
     * 格式化模版
     */
    private final DecimalFormat df = new DecimalFormat("0.00M");

    /**
     * 排除实例
     */
    private String excludeInstances = "";

    /**
     * 开关
     */
    private boolean enabled = true;

    /**
     * 提醒模版
     */
    private final String ALARM_TPL = "测试请忽略，服务实例【%s】,内存使用超阈值【%s】,累计【%s】次,当前最大内存【%s】,已使用【%s】,当前线程数【%s】";

    /**
     * 超过阈值次数
     */
    private final Map<String, Integer> instanceCount = new HashMap<>();

    BasicAuthHttpHeaderProvider basicAuthHttpHeaderProvider;

    public JvmAlarm(InstanceRepository repository, AlarmMessage alarmMessage, BasicAuthHttpHeaderProvider basicAuthHttpHeaderProvider) {
        this.repository = repository;
        this.alarmMessage = alarmMessage;
        this.basicAuthHttpHeaderProvider = basicAuthHttpHeaderProvider;
    }

    private void checkFn(Long aLong) {
        if (!enabled) {
            return;
        }
        log.debug("check jvm for all instances");
        repository.findAll().filter(instance -> !excludeInstances.contains(instance.getRegistration().getName())).map(instance -> {
            String instanceName = instance.getRegistration().getName();
            //最大堆空间
            double jvmMax = getJvmValue(instance, "jvm.memory.max?tag=area:heap") / (1024*1024d);
            //已使用堆空间
            double jvmUsed = getJvmValue(instance, "jvm.memory.used?tag=area:heap") /  (1024*1024d);
            if (jvmMax != 0 && jvmUsed / jvmMax > threshold && instanceCount.computeIfAbsent(instanceName, key -> 0) > alarmCountThreshold) {
                //当前活跃线程数
                int threads = (int) getJvmValue(instance, "jvm.threads.live");
                String content = String.format(ALARM_TPL, instanceName, (threshold * 100) + "%", alarmCountThreshold, df.format(jvmMax), df.format(jvmUsed), threads);
                alarmMessage.sendData(content);
                //重新计算
                instanceCount.remove(instanceName);
            }
            //更新累计超过阈值次数
            if (jvmMax != 0 && jvmUsed / jvmMax > threshold) {
                instanceCount.computeIfPresent(instanceName, (key, value) -> value + 1);
            }
            return Mono.just(0d);
        }).subscribe();
    }

    private long getJvmValue(Instance instance, String tags) {
        try {
            String reqUrl = instance.getRegistration().getManagementUrl() + "/metrics/" + tags;
            log.debug("check jvm {},uri {}", instance.getRegistration().getName(), reqUrl);
            HttpHeaders headers = basicAuthHttpHeaderProvider.getHeaders(instance);
            HttpEntity httpEntity = new HttpEntity(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(reqUrl, HttpMethod.GET, httpEntity,  String.class);
            String body = responseEntity.getBody();
            JSONObject bodyObject = JSON.parseObject(body);
            JSONArray measurementsArray = bodyObject.getJSONArray("measurements");
            if (measurementsArray != null && !measurementsArray.isEmpty()) {
                return measurementsArray.getJSONObject(0).getLongValue("value");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return 0L;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public String getExcludeInstances() {
        return excludeInstances;
    }

    public void setExcludeInstances(String excludeInstances) {
        this.excludeInstances = excludeInstances;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getAlarmCountThreshold() {
        return alarmCountThreshold;
    }

    public void setAlarmCountThreshold(int alarmCountThreshold) {
        this.alarmCountThreshold = alarmCountThreshold;
    }

    private void start() {
        this.scheduler = Schedulers.newSingle("jvm-check");
        this.subscription = Flux.interval(Duration.ofSeconds(this.interval)).subscribeOn(this.scheduler).subscribe(this::checkFn);
    }

    private void stop() {
        if (this.subscription != null) {
            this.subscription.dispose();
            this.subscription = null;
        }
        if (this.scheduler != null) {
            this.scheduler.dispose();
            this.scheduler = null;
        }
    }

    protected String encode(String username, String password) {
        String token = Base64Utils.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }
}
