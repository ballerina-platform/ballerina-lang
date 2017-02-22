import ballerina.net.jms;
import ballerina.lang.messages;

function jmsClientConnectorTest() (boolean) {
    jms:ClientConnector jmsEP = create jms:ClientConnector("", "file:///tmp/jndi.properties");
    message queueMessage = {};
    map dataMap = {};
    dataMap = {};
    messages:setStringPayload(queueMessage, "Hello from ballerina");
    jms:ClientConnector.send(jmsEP, "QueueConnectionFactory", "MyQueue", "queue", "TextMessage", queueMessage, dataMap);
    return true;
}