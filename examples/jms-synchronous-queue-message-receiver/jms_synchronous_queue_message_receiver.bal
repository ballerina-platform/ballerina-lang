import ballerinax/java.jms;
import ballerina/io;

// This initializes a JMS connection with the provider. This example uses
// the ActiveMQ Artemis broker for demonstration. However, it can be tried
// with other brokers that support JMS.

jms:Connection jmsConnection = new({
        initialContextFactory: 
        "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

// Initializes a JMS session on top of the created connection.
jms:Session jmsSession = new(jmsConnection, {
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

// Initializes a queue receiver on top of the created sessions.
listener jms:QueueListener queueReceiver = new(jmsSession, "MyQueue");

public function main() {
    jms:QueueReceiverCaller caller = queueReceiver.getCallerActions();
    // Keeps the JMS session alive until the message is received by the JMS provider.
    // If the message is not received within five seconds, the session times out.
    var result = caller->receive(30000);

    if (result is jms:Message) {
        // This is executed if the message is received.
        var messageText = result.getPayload();
        if (messageText is string) {
            io:println("Message : " + messageText);
        } else if (messageText is error) {
            io:println("Error occurred while reading message.",
                messageText.reason());
        }
    } else if (result is ()) {
        // This is executed if the message is not received within five seconds.
        io:println("Message not received");

    } else {
        // This is executed if an error occurs.
        io:println("Error receiving message : " +
                <string>result.detail()["message"]);
    }
}
