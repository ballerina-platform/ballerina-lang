import ballerina.net.jms;
import ballerina.lang.message;
import ballerina.lang.system;

@Source (
protocol = "jms", destination = "ballerinaqueue", connectionFactoryJNDIName = "QueueConnectionFactory",
factoryInitial = "org.apache.activemq.jndi.ActiveMQInitialContextFactory", providerUrl = "tcp://localhost:61616",
connectionFactoryType = "queue")
service jmsWSO2ActiveMqQueueService {
    @OnMessage
    resource onMessage (message m) {
        string messageType;
        map dataMap = {};
        messageType = jms:getMessageType(m);
    	system:println(messageType);
        system:println(message:getStringValue(m, "queue message count"));
        jms:ClientConnector jmsEP = create jms:ClientConnector("org.apache.activemq.jndi.ActiveMQInitialContextFactory", "tcp://localhost:61616");
        jms:ClientConnector.send(jmsEP, "TopicConnectionFactory", "ballerinatopic", "topic", messageType, m, dataMap);
    }
}


@Source (
protocol = "jms", destination = "ballerinatopic", connectionFactoryJNDIName = "TopicConnectionFactory",
factoryInitial = "org.apache.activemq.jndi.ActiveMQInitialContextFactory", providerUrl = "tcp://localhost:61616",
connectionFactoryType = "topic")
service jmsWSO2ActiveMqTopicService {
    @OnMessage
    resource onMessage (message m) {
        system:println("Received map message : " + message:getStringValue(m, "queue message count"));
    }
}
