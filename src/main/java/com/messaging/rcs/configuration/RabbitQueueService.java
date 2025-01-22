package com.messaging.rcs.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
@Component
@Service

public class RabbitQueueService {
    @Autowired
    private RabbitAdmin rabbitAdmin;
    @Autowired
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;


    public void addNewQueue(String queueName, String exchangeName, String routingKey) {
        Queue queue = new Queue(queueName, true, false, false);
        Binding binding = new Binding(
                queueName,
                Binding.DestinationType.QUEUE,
                exchangeName,
                routingKey,
                null
        );
        rabbitAdmin.declareQueue(queue);
        DirectExchange exchange=new DirectExchange(exchangeName);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareBinding(binding);
        this.addQueueToListener("xiaomiapp",queueName);
    }


    public void addQueueToListener(String listenerId, String queueName) {
        System.out.println("adding queue : " + queueName + " to listener with id : " + listenerId);
        if (!checkQueueExistOnListener(listenerId,queueName)) {
            this.getMessageListenerContainerById(listenerId).addQueueNames(queueName);
            System.out.println("queue ");
        } else {
        	System.out.println("given queue name : " + queueName + " not exist on given listener id : " + listenerId);
        }
    }


    public void removeQueueFromListener(String listenerId, String queueName) {
    	System.out.println("removing queue : " + queueName + " from listener : " + listenerId);
        if (checkQueueExistOnListener(listenerId,queueName)) {
            this.getMessageListenerContainerById(listenerId).removeQueueNames(queueName);
            System.out.println("deleting queue from rabbit management");
           // this.rabbitAdmin.deleteQueue(queueName);
        } else {
        	System.out.println("given queue name : " + queueName + " not exist on given listener id : " + listenerId);
        }
    }


    public Boolean checkQueueExistOnListener(String listenerId, String queueName) {
        try {
        	System.out.println("checking queueName : " + queueName + " exist on listener id : " + listenerId);
        	System.out.println("getting queueNames");
            String[] queueNames = this.getMessageListenerContainerById(listenerId).getQueueNames();
            System.out.println("queueNames : " + new Gson().toJson(queueNames));
            if (queueNames != null) {
            	System.out.println("checking " + queueName + " exist on active queues");
                for (String name : queueNames) {
                	System.out.println("name : " + name + " with checking name : " + queueName);
                    if (name.equals(queueName)) {
                    	System.out.println("queue name exist on listener, returning true");
                        return Boolean.TRUE;
                    }
                }
                return Boolean.FALSE;
            } else {
            	System.out.println("there is no queue exist on listener");
                return Boolean.FALSE;
            }
        } catch (Exception e) {
        	System.out.println("Error on checking queue exist on listener");
        	System.out.println("error message : " + e.getMessage());
        	System.out.println("trace : " + e.getStackTrace());
            return Boolean.FALSE;
        }
    }

    private AbstractMessageListenerContainer getMessageListenerContainerById(String listenerId) {
    	System.out.println("getting message listener container by id : " + listenerId);
        return ((AbstractMessageListenerContainer) this.rabbitListenerEndpointRegistry
                .getListenerContainer(listenerId)
        );
    }
}
