package jmsServiceWithActiveMq.samples;

import ballerina.net.jms;
import ballerina.lang.messages;
import ballerina.lang.system;

@jms:JMSSource {
    factoryInitial : "org.apache.activemq.jndi.ActiveMQInitialContextFactory",
    providerUrl : "tcp://localhost:61616"
    }
@jms:ConnectionProperty{key:"connectionFactoryType", value:"queue"}
@jms:ConnectionProperty{key:"destination", value:"ballerinaqueue"}
@jms:ConnectionProperty{key:"connectionFactoryJNDIName", value:"QueueConnectionFactory"}
service jmsActiveMqQueueService {

    resource onMessage (message m) {
        string messageType;
        map dataMap = {"factoryInitial":"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
                       "providerUrl":"tcp://localhost:61616"};
        messageType = messages:getProperty(m, "JMS_MESSAGE_TYPE");
        system:println(messageType);
        system:println(messages:getStringValue(m, "queue message count"));
        //jms:ClientConnector jmsEP = create jms:ClientConnector("org.apache.activemq.jndi.ActiveMQInitialContextFactory", "tcp://localhost:61616");
        jms:ClientConnector jmsEP = create jms:ClientConnector(dataMap);
        jms:ClientConnector.send(jmsEP, "TopicConnectionFactory", "ballerinatopic", "topic", messageType, m, dataMap);
    }
}
