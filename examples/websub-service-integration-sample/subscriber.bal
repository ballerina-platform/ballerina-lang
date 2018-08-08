// Ballerina WebSub Subscriber service, which subscribes to notifications at the Hub.
import ballerina/log;
import ballerina/websub;

// The endpoint to which the subscriber service is bound.
endpoint websub:Listener websubEP {
    port: 8181
};

// Annotations specifying the subscription parameters for the order management service.
// A subscription request would be sent to the hub and topic discovered at the
// resource URL specified.
@websub:SubscriberServiceConfig {
    path: "/ordereventsubscriber",
    subscribeOnStartUp: true,
    resourceUrl: "http://localhost:9090/ordermgt/order",
    leaseSeconds: 3600,
    secret: "Kslk30SNF2AChs2"
}
service websubSubscriber bind websubEP {
    // Define the resource that accepts the content delivery requests.
    onNotification(websub:Notification notification) {
        match (notification.getPayloadAsString()) {
            string payloadAsString => log:printInfo("WebSub Notification Received: "
                    + payloadAsString);
            error e => log:printError("Error retrieving payload as string", err = e);
        }
    }
}
