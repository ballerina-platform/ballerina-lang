import ballerina.net.jms;
import ballerina.lang.messages;
import ballerina.lang.system;

@Source (
protocol = "jms", destination = "ballerinaqueue", connectionFactoryJNDIName = "QpidConnectionFactory",
factoryInitial = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory", providerUrl = "jndi.properties",
connectionFactoryType = "queue")
service jmsWSO2MBQueueService {

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


@Source (
protocol = "jms", destination = "ballerinatopic", connectionFactoryJNDIName = "QpidConnectionFactory",
factoryInitial = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory", providerUrl = "jndi.properties",
connectionFactoryType = "topic")
service jmsWSO2MBTopicService {

    resource onMessage (message m) {
        system:println("Received map message : " + messages:getStringValue(m, "queue message count"));
    }
}
