import ballerina.lang.system;
import ballerina.net.jms;
import ballerina.net.jms.jmsmessage;

@jms:configuration {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl:
    "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue",
    acknowledgementMode:"CLIENT_ACKNOWLEDGE"
}
service<jms> jmsService {
    resource onMessage (jms:JMSMessage m) {

        string stringPayload = jmsmessage:getTextMessageContent(m);
        system:println("Payload: " + stringPayload);
        // acknowledge the message with positive acknowledgment. If we want to reject the message due to some error
        // we can use the same method with second argument as "ERROR".
        jms:acknowledge(m, "SUCCESS");

    }
}
