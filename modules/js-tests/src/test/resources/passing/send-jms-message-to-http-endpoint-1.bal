import ballerina.net.jms;
import ballerina.net.http;

@jms:configuration {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl:
    "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue",
    acknowledgementMode: "CLIENT_ACKNOWLEDGE"
}
service<jms> jmsService {
    resource onMessage (jms:JMSMessage m) {
        endpoint<http:HttpClient> httpConnector {
             create http:HttpClient ("http://localhost:8080",{});
        }

        http:Request req = {};

        // Retrieve the string payload using native function and set as a json payload.
        req.setStringPayload(m.getTextMessageContent());
        var resp,err = httpConnector.get("/my-webapp/echo", req);
        println("POST response: ");
        println(resp.getJsonPayload());
    }
}
