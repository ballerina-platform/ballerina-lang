import ballerina/jms;
import ballerina/log;

// This creates a simple durable topic subscriber.
listener jms:DurableTopicSubscriber subscriberEndpoint = new({
        initialContextFactory: "bmbInitialContextFactory",
        providerUrl: "amqp://admin:admin@carbon/carbon"
            + "?brokerlist='tcp://localhost:5672'",
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    }, "BallerinaTopic", "sub1");

// This binds the created consumer to the listener service.
service jmsListener on subscriberEndpoint {

    // This resource is invoked when a message is received.
    resource function onMessage(jms:TopicSubscriberCaller consumer,
                                jms:Message message) {
        // Retrieve the text message.
        var messageText = message.getTextMessageContent();
        if (messageText is string) {
            log:printInfo("Message : " + messageText);
        } else {
            log:printError("Error occurred while reading message",
                err = messageText);
        }
    }
}
