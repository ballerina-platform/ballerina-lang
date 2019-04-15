import ballerina/jms;
import ballerina/log;

// This initializes a JMS connection with the provider. This example makes use
// of the ActiveMQ Artemis broker for demonstration while it can be tried with
// other brokers that support JMS.
jms:Connection conn = new({
        initialContextFactory:
         "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

// This initializes a JMS session on top of the created connection.
jms:Session jmsSession = new(conn, {
        // Set to client acknowledgment mode.
        acknowledgementMode: "CLIENT_ACKNOWLEDGE"
    });

// This initializes a queue receiver using the created session.
listener jms:QueueReceiver consumerEndpoint = new(jmsSession, queueName = "MyQueue");

// This binds the created consumer to the listener service.
service jmsListener on consumerEndpoint {

    // This resource is invoked when a message is received.
    resource function onMessage(jms:QueueReceiverCaller consumer,
    jms:Message message) {
        // This retrieves the text message.
        var result = message.getTextMessageContent();
        if (result is string) {
            log:printInfo("Message : " + result);
            // This acknowledges the received message using the acknowledge function
            // of the queue receiver endpoint.
            var ack = consumer->acknowledge(message);
            if (ack is error) {
                log:printError("Error occurred while acknowledging message",
                    err = ack);
            }
        } else {
            log:printError("Error occurred while reading message",
                err = result);
        }

    }
}
