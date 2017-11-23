import ballerina.net.jms;

function main (string[] args) {
    jmsSender();
}

function jmsSender() {
    endpoint<jms:JmsClient> jmsEP {
         create jms:JmsClient (getConnectorConfig());
    }

    // Create an empty Ballerina message.
    jms:JMSMessage topicMessage = jms:createTextMessage(getConnectorConfig());
    // Set a string payload to the message.
    topicMessage.setTextMessageContent("Hello from Ballerina!");
    // Send the Ballerina message to the JMS provider.
    jmsEP.send("mytopic", topicMessage);
}

function getConnectorConfig () (jms:ClientProperties) {
    jms:ClientProperties properties = {
                                          initialContextFactory:"wso2mbInitialContextFactory",
                                          configFilePath:"../jndi.properties",
                                          connectionFactoryName: "TopicConnectionFactory",
                                          connectionFactoryType : "topic"
                                      };
    return properties;
}
