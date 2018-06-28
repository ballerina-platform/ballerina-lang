import ballerina/jms;
import ballerina/http;
import ballerina/log;

// Create a simple queue receiver.
endpoint jms:SimpleQueueReceiver consumerEndpoint {
    initialContextFactory: "bmbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon"
        + "?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    queueName: "MyQueue"
};

// Bind the created JMS consumer to the listener service.
service<jms:Consumer> jmsListener bind consumerEndpoint {

    onMessage(endpoint consumer, jms:Message message) {

        match (message.getTextMessageContent()) {
            string textContent => {
                log:printInfo("Message received from broker. Payload: "
                        + textContent);

                forwardToBakend(textContent);
            }
            error e => log:printError("Error while reading message", err = e);
        }
    }
}

function forwardToBakend(string textContent) {
    // Create an HTTP client endpoint.
    endpoint http:Client clientEP {
        url: "http://localhost:9090/"
    };

    // Forward the received text content of the JMS message to the backend Service
    // using the HTTP client endpoint.
    http:Request req = new;
    req.setPayload(textContent);
    var result = clientEP->post("/backend/jms", req);
    match (result) {
        http:Response response => {
            match (response.getTextPayload()) {
                string responseMessage =>
                log:printInfo("Response from backend service: "
                        + responseMessage);
                error e =>
                log:printError("Error while reading response", err = e);
            }
        }
        error e => log:printError("Error while sending payload", err = e);
    }
}

// Backend service that receive the forwarded message from the broker.
service<http:Service> backend bind { port: 9090 } {

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/jms"
    }
    jmsPayloadReceiver(endpoint conn, http:Request req) {
        http:Response res = new;

        match (req.getTextPayload()) {
            string stringPayload => {
                log:printInfo("Message received from backend service. "
                        + "Payload: " + stringPayload);

                // A util method that can be used to set string payload.
                res.setPayload("Message Received.");
                // Sends the response back to the client.
                conn->respond(res) but {
                    error e => log:printError("Error occurred while"
                            + "acknowledging message", err = e)
                };
            }
            error e => log:printError("Error while reading payload", err = e);
        }
    }
}
