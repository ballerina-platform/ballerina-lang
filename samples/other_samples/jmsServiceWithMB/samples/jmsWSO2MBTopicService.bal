package jmsServiceWithMB.samples;

import ballerina.net.jms;
import ballerina.lang.messages;
import ballerina.lang.system;

@jms:JMSSource {
    factoryInitial : "org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
    providerUrl : "jndi.properties"
    }
@jms:ConnectionProperty{key:"connectionFactoryType", value:"topic"}
@jms:ConnectionProperty{key:"destination", value:"ballerinatopic"}
@jms:ConnectionProperty{key:"connectionFactoryJNDIName", value:"QpidConnectionFactory"}

service jmsWSO2MBTopicService {

    resource onMessage (message m) {
        system:println("Received " + messages:getProperty(m, "JMS_MESSAGE_TYPE") + " : " + messages:getStringValue(m, "queue message count"));
    }
}
