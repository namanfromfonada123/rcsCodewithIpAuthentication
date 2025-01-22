package com.messaging.rcs.consumer;

import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Component;

@Component
public class QueueName {
    public final String buildFor(String name) {
        return  "telco."+name+".queue";
    }
    public String buildForExchange(String name) {
        return "telco."+name+".exchange";
    }
    public String buildForRouting(String name) {
        return "telco."+name+".routing";
    }

    public Queue queue(String name) {
        return new Queue("telco."+name+".queue", false);
    }
}
