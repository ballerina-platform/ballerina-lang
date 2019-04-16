import ballerina/jms;
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
listener jms:QueueReceiver consumerEndpoint = new(jmsSession,
    queueName = "MyQueue");

// Bind the created consumer to the listener service.
service jmsListener on consumerEndpoint {

    // The `OnMessage` resource gets invoked when a message is received.
    resource function onMessage(jms:QueueReceiverCaller consumer,
                                jms:Message message) {
        // Create a queue sender.
        jms:QueueSender queueSender = new({
                initialContextFactory: "bmbInitialContextFactory",
                providerUrl: "amqp://admin:admin@carbon/carbon"
                    + "?brokerlist='tcp://localhost:5672'"
            }, queueName = "RequestQueue");

        var content = message.getTextMessageContent();
        if (content is string) {
            log:printInfo("Message Text: " + content);
        } else {
            log:printError("Error retrieving content", err = content);
        }

        // Retrieve JMS message headers
        var id = message.getCorrelationID();
        if (id is string) {
            log:printInfo("Correlation ID: " + id);
        } else if (id is ()) {
            log:printInfo("Correlation ID not set");
        } else {
            log:printError("Error getting correlation id", err = id);
        }

        var msgType = message.getType();
        if (msgType is string) {
            log:printInfo("Message Type: " + msgType);
        } else {
            log:printError("Error getting message type", err = msgType);
        }

        // Retrieve custom JMS string property.
        var size = message.getStringProperty("ShoeSize");
        if (size is string) {
            log:printInfo("Shoe size: " + size);
        } else if (size is ()) {
            log:printInfo("Please provide the shoe size");
        } else {
            log:printError("Error getting string property", err = size);
        }

        // Create a new text message.
        var msg = queueSender.session.createTextMessage(
                                         "Hello From Ballerina!");
        if (msg is jms:Message) {
            // Set JMS header, Correlation ID.
            var cid = msg.setCorrelationID("Msg:1");
            if (cid is error) {
                log:printError("Error setting correlation id",
                    err = cid);
            }

            // Set JMS string property.
            var stringProp = msg.setStringProperty("Instruction",
                "Do a perfect Pirouette");
            if (stringProp is error) {
                log:printError("Error setting string property",
                    err = stringProp);
            }
            var result = queueSender->send(msg);
            if (result is error) {
                log:printError("Error sending message to broker",
                    err = result);
            }
        } else {
            log:printError("Error creating message", err = msg);
        }
    }
}
