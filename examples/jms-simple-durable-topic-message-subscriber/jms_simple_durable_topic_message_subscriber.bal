import ballerina/jms;
import ballerina/log;

// This creates a simple durable topic subscriber.
jms:SimpleDurableTopicConsumerEndpointConfiguration config = {initialContextFactory:"bmbInitialContextFactory",
                                                              providerUrl:"amqp://admin:admin@carbon/carbon"
                                                              + "?brokerlist='tcp://localhost:5672'",
                                                              acknowledgementMode: "AUTO_ACKNOWLEDGE",
                                                              topicPattern:"BallerinaTopic", identifier:"sub1"};

// This binds the created subscriber to the listener service.
service jmsListener on new jms:SimpleDurableTopicConsumer(config) {

    // This resource is invoked when a message is received.
    resource function onMessage(jms:SimpleDurableTopicCaller consumer, jms:Message message) {
        var result = message.getTextMessageContent();
        if(result is string) {
            log:printInfo("Message : " + result);
        } else if (result is error) {
            log:printError("Error occurred while reading message", err=result);
        }
    }
}
