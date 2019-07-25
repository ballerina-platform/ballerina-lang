import ballerinax/java.jms;
import ballerina/io;

// Initialize a JMS connection with the provider. This example uses
// the ActiveMQ Artemis broker. However, it can be tried with
// other brokers that support JMS.

jms:Connection jmsConnection = new({
        initialContextFactory: 
        "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

// Initializes a JMS session on top of the created connection.
jms:Session jmsSession = new(jmsConnection, {
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

// Initializes a queue sender on top of the the created session.
jms:TopicPublisher topicPublisher = new(jmsSession, "BallerinaTopic");

public function main() {
    // Create a text message.
    var msg = new jms:Message(jmsSession, jms:TEXT_MESSAGE);
    if (msg is jms:Message) {
        var err = msg.setPayload("Hello from Ballerina");
        if (err is error) {
            io:println("Unable to set payload" , err.reason());
        }
        // Send the Ballerina message to the JMS provider.
        var returnVal = topicPublisher->send(msg);
        if (returnVal is error) {
            io:println("Error occurred while sending message",
                returnVal.reason());
        }
    } else {
        io:println("Error occurred while creating message",
            msg.reason());
    }
}
