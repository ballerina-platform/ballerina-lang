import ballerina.net.jms.jmsmessage;
import ballerina.net.jms;
import ballerina.net.http;
import ballerina.net.http.request;
import ballerina.net.http.response;
import ballerina.lang.system;

@jms:configuration {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl:
    "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue",
    acknowledgementMode: "AUTO_ACKNOWLEDGE"
}
service<jms> jmsService {
    resource onMessage (jms:JMSMessage m) {
        http:ClientConnector httpConnector;
        http:Options connectorOptions = {};
        httpConnector = create http:ClientConnector("http://localhost:8080", connectorOptions);
        http:Request req = {};

        // Retrieve the string payload using native function and set as a json payload.
        request:setStringPayload(req, jmsmessage:getTextMessageContent(m));

        http:Response resp = httpConnector.post("/my-webapp/echo", req);
        system:println("POST response: ");
        system:println(response:getJsonPayload(resp));
    }
}
