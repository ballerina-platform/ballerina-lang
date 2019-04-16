import ballerina/jms;
import ballerina/log;

// This creates a topic publisher.
jms:TopicPublisher topicPublisher = new({
        initialContextFactory: "bmbInitialContextFactory",
        providerUrl: "amqp://admin:admin@carbon/carbon"
            + "?brokerlist='tcp://localhost:5672'",
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    }, topicPattern = "BallerinaTopic");

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
