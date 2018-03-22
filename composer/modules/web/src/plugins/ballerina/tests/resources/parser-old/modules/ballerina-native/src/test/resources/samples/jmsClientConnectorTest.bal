import ballerina/net.jms;
import ballerina/lang.messages;

function jmsClientConnectorTest() (boolean) {
    map properties = {"providerUrl":"jndi.properties",
                        "connectionFactoryJNDIName": "QueueConnectionFactory",
                        "connectionFactoryType" : "queue"};

    jms:ClientConnector jmsEP = create jms:ClientConnector(properties);
    message queueMessage = {};
    messages:setStringPayload(queueMessage, "Hello from ballerina");
    jms:ClientConnector.send(jmsEP, "MyQueue", "TextMessage", queueMessage);
    return true;
}

function jmsSendNoMessageTest() (boolean) {
    map properties = {"factoryInitial":"org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
                        "providerUrl":"jndi.properties",
                        "connectionFactoryJNDIName": "QueueConnectionFactory",
                        "connectionFactoryType" : "queue"};

    jms:ClientConnector jmsEP = create jms:ClientConnector(properties);
    message queueMessage = {};
    json jsonData = {};
    messages:setJsonPayload(queueMessage, jsonData);
    jms:ClientConnector.send(jmsEP, "MyQueue", "TextMessage", queueMessage);
    return true;
}

function jmsSendMapMessageWithoutData() (boolean) {
    map properties = {"factoryInitial":"org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
                        "providerUrl":"jndi.properties",
                        "connectionFactoryJNDIName": "QueueConnectionFactory",
                        "connectionFactoryType" : "queue"};

    jms:ClientConnector jmsEP = create jms:ClientConnector(properties);
    message queueMessage = {};
    messages:setStringPayload(queueMessage, "Hello from ballerina");
    jms:ClientConnector.send(jmsEP, "MyQueue", "MapMessage", queueMessage);
    return true;
}