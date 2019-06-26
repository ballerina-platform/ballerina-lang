import ballerina/jms;
import ballerina/log;

// This initializes a JMS connection with the provider. This example uses
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
jms:TopicPublisher topicPublisher = new(jmsSession, topicPattern = "BallerinaTopic");

public function main() {
    // Creates a text message.
    var msg = jmsSession.createTextMessage("Hello from Ballerina");
    if (msg is jms:Message) {
        // Sends the Ballerina message to the JMS provider.
        var returnVal = topicPublisher->send(msg);
        if (returnVal is error) {
            log:printError("Error occurred while sending message",
                err = returnVal);
        }
    } else {
        log:printError("Error occurred while creating message",
            err = msg);
    }
}
