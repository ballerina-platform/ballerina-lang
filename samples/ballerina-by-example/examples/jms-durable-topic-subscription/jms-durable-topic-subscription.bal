import ballerina.lang.system;
import ballerina.net.jms;
import ballerina.doc;
import ballerina.net.jms.jmsmessage;

@doc:Description{value : "Add the subscriptionId when connecting to a topic to create a durable topic subscription. clientId should be set if you are using any other broker. "}
@jms:configuration {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl:
    "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"topic",
    connectionFactoryName:"TopicConnectionFactory",
    destination:"MyTopic",
    subscriptionId:"mySub1"
}
service<jms> jmsService {
    resource onMessage (jms:JMSMessage m) {

        // Retrieve the string payload using native function.
        string stringPayload = jmsmessage:getTextMessageContent(m);

        // Print the retrieved payload.
        system:println("Payload: " + stringPayload);
    }
}