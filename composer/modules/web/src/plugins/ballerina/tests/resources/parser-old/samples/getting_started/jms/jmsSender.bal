import ballerina/net.jms;
import ballerina/lang.messages;

function main (string... args) {
    jmsSender();

}

function jmsSender () (boolean) {
    map properties = {"initialContextFactory":"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
                         "providerUrl":"tcp://localhost:61616",
                         "connectionFactoryName":"QueueConnectionFactory",
                         "connectionFactoryType":"queue"};

    jms:ClientConnector jmsEP = create jms:ClientConnector(properties);
    message queueMessage = {};
    messages:setStringPayload(queueMessage, "Hello from JMS");

    jmsEP.send("MyQueue", queueMessage);

    return true;
}

