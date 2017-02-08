package samples.connectors.test;

import ballerina.net.jms;
import ballerina.net.http;
import ballerina.lang.message;
import ballerina.lang.system;
import ballerina.lang.json;
import ballerina.lang.xml;



function test() (boolean) {
    jms:JMSConnector jmsEP = new jms:JMSConnector("org.wso2.andes.jndi.PropertiesFileInitialContextFactory", "file:///tmp/jndi.properties");
    message queueMessage;
    map dataMap;
    dataMap = { "country" : "US", "currency" : "Dollar" , "states" : "50"};
    message:setStringPayload(queueMessage, "Hello from ballerina");
    jms:JMSConnector.send(jmsEP, "QueueConnectionFactory", "MyQueue", "queue", "TextMessage", queueMessage, dataMap);
    return true;
}