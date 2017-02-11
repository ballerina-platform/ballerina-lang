package samples.connectors.test;

import ballerina.net.jms;
import ballerina.lang.message;



function test() (boolean) {
    jms:JMSConnector jmsEP = create jms:JMSConnector("", "file:///tmp/jndi.properties");
    message queueMessage = {};
    map dataMap = {};
    dataMap = { "country" : "US", "currency" : "Dollar" , "states" : "50"};
    message:setStringPayload(queueMessage, "Hello from ballerina");
    jms:JMSConnector.send(jmsEP, "QueueConnectionFactory", "MyQueue", "queue", "TextMessage", queueMessage, dataMap);
    return true;
}