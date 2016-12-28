package sync.listener;


import elasticsearch.constant.ESConstants;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import sun.misc.MessageUtils;
import sync.common.ESHandleMessage;
import sync.common.MQMessageConst;
import sync.handler.ESHandler;
import sync.handler.ESHandlerManager;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Created by I311352 on 10/17/2016.
 */
@Component
public class ESMessageListener implements MessageListener {
    private static final Logger lightLogger = Logger.getLogger(ESMessageListener.class);

    private ESHandlerManager handlerManager = null;
    private ConfigurableApplicationContext context;

    private final CounterService counterService;

    private final GaugeService gaugeService;

    public ESMessageListener(ConfigurableApplicationContext context) {
        this.counterService = context.getBean("counterService", CounterService.class);
        this.gaugeService = context.getBean("gaugeService", GaugeService.class);
        this.context = context;
        //handlerManager.initHandler(context);
    }

    @Override
    public void onMessage(Message message) {
        if (handlerManager == null) {
            handlerManager = new ESHandlerManager();
            handlerManager.initHandler(context);
        }

        doWork(message);
    }

    protected void postHandle(Message message) {
        String messageId = String.valueOf(message.getMessageProperties()
                .getHeaders().get(MQMessageConst.H_MESSAGEID));
        String jobName = String.valueOf(message.getMessageProperties()
                .getHeaders().get(MQMessageConst.H_JOBNAME));
    }

    public void doWork(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        Matcher m = ESConstants.ROUTINGKEY_PATTERN.matcher(routingKey);
        try {
            if (m.find() && m.groupCount() == 3) {
                ESHandleMessage esHandleMessage = new ESHandleMessage(Long.parseLong(m.group(3)),
                        m.group(1), m.group(2), message.getBody());
                esHandleMessage.setEventGenerated(isGeneratedMessage(message));
                handleMsg(esHandleMessage);
            } else {
                lightLogger.warn("Message is invalid: " + routingKey);
            }
        } catch (Exception e) {
            lightLogger.error("Exception " + e);
        } finally {
            lightLogger.info("Done");
        }

    }

    private boolean isGeneratedMessage(Message message) {
        return ObjectUtils.equals("EventGenerator", message.getMessageProperties().getHeaders().get("eventSource"));
    }

    private void handleMsg(ESHandleMessage message) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        boolean successful = false;
        lightLogger.info("ES sync start on lightMessage[" + message + "]");
        ESHandler handler = null;
        try {
            handler = handlerManager.getHandler(message);
            handler.onMessage(message);

            successful = true;
        } catch (Throwable t) {
            successful = false;
            throw t;
        } finally {
            stopWatch.stop();
            String metricName = message.getClass().getSimpleName() + "." + handler.getClass().getSimpleName();
            //counterService.increment(metricName + "." + (successful ? ".success" : "failed"));
            //gaugeService.submit(metricName, stopWatch.getTime());
            lightLogger.info("ES sync end on lightMessage[" + message + "], execTime[" + stopWatch.getTime() + "]");
        }


    }

//    @Override
    public String[] getRoutingKeys() {
        List<String> routingList = handlerManager.getRoutingKeyList();
        return routingList.toArray(new String[routingList.size()]);
    }

    public void setHandlerManager(ESHandlerManager handlerManager) {
        this.handlerManager = handlerManager;
    }
}
