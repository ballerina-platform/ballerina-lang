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

function main(string... args) {
    // This creates a Text message.
    match (topicPublisher.createTextMessage("Hello from Ballerina")) {
        error e => {
            log:printError("Error occurred while creating message", err=e);
        }

        jms:Message msg => {
            // This sends the Ballerina message to the JMS provider.
            topicPublisher->send(msg) but {
                error e => log:printError("Error occurred while sending"
                                           + "message", err=e)
            };
        }
    }
}
