import ballerina.net.jms;
import ballerina.lang.message;
import ballerina.lang.system;

@Source (
protocol = "jms", Destination = "ballerinaqueue", ConnectionFactoryJNDIName = "QpidConnectionFactory",
FactoryInitial = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory", ProviderUrl = "jndi.properties",
ConnectionFactoryType = "queue")
service jmsWSO2MBQueueService {
    @OnMessage
    resource onMessage (message m) {
        string messageType;
        map dataMap = {};
        messageType = jms:getMessageType(m);
        system:println(message:getStringValue(m, "queue message count"));
    	system:println(messageType);
        jms:ClientConnector jmsEP = create jms:ClientConnector("org.wso2.andes.jndi.PropertiesFileInitialContextFactory", "jndi.properties");
        jms:ClientConnector.send(jmsEP, "QpidConnectionFactory", "ballerinatopic", "topic", messageType, m, dataMap);

    }
}


@Source (
protocol = "jms", Destination = "ballerinatopic", ConnectionFactoryJNDIName = "QpidConnectionFactory",
FactoryInitial = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory", ProviderUrl = "jndi.properties",
ConnectionFactoryType = "topic")
service jmsWSO2MBTopicService {
    @OnMessage
    resource onMessage (message m) {
        system:println("Received map message : " + message:getStringValue(m, "queue message count"));
    }
}
