import ballerina.net.jms;

@Description{value : "Service level annotation to provide connection details. Connection factory type can be either queue or topic depending on the requirement. "}
@jms:configuration {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl:
    "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryName:"QueueConnectionFactory",
    concurrentConsumers:300,
    destination:"MyQueue"
}
service<jms> jmsService {
    resource onMessage (jms:JMSMessage m) {

        // Retrieve the string payload using native function.
        string stringPayload = m.getTextMessageContent();

        // Print the retrieved payload.
        println("Payload: " + stringPayload);
    }
}
