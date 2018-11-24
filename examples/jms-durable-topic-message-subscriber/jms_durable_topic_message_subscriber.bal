import ballerina/jms;
import ballerina/log;

// This initializes a JMS connection with the provider.
jms:Connection conn = new({
    initialContextFactory:"bmbInitialContextFactory",
    providerUrl:"amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
});

// This initializes a JMS session on top of the created connection.
jms:Session jmsSession = new(conn, {
    // An optional property that defaults to `AUTO_ACKNOWLEDGE`.
    acknowledgementMode:"AUTO_ACKNOWLEDGE"
});

jms:DurableTopicConsumerEndpointConfiguration config = {session:jmsSession, topicPattern:"BallerinaTopic",
                                                        identifier:"sub1"};

// This binds the created subscriber to the listener service.
service jmsListener on new jms:DurableTopicConsumer(config) {
    // This resource is invoked when a message is received.
    resource function onMessage(jms:DurableTopicCaller consumer, jms:Message message) {
        var result = message.getTextMessageContent();
        if (result is string) {
            log:printInfo("Message : " + result);
        } else if (result is error) {
            log:printError("Error occurred while reading message", err=result);
        }
    }
}
