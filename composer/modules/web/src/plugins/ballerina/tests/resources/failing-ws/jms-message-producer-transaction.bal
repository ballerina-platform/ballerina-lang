import ballerina/net.jms;

function main (string... args) {
    jmsTransactedSender();
}

function jmsTransactedSender() {
    endpoint<jms:JmsClient> jmsEP {
        create jms:JmsClient (getConnectorConfig());
    }

    // Create an empty Ballerina message.
    jms:JMSMessage queueMessage = jms:createTextMessage(getConnectorConfig());
    // Set a string payload to the message.
    queueMessage.setTextMessageContent("Hello from Ballerina!");
    // Send the Ballerina message to the JMS provider using JMS Local transactions. Local transactions can only be used
    // when you are sending multiple messages using the same ClientConnector
    transaction {
        jmsEP.send("MyQueue", queueMessage);
        jmsEP.send("MySecondQueue", queueMessage);
    }
}


function getConnectorConfig () (jms:ClientProperties) {
    jms:ClientProperties properties = {
                                          initialContextFactory:"wso2mbInitialContextFactory",
                                          configFilePath:"../jndi.properties",
                                          connectionFactoryName: "QueueConnectionFactory",
                                          connectionFactoryType : "queue",
                                          acknowledgementMode: "SESSION_TRANSACTED"};
    return properties;
}