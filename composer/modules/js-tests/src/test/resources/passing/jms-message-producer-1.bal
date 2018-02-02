import ballerina.net.jms;

function main (string[] args) {
    jmsSender();
}

function jmsSender() {
    endpoint<jms:JmsClient> jmsEP {
        create jms:JmsClient (getConnectorConfig());
    }

    // Create an empty Ballerina message.
    jms:JMSMessage queueMessage = jms:createTextMessage(getConnectorConfig());
    // Set a string payload to the message.
    queueMessage.setTextMessageContent("Hello from Ballerina!");
    // Send the Ballerina message to the JMS provider.
    jmsEP.send("MyQueue", queueMessage);
}

function getConnectorConfig () (jms:ClientProperties) {
     // We define the connection properties as a map. 'providerUrl' or 'configFilePath' and the 'initialContextFactory' vary according to        //the JMS provider you use.
     // In this example we connect to the WSO2 MB server.
    jms:ClientProperties properties = {   initialContextFactory:"wso2mbInitialContextFactory",
                                          configFilePath:"../jndi.properties",
                                          connectionFactoryName: "QueueConnectionFactory",
                                          connectionFactoryType : "queue"};
    return properties;
}