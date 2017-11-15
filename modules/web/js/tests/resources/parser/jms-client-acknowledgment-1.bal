import ballerina.net.jms;

@jms:configuration {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl:
    "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue",
    connectionFactoryType:jms:TYPE_QUEUE,
    acknowledgementMode:jms:CLIENT_ACKNOWLEDGE
}
service<jms> jmsService {
    resource onMessage (jms:JMSMessage m) {

        string stringPayload = m.getTextMessageContent();
        println("Payload: " + stringPayload);
        // We don't need to do the explicit acknowledgement. Messages will get acked/restored based on the errors
        // occurred. But in cases of user wants to do this manually, it can be done as follows.
        // Following will acknowledge the message with positive acknowledgment. If the message needs to be rejected
        // use 'jms:DELIVERY_ERROR'
        // m.acknowledge(jms:DELIVERY_SUCCESS);

    }
}
