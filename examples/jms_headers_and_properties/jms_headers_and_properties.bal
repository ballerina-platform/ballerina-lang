import ballerina/jms;
import ballerina/mb;
import ballerina/log;

// Initialize a JMS connection with the provider.
jms:Connection conn = new ({
    initialContextFactory: "bmbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
});

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession = new (conn, {
    // An optional property that defaults to `AUTO_ACKNOWLEDGE`.
    acknowledgementMode: "AUTO_ACKNOWLEDGE"
});

// Initialize a queue receiver using the created session.
endpoint jms:QueueReceiver consumer {
    session: jmsSession,
    queueName: "MyQueue"
};

// Bind the created consumer to the listener service.
service<jms:Consumer> jmsListener bind consumer {

    // The `OnMessage` resource gets invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
      // Create a queue sender.
        endpoint jms:SimpleQueueSender queueSender {
            initialContextFactory: "bmbInitialContextFactory",
            providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
            queueName: "RequestQueue"
        };
        string messageText = check message.getTextMessageContent();
        // Retrieve JMS message headers
        var correlationId = check message.getCorrelationID();
        string messageType = check message.getType();

        match(correlationId) {
          string s => log:printInfo("Correlation ID: " + s);
          () => log:printInfo("Correlation ID not set.");
        }

        log:printInfo("Message Type: " + messageType);
        log:printInfo("Message Text: " + messageText);

        // Retrieve custom JMS string property.
        var customProperty = check message.getStringProperty("ShoeSize");
        match (customProperty) {
          string s => log:printInfo("Shoe size: " + s);
          () => log:printInfo("Please provide the shoe size.");
        }

        jms:Message outMessage = check queueSender.createTextMessage("Hello From Ballerina!");
        // Set JMS header, Correlation ID
        check outMessage.setCorrelationID("Msg:1");

        // Set a JMS string property property
        check outMessage.setStringProperty("Instruction", "Do a perfect Pirouette");

        check queueSender->send(outMessage);
  }
}
