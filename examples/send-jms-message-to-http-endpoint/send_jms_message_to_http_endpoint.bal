import ballerina/http;
import ballerina/jms;
import ballerina/log;

// Create a simple queue receiver.
jms:SimpleQueueReceiver consumerEndpoint = new({
    initialContextFactory:"bmbInitialContextFactory",
    providerUrl:"amqp://admin:admin@carbon/carbon"
                + "?brokerlist='tcp://localhost:5672'",
    acknowledgementMode:"AUTO_ACKNOWLEDGE",
    queueName:"MyQueue"
});

// Bind the created JMS consumer to the listener service.
service jmsListener on consumerEndpoint {

    resource function onMessage(jms:QueueReceiver consumer,
         jms:Message message) {
        var textContent = message.getTextMessageContent();
        if (textContent is string) {
            log:printInfo("Message received from broker. Payload: " +
                              textContent);
            forwardToBakend(untaint textContent);
        } else {
            log:printError("Error while reading message", err = e);
        }
    }
}

function forwardToBakend(string textContent) {
    // Create an HTTP client endpoint.
    http:Client clientEP {
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
service backend on new http:Listener(9090) {

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/jms"
    }
    resource function jmsPayloadReceiver(http:Caller conn, http:Request req) {
        http:Response res = new;

        var stringPayload = req.getTextPayload();
        match (req.getTextPayload()) {
            string stringPayload => {
                log:printInfo("Message received from backend service. "
                              + "Payload: " + stringPayload);

                // A util method that can be used to set string payload.
                res.setPayload("Message Received.");
                // Sends the response back to the client.

                var result = conn->respond(res);
                if (result is error) {
                   log:printError("Error occurred while acknowledging message", err = result);
                }
            }
            error e => log:printError("Error while reading payload", err = e);
        }
    }
}
