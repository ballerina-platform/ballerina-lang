import ballerina/jms;
import ballerina/log;

// This initializes a JMS connection with the provider.
jms:Connection conn = new({
    initialContextFactory:"bmbInitialContextFactory",
    providerUrl:"amqp://admin:admin@carbon/carbon"
                + "?brokerlist='tcp://localhost:5672'"
});

// This initializes a JMS session on top of the created connection.
jms:Session jmsSession = new(conn, {
    // An optional property that defaults to AUTO_ACKNOWLEDGE.
    acknowledgementMode:"AUTO_ACKNOWLEDGE"
});

// This initializes a topic subscriber using the created session.
endpoint jms:TopicSubscriber subscriberEndpoint {
    session:jmsSession,
    topicPattern:"BallerinaTopic"
};

// This binds the created subscriber to the listener service.
service<jms:Consumer> jmsListener bind subscriberEndpoint {

    // This resource is invoked when a message is received.
    onMessage(endpoint subscriber, jms:Message message) {
        match (message.getTextMessageContent()) {
            string messageText => log:printInfo("Message : " + messageText);
            error e => log:printError("Error occurred while reading message",
                                      err=e);
        }
    }
}
