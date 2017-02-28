package jmsServiceWithActiveMq.samples;

import ballerina.net.jms;
import ballerina.lang.messages;
import ballerina.lang.system;

@Source (
protocol = "jms", destination = "ballerinatopic", connectionFactoryJNDIName = "TopicConnectionFactory",
factoryInitial = "org.apache.activemq.jndi.ActiveMQInitialContextFactory", providerUrl = "tcp://localhost:61616",
connectionFactoryType = "topic")
service jmsActiveMqTopicService {

    resource onMessage (message m) {
        system:println("Received " + jms:getMessageType(m) + " : " + messages:getStringValue(m, "queue message count"));
    }
}
