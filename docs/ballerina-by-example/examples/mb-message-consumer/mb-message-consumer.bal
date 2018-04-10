import ballerina/jms;
import ballerina/mb;
import ballerina/io;

endpoint mb:ConsumerEndpoint ep1 {
    brokerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
};

service<jms:Service> jmsService bind ep1 {

    onMessage (endpoint client, jms:Message message) {
        // Retrieve the content of the text message.
        string messageText = message.getTextMessageContent();
        // Print the retrieved message.
        io:println("Message: " + messageText);
    }
}
