package com.sensorweb.eurekaserver02.config;

import com.sensorweb.eurekaserver02.util.SendMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * eureka状态改变监听器
 *
 * @Author zyt
 * @Date 2018/10/29 10:15
 */
@Component
public class EurekaStateChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(EurekaStateChangeListener.class);

    /**
     * 区分生产环境
     */
    /*@Value("${spring.profiles.active}")
    String active;*/

    /**
     * 服务下线事件
     *
     * @param event
     */
    @EventListener
    public void listenDown(EurekaInstanceCanceledEvent event) throws Exception {
        //if (active.equals("production")) {

            String mes = "服务ID：" + event.getServerId() + "\t" +
                    "服务实例：" + event.getAppName() + "\t服务下线";
            // 发送邮件
            logger.info(MarkerFactory.getMarker("DOWN"), "服务ID：" + event.getServerId() + "\t" +
                    "服务实例：" + event.getAppName() + "\t服务下线");
            SendMail.sendemail(mes);
        //}
        logger.info(event.getServerId() + "\t" + event.getAppName() + "服务下线");
    }
}
