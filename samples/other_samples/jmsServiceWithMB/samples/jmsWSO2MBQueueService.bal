package jmsServiceWithMB.samples;

import ballerina.net.jms;
import ballerina.lang.messages;
import ballerina.lang.system;

@jms:JMSSource {
    factoryInitial : "org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
    providerUrl : "jndi.properties"
    }
@jms:ConnectionProperty{key:"connectionFactoryType", value:"queue"}
@jms:ConnectionProperty{key:"destination", value:"ballerinaqueue"}
@jms:ConnectionProperty{key:"connectionFactoryJNDIName", value:"QpidConnectionFactory"}

service jmsWSO2MBQueueService {

    resource onMessage (message m) {
        string messageType;
        map dataMap = {};
        messageType = messages:getProperty(m, "JMS_MESSAGE_TYPE");
        system:println(messages:getStringValue(m, "queue message count"));
        system:println(messageType);
        jms:ClientConnector jmsEP = create jms:ClientConnector("org.wso2.andes.jndi.PropertiesFileInitialContextFactory", "jndi.properties");
        jms:ClientConnector.send(jmsEP, "QpidConnectionFactory", "ballerinatopic", "topic", messageType, m, dataMap);

    }
}
