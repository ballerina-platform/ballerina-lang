import ballerina/jms;
import ballerina/log;

// Initialize a JMS connection with the provider.
jms:Connection conn = new ({
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
});

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession = new (conn, {
    // An optional property that defaults to `AUTO_ACKNOWLEDGE`.
    acknowledgementMode: "AUTO_ACKNOWLEDGE"
});

// Initialize a Durable topic subscriber using the created session.
endpoint jms:DurableTopicSubscriber subscriber {
    session: jmsSession,
    topicPattern: "BallerinaTopic",
    identifier: "sub1"
};

// Bind the created subscriber to the listener service.
service<jms:Consumer> jmsListener bind subscriber {

    // The `OnMessage` resource gets invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
        // Retrieve the text message. The `check` keyword is used for error lifting. It terminates the function if an
        // error occurs.
        string messageText = check message.getTextMessageContent();
        log:printInfo("Message : " + messageText);
  }
}
