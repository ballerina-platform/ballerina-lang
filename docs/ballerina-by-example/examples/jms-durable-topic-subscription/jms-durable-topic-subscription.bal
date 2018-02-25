import ballerina.lang.messages;
import ballerina.io;
import ballerina.net.jms;

@Description{value : "Add the subscriptionId when connecting to a topic to create a durable topic subscription."}
@jms:configuration {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl:
           "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"topic",
    connectionFactoryName:"TopicConnectionFactory",
    destination:"MyQueue",
    acknowledgmentMode:"AUTO_ACKNOWLEDGE",
    subscriptionId:"mySub1"
}
service<jms> jmsService {
    resource onMessage (message m) {

        // Retrieve the string payload using native function.
        string stringPayload = messages:getStringPayload(m);

        // Print the retrieved payload.
        io:println("Payload: " + stringPayload);
    }
}
