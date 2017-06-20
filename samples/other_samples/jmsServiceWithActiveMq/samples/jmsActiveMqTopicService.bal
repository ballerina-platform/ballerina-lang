package jmsServiceWithActiveMq.samples;

import ballerina.net.jms;
import ballerina.lang.messages;
import ballerina.lang.system;

@jms:JMSSource {
    factoryInitial : "org.apache.activemq.jndi.ActiveMQInitialContextFactory",
    providerUrl : "tcp://localhost:61616"
    }
@jms:ConnectionProperty{key:"connectionFactoryType", value:"topic"}
@jms:ConnectionProperty{key:"destination", value:"ballerinatopic"}
@jms:ConnectionProperty{key:"connectionFactoryJNDIName", value:"TopicConnectionFactory"}
service jmsActiveMqTopicService {

    resource onMessage (message m) {
        system:println("Received " + messages:getProperty(m, "JMS_MESSAGE_TYPE") + " : " + messages:getStringValue(m, "queue message count"));
    }
}
