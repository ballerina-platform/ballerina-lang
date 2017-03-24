import ballerina.net.jms;
import ballerina.lang.messages;

function jmsClientConnectorTestWrongContextFactory() (boolean) {
    jms:ClientConnector jmsEP = create jms:ClientConnector("", "file:///tmp/jndi.properties");
    message queueMessage = {};
    map dataMap = {};
    dataMap = {};
    messages:setStringPayload(queueMessage, "Hello from ballerina");
    jms:ClientConnector.send(jmsEP, "QueueConnectionFactory", "MyQueue", "queue", "TextMessage", queueMessage, dataMap);
    return true;
}

function jmsClientConnectorTestWrongType() (boolean) {
    jms:ClientConnector jmsEP = create jms:ClientConnector("org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
     "file:///tmp/jndi.properties");
    message queueMessage = {};
    map dataMap = {};
    dataMap = {};
    messages:setStringPayload(queueMessage, "Hello from ballerina");
    jms:ClientConnector.send(jmsEP, "QueueConnectionFactory", "MyQueue", "queue", "JMSMessage", queueMessage, dataMap);
    return true;
}