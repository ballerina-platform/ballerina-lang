import ballerina.lang.system;
import ballerina.net.jms;
import ballerina.doc;
import ballerina.net.jms.jmsmessage;

@doc:Description{value : "Service level annotation to provide connection details. Connection factory type can be either queue or topic depending on the requirement. "}
@jms:configuration {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl:
    "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue"
}
service<jms> jmsService {
    resource onMessage (jms:JMSMessage m) {

        // Retrieve the string payload using native function.
        string stringPayload = jmsmessage:getTextMessageContent(m);

        // Print the retrieved payload.
        system:println("Payload: " + stringPayload);
    }
}
