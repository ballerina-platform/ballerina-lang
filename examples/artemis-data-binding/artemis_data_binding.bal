import ballerina/artemis;
import ballerina/http;
import ballerina/log;

type Order record {
    string description?;
    int id;
    float cost;
};

// Consumer listens to the queue (i.e., "my_queue") with the address
// (i.e., "my_address").
@artemis:ServiceConfig {
    queueConfig: {
        queueName: "my_queue",
        addressName: "my_address"
    }
}
// Attaches the service to the listener.
service artemisConsumer on new artemis:Listener(
                            { host: "localhost", port: 61616 }) {

    // This resource is triggered when a valid `Order` is received.
    resource function onMessage(artemis:Message message, Order orderDetails)
                         returns error? {

        // Posts order details to the backend and awaits response.
        http:Client clientEP = new("http://www.mocky.io");
        var response = clientEP->post("/v2/5cde49ef3000005e004307f0",
                             <@untainted> check json.convert(orderDetails));
        if (response is http:Response) {
            log:printInfo(check response.getTextPayload());
        } else {
            log:printError("Invalid response ", err = response);
        }
    }

    resource function onError(artemis:Message message, artemis:ArtemisError err) {
        log:printError(err.detail().message);
    }
}
