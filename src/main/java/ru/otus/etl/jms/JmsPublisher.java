package ru.otus.etl.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.otus.etl.core.EtlException;

@Slf4j
@Component
public class JmsPublisher {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendMessage(final String queueName, final String text) throws EtlException {
        if (queueName == null || queueName.isEmpty()) {
            throw new EtlException("Не указаны JMS топик/очередь");
        }
        ActiveMQDestination dest;
        if (queueName.startsWith("topic:")) {
            dest = new ActiveMQTopic(queueName.replace("topic:", ""));
        } else if (queueName.startsWith("queue:")) {
            dest = new ActiveMQQueue(queueName.replace("queue:", ""));
        } else {
            throw new EtlException("Адрес JMS должен начинаться на queue: или topic:");
        }
        try {
            jmsTemplate.send(dest, new MessageCreator() {

                public Message createMessage(Session session) throws JMSException {
                    TextMessage message = session.createTextMessage(text);
                    return message;
                }
            });
        } catch (Exception e) {
            log.error("sendMessage: failed for {}", queueName, e);
            throw new EtlException("Не удалось отправить сообщение в очередь");
        }
    }

}
