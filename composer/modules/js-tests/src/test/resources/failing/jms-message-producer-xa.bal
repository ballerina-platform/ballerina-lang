import ballerina.net.jms;

function main (string[] args) {
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

    // Send the Ballerina message to the JMS provider using Ballerina-JMS XA transactions.
    // XA transactions can use used when multiple JMS Client Connector sends or other any other Ballerina Transacted
    // action(s) in present.
    // This example scenario can be achieved with Local transactions as well and it is the ideal way to do it. XA is
    // used for demonstration purpose
    transaction {
        jmsEP.send("MyQueue", queueMessage);
        jmsEP.send("MySecondQueue", queueMessage);
    } failed {
        println("Rollbacked");
    } committed {
        println("Committed");
    }
}

function getConnectorConfig () (jms:ClientProperties) {
    jms:ClientProperties properties = {
                                          initialContextFactory:"wso2mbInitialContextFactory",
                                          configFilePath:"../jndi.properties",
                                          connectionFactoryName: "QueueConnectionFactory",
                                          connectionFactoryType : "queue",
                                          acknowledgementMode: "XA_TRANSACTED"
                                      };
    return properties;
}
