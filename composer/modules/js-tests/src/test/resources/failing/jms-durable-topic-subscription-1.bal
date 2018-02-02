import ballerina.net.jms;

@Description{value : "Add the subscriptionId when connecting to a topic to create a durable topic subscription.
clientId should be set if you are using any other broker. If you susbcribe without a subscription ID it will
automatically be a non-durable susbcription. "}
@jms:configuration {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl:
    "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryName:"TopicConnectionFactory",
    destination:"MyTopic",
    subscriptionId:"mySub",
    connectionFactoryType:jms:TYPE_TOPIC
}
service<jms> jmsService {
    resource onMessage (jms:JMSMessage m) {

        // Retrieve the string payload using native function.
        string stringPayload = m.getTextMessageContent();

        // Print the retrieved payload.
        println("Payload: " + stringPayload);
    }
}