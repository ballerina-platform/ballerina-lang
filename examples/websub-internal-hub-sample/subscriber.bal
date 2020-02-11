// Ballerina WebSub Subscriber service, which subscribes to notifications at a Hub.
import ballerina/http;
import ballerina/log;
import ballerina/websub;

// The endpoint to which the subscriber service is bound.
listener websub:Listener websubEP = new websub:Listener(8181);

// Annotations specifying the subscription parameters.
@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    target: ["http://localhost:9191/websub/hub", "http://websubpubtopic.com"],
    leaseSeconds: 36000,
    secret: "Kslk30SNF2AChs2"
}
service websubSubscriber on websubEP {

    // Define sthe resource, which accepts the intent verification requests.
    // If the resource is not specified, intent verification happens automatically. It verifies if the topic specified in the intent
    // verification request matches the topic specified as the annotation.
    resource function onIntentVerification(websub:Caller caller,
                                   websub:IntentVerificationRequest request) {
        // Builds the response to the intent verification request that was received for subscription.
        http:Response response =
            request.buildSubscriptionVerificationResponse("http://websubpubtopic.com");
        if (response.statusCode == 202) {
            log:printInfo("Intent verified for subscription request");
        } else {
            log:printWarn("Intent verification denied for subscription request");
        }
        var result = caller->respond(<@untainted>response);

        if (result is error) {
            log:printError("Error responding to intent verification request",
                                                    err = result);
        }
    }

    // Defines the resource that accepts the content delivery requests.
    resource function onNotification(websub:Notification notification) {
        var payload = notification.getTextPayload();
        if (payload is string) {
            log:printInfo("WebSub Notification Received: " + payload);
        } else {
            log:printError("Error retrieving payload as string", payload);
        }
    }
}
