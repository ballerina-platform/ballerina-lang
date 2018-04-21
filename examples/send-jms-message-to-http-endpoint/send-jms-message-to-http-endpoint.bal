import ballerina/jms;
import ballerina/http;
import ballerina/log;

// Create a simple queue receiver.
endpoint jms:SimpleQueueReceiver consumer {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    queueName: "MyQueue"
};

// Bind the created JMS consumer to the listener service.
service<jms:Consumer> jmsListener bind consumer {

    onMessage(endpoint consumer, jms:Message message) {
        // Create an HTTP client endpoint.
        endpoint http:Client clientEP {
            url: "http://localhost:9090/"
        };

        string textContent = check message.getTextMessageContent();
        log:printInfo("Message received from broker. Payload: " + textContent);

        // Forward the received text content of the JMS message to the backend Service
        // over using the HTTP client endpoint.
        http:Request req = new;
        req.setStringPayload(textContent);
        http:Response response = check clientEP->post("/backend/jms", request=req);

        string responseMessage = check response.getStringPayload();
        log:printInfo("Response from backend service: " + responseMessage);
    }
}

@Description {value:"Backend service that receive the forwarded message from the broker."}
service<http:Service> backend bind {port:9090} {

    @http:ResourceConfig {
        methods: ["POST"],
        path:"/jms"
    }
    jmsPayloadReceiver(endpoint conn, http:Request req) {
        http:Response res = new;

        string stringPayload = check req.getStringPayload();
        log:printInfo("Message received from backend service. Payload: " + stringPayload);

        // A util method that can be used to set string payload.
        res.setStringPayload("Message Received.");
        // Sends the response back to the client.
        _ = conn -> respond(res);
    }
}
