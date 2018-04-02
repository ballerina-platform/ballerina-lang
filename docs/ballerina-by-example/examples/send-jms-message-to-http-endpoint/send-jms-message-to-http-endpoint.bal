
import ballerina/lang.messages;
import ballerina/net.jms;
import ballerina/http;

@jms:configuration {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl:
           "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue",
    acknowledgmentMode: "AUTO_ACKNOWLEDGE"
}
service<jms> jmsService {
    resource onMessage (message m) {
        endpoint<http:HttpClient> nyseEP2 {
            create http:HttpClient("http://localhost:8080", {});
        }
        // Retrieve the string payload using native function and set as a 
        // JSON payload.
        messages:setJsonPayload(m, messages:getJsonPayload(m));
        // Remove unrelated JMS headers
        messages:removeHeader(m, jms:HEADER_CORRELATION_ID);
        messages:removeHeader(m, jms:HEADER_MESSAGE_ID);
        messages:removeHeader(m, jms:HEADER_DESTINATION);
        messages:removeHeader(m, jms:HEADER_DELIVERY_MODE);
        messages:removeHeader(m, jms:HEADER_PRIORITY);
        messages:removeHeader(m, jms:HEADER_TIMESTAMP);
        messages:removeHeader(m, jms:HEADER_EXPIRATION);
        messages:removeHeader(m, jms:HEADER_MESSAGE_TYPE);
        messages:removeHeader(m, jms:HEADER_REDELIVERED);
        // Send the JSON payload to the HTTP endpoint.
        message response = nyseEP2.post("/my-webapp/echo", m);
    }
}
