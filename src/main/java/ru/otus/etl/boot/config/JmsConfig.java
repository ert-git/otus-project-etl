package ru.otus.etl.boot.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class JmsConfig {
    private final String brokerUrl;
    private final String brokerUsername;
    private final String brokerPassword;
    private final int TIMEOUT = 10_000;

    public JmsConfig(@Value("${spring.activemq.broker-url}") String brokerUrl,
            @Value("${spring.activemq.user}") String brokerUsername,
            @Value("${spring.activemq.password}") String brokerPasswd) {
        this.brokerUrl = brokerUrl;
        this.brokerUsername = brokerUsername;
        this.brokerPassword = brokerPasswd;
    }

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);
        connectionFactory.setPassword(brokerUsername);
        connectionFactory.setUserName(brokerPassword);
        connectionFactory.setConnectResponseTimeout(TIMEOUT);
        return connectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(connectionFactory());
        return template;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrency("1-1");
        return factory;
    }

}
