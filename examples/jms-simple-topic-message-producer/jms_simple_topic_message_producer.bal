import ballerina/jms;
import ballerina/log;

// This creates a topic publisher.
endpoint jms:SimpleTopicPublisher topicPublisher {
    initialContextFactory:"bmbInitialContextFactory",
    providerUrl:"amqp://admin:admin@carbon/carbon"
                + "?brokerlist='tcp://localhost:5672'",
    acknowledgementMode:"AUTO_ACKNOWLEDGE",
    topicPattern:"BallerinaTopic"
};

public function main() {
    // This creates a Text message.
    match (topicPublisher.createTextMessage("Hello from Ballerina")) {
        error e => {
            log:printError("Error occurred while creating message", err=e);
        }

        jms:Message msg => {
            // This sends the Ballerina message to the JMS provider.

            var result = topicPublisher->send(msg);
            if (result is error) {
               log:printError("Error occurred while sending message", err = result);
            }
        }
    }
}
