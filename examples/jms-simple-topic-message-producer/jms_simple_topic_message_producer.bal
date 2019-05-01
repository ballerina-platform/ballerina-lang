import ballerina/jms;
import ballerina/log;

// This creates a topic publisher.  This example makes use of the ActiveMQ'
// Artemis broker for demonstration while it can be tried with other brokers
// that support JMS.

jms:TopicPublisher topicPublisher = new({
        initialContextFactory: 
        "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616",
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    }, topicPattern = "MyTopic");

public function main() {
    // This creates a Text message.
    var msg = topicPublisher.session.createTextMessage("Hello from Ballerina");
    if (msg is jms:Message) {
        // This sends the Ballerina message to the JMS provider.
        var returnVal = topicPublisher->send(msg);
        if (returnVal is error) {
            log:printError("Error occurred while sending message",
                err = returnVal);
        }
    } else {
        log:printError("Error occurred while creating message", err = msg);
    }
}
