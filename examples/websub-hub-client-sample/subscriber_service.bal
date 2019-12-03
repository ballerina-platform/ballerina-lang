// The Ballerina WebSub Subscriber service, which represents the callback registered at the Hub.
import ballerina/log;
import ballerina/websub;

// The endpoint to which the subscriber service is bound.
listener websub:Listener websubEP = new(8181);

// Annotations specifying the subscription parameters.
// The omission of `subscribeOnStartUp` as an annotation due to which a subscription request would not be sent
// automatically on the start up.
// Also, the exclusion of the onIntentVerification resource will result in auto intent-verification.
@websub:SubscriberServiceConfig {
    path: "/websub",
    target: ["http://localhost:9191/websub/hub","http://websubpubtopic.com"],
    secret: "Kslk30SNF2AChs2"
}
service websubSubscriber on websubEP {

    // This resource accepts content delivery requests.
    resource function onNotification(websub:Notification notification) {
        var payload = notification.getTextPayload();
        if (payload is string) {
            log:printInfo("WebSub Notification Received: " + payload);
        } else {
            log:printError("Error retrieving payload as string", err = payload);
        }
    }
}
