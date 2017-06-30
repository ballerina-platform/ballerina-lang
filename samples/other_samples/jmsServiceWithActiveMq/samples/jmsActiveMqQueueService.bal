package jmsServiceWithActiveMq.samples;

import ballerina.net.jms;
import ballerina.lang.messages;
import ballerina.lang.system;

@Source (
protocol = "jms", destination = "ballerinaqueue", connectionFactoryJNDIName = "QueueConnectionFactory",
factoryInitial = "org.apache.activemq.jndi.ActiveMQInitialContextFactory", providerUrl = "tcp://localhost:61616",
connectionFactoryType = "queue")
service<jms> jmsActiveMqQueueService {

    resource onMessage (message m) {
        string messageType;
        map dataMap = {};
        messageType = jms:getMessageType(m);
        system:println(messageType);
        system:println(messages:getStringValue(m, "queue message count"));
        jms:ClientConnector jmsEP = create jms:ClientConnector("org.apache.activemq.jndi.ActiveMQInitialContextFactory", "tcp://localhost:61616");
        jms:ClientConnector.send(jmsEP, "TopicConnectionFactory", "ballerinatopic", "topic", messageType, m, dataMap);
    }
}
