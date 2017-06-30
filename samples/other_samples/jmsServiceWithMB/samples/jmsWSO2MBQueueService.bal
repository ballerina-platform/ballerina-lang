package jmsServiceWithMB.samples;

import ballerina.net.jms;
import ballerina.lang.messages;
import ballerina.lang.system;

@Source (
protocol = "jms", destination = "ballerinaqueue", connectionFactoryJNDIName = "QpidConnectionFactory",
factoryInitial = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory", providerUrl = "jndi.properties",
connectionFactoryType = "queue")
service<jms> jmsWSO2MBQueueService {

    resource onMessage (message m) {
        string messageType;
        map dataMap = {};
        messageType = jms:getMessageType(m);
        system:println(messages:getStringValue(m, "queue message count"));
        system:println(messageType);
        jms:ClientConnector jmsEP = create jms:ClientConnector("org.wso2.andes.jndi.PropertiesFileInitialContextFactory", "jndi.properties");
        jms:ClientConnector.send(jmsEP, "QpidConnectionFactory", "ballerinatopic", "topic", messageType, m, dataMap);

    }
}
