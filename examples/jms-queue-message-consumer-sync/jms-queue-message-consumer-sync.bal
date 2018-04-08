import ballerina/jms;
import ballerina/log;

// Initialize a JMS connection with the provider
jms:Connection jmsConnection = new ({
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
});

// Initialize a JMS session on top of the created connection
jms:Session jmsSession = new (jmsConnection, {
    acknowledgementMode: "AUTO_ACKNOWLEDGE"
});

// Initialize a Queue sender on top of the the created sessions
endpoint jms:QueueConsumer queueConsumer {
    session: jmsSession,
    queueName: "MyQueue"
};

public function main (string[] args) {
    // Receive a message from the JMS provider.
    var result = queueConsumer -> receive(5000);

    match result {
        jms:Message msg => {
            log:printInfo("Message received " + msg.getTextMessageContent());
        }
        () => {
            log:printInfo("Message not received");
        }
    }
}
