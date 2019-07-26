import ballerinax/java.jms;
import ballerina/log;

// This initializes a JMS connection with the provider. This example uses
// the ActiveMQ Artemis broker. However, it can be tried with
// other brokers that support JMS.
jms:Connection conn = new({
        initialContextFactory:
         "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

// Initializes a JMS session on top of the created connection.
jms:Session jmsSession = new(conn, {
        // Sets to the client-acknowledgment mode.
        acknowledgementMode: "CLIENT_ACKNOWLEDGE"
    });

// Initializes a queue receiver using the created session.
listener jms:QueueListener consumerEndpoint = new(jmsSession, "MyQueue");

// Binds the created consumer to the listener service.
service jmsListener on consumerEndpoint {

    // This resource is invoked when a message is received.
    resource function onMessage(jms:QueueReceiverCaller consumer,
    jms:Message message) {
        // Retrieve the text message.
        var result = message.getPayload();
        if (result is string) {
            log:printInfo("Message : " + result);
            // Acknowledge the received message using the acknowledge function
            // of the queue receiver endpoint.
            var ack = message->acknowledge();
            if (ack is error) {
                log:printError("Error occurred while acknowledging message",
                    err = ack);
            }
        } else if(result is error) {
            log:printError("Error occurred while reading message",
                err = result);
        }

    }
}
