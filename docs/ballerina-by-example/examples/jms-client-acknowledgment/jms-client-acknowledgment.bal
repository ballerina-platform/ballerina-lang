import ballerina/net.jms;
import ballerina/io;

endpoint jms:ConsumerEndpoint ep1 {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
};

@jms:ServiceConfig {
    acknowledgementMode: "CLIENT_ACKNOWLEDGE"
}
service<jms:Service> jmsService bind ep1 {

    onMessage (endpoint client, jms:Message message) {
        // Retrieve content of the text message.
        string messageText = message.getTextMessageContent();
        // Print the retrieved message.
        io:println("Message: " + messageText);
    }
}
