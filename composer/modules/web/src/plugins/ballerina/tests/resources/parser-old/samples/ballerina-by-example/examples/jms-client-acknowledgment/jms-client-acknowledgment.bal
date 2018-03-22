import ballerina/lang.messages;
import ballerina/lang.system;
import ballerina/net.jms;

@jms:configuration {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl:
        "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue",
    acknowledgmentMode:"CLIENT_ACKNOWLEDGE"
}
service<jms> jmsService {
    resource onMessage (message m) {

        string stringPayload = messages:getStringPayload(m);
        system:println("Payload: " + stringPayload);
        // acknowledge the message with positive acknowledgment. If we want to reject the message due to some error
        // we can use the same method with second argument as 'jms:DELIVERY_ERROR'.
        jms:acknowledge(m, jms:DELIVERY_SUCCESS);

    }
}
