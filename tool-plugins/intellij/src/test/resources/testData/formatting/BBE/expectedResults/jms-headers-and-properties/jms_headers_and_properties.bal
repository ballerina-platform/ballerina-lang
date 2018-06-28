import ballerina/jms;
import ballerina/mb;
import ballerina/log;

// Initialize a JMS connection with the provider.
jms:Connection conn = new({
        initialContextFactory: "bmbInitialContextFactory",
        providerUrl: "amqp://admin:admin@carbon/carbon"
            + "?brokerlist='tcp://localhost:5672'"
    });

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession = new(conn, {
        // An optional property that defaults to `AUTO_ACKNOWLEDGE`.
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

// Initialize a queue receiver using the created session.
endpoint jms:QueueReceiver consumerEndpoint {
    session: jmsSession,
    queueName: "MyQueue"
};

// Bind the created consumer to the listener service.
service<jms:Consumer> jmsListener bind consumerEndpoint {

    // The `OnMessage` resource gets invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
        // Create a queue sender.
        endpoint jms:SimpleQueueSender queueSender {
            initialContextFactory: "bmbInitialContextFactory",
            providerUrl: "amqp://admin:admin@carbon/carbon"
                + "?brokerlist='tcp://localhost:5672'",
            queueName: "RequestQueue"
        };

        match (message.getTextMessageContent()) {
            string content => log:printInfo("Message Text: " + content);
            error e => log:printError("Error retrieving content", err = e);
        }

        // Retrieve JMS message headers
        match (message.getCorrelationID()) {
            string id => log:printInfo("Correlation ID: " + id);
            () => log:printInfo("Correlation ID not set");
            error e => log:printError("Error getting correlation id", err = e);
        }

        match (message.getType()) {
            string msgType => log:printInfo("Message Type: " + msgType);
            error e => log:printError("Error getting message type", err = e);
        }

        // Retrieve custom JMS string property.
        match (message.getStringProperty("ShoeSize")) {
            string size => log:printInfo("Shoe size: " + size);
            () => log:printInfo("Please provide the shoe size");
            error e => log:printError("Error getting string property", err = e);
        }

        // Create a new text message
        match (queueSender.createTextMessage("Hello From Ballerina!")) {
            error e => log:printError("Error creating message", err = e);

            jms:Message msg => {
                // Set JMS header, Correlation ID
                match (msg.setCorrelationID("Msg:1")) {
                    error e => log:printError("Error seeting correlation id",
                        err = e);
                    () => {}
                }
                // Set a JMS string property
                match (msg.setStringProperty("Instruction",
                    "Do a perfect Pirouette")) {
                    error e => log:printError("Error seeting string property",
                        err = e);
                    () => {}
                }
                var result = queueSender->send(msg);
                match (result) {
                    error e => log:printError("Error sending message to broker",
                        err = e);
                    () => {}
                }
            }
        }
    }
}
