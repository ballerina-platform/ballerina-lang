// Ballerina WebSub Subscriber service, which subscribes to notifications at a Hub.
import ballerina/log;
import ballerina/mime;
import ballerina/http;
import ballerina/websub;

// The endpoint to which the subscriber service is bound.
endpoint websub:Listener websubEP {
    port: 8181
};

// Annotations specifying the subscription parameters.
@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    topic: "http://websubpubtopic.com",
    hub: "https://localhost:9191/websub/hub",
    leaseSeconds: 3600000,
    secret: "Kslk30SNF2AChs2"
}
service websubSubscriber bind websubEP {

    // Define the resource that accepts the intent verification requests.
    // If the resource is not specified, intent verification happens automatically. It verifies if the topic specified in the intent verification request matches the topic specified as the annotation.
    onIntentVerification(endpoint caller,
                         websub:IntentVerificationRequest request) {
        // Build the response for the subscription intent verification request that was received.
        http:Response response =
            request.buildSubscriptionVerificationResponse("http://websubpubtopic.com");
        if (response.statusCode == 202) {
            log:printInfo("Intent verified for subscription request");
        } else {
            log:printWarn("Intent verification denied for subscription request");
        }
        caller->respond(response) but {
            error e => log:printError("Error responding to intent verification
                                        request", err = e)
        };
    }

    // Define the resource that accepts the content delivery requests.
    onNotification(websub:Notification notification) {
        match (notification.getPayloadAsString()) {
            string payloadAsString => log:printInfo("WebSub Notification Received: "
                    + payloadAsString);
            error e => log:printError("Error retrieving payload as string", err = e);
        }
    }
}
