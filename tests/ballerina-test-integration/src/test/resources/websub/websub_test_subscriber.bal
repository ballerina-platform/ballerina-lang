import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/websub;

endpoint websub:SubscriberServiceEndpoint websubEP {
    host:"localhost",
    port:8181
};

@websub:SubscriberServiceConfig {
    basePath:"/websub",
    subscribeOnStartUp:true,
    topic: "http://www.websubpubtopic.com",
    hub: "https://localhost:9999/websub/hub",
    leaseSeconds: 3600000,
    secret: "Kslk30SNF2AChs2"
}
service<websub:SubscriberService> websubSubscriber bind websubEP {

    onIntentVerification (endpoint client, websub:IntentVerificationRequest request) {
        http:Response response = new;
        match (request.buildSubscriptionVerificationResponse()) {
            http:Response httpResponse => {
                io:println("Intent verified for subscription request");
                response = httpResponse;
            }
            () => {
                io:println("Intent verification for subscription request denied");
                response.statusCode = 404;
            }
        }
        _ = client -> respond(response);
    }

    onNotification (websub:NotificationRequest notification) {
        string notificationPayload = notification.payload.toString() but { () => "" };
        io:println("WebSub Notification Received: " + notificationPayload);
    }

}

