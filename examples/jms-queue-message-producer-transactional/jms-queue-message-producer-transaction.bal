import ballerina/jms;
import ballerina/log;

// Initialize a JMS connection with the provider
jms:Connection jmsConnection = new ({
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
});

// Initialize a JMS session on top of the created connection
jms:Session jmsSession = new (jmsConnection, {
    acknowledgementMode: "SESSION_TRANSACTED"
});

endpoint jms:QueueSender queueSender {
    session: jmsSession,
    queueName: "MyQueue"
};

public function main (string[] args) {
    transaction {
        // Create a Text message.
        jms:Message m = check jmsSession.createTextMessage("Test Text");
        // Send the Ballerina message to the JMS provider.
        queueSender -> send(m);
    }
}
