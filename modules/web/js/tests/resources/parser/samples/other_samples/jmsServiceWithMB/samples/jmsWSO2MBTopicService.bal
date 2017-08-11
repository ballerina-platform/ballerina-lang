package jmsServiceWithMB.samples;

import ballerina.net.jms;
import ballerina.lang.messages;
import ballerina.lang.system;

@Source (
protocol = "jms", destination = "ballerinatopic", connectionFactoryJNDIName = "QpidConnectionFactory",
factoryInitial = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory", providerUrl = "jndi.properties",
connectionFactoryType = "topic")
service<jms> jmsWSO2MBTopicService {

    resource onMessage (message m) {
        system:println("Received " + jms:getMessageType(m) + " : " + messages:getStringValue(m, "queue message count"));
    }
}
