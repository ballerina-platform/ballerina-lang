import ballerina/jms;
import ballerina/log;

// Initialize a JMS connection with the provider.
jms:Connection conn = new ({
    initialContextFactory: "bmbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
});

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession = new (conn, {
    // Set to client acknowledgment mode.
    acknowledgementMode: "CLIENT_ACKNOWLEDGE"
});

// Initialize a Queue receiver using the created session.
endpoint jms:QueueReceiver consumer {
    session: jmsSession,
    queueName: "MyQueue"
};

// Bind the created consumer to the listener service.
service<jms:Consumer> jmsListener bind consumer {

    // The `OnMessage` resource gets invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
        // Retrieve the text message.
        match (message.getTextMessageContent()) {
            string messageText => {
                log:printInfo("Message : " + messageText);
                // Acknowledge the received message using the queue receiver endpoint's acknowledge function.
                consumer->acknowledge(message)
                    but { error e => log:printError("Error occurred while acknowledging message", err = e) };
            }
            error e => log:printError("Error occurred while reading message", err = e);
        }
  }
}
