import ballerina/config;
import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/websub;

endpoint websub:Listener websubEP {
    port:8181
};

@websub:SubscriberServiceConfig {
    path:"/websub",
    subscribeOnStartUp:true,
    topic: "http://one.websub.topic.com",
    hub: config:getAsString("test.hub.url")
}
service<websub:Service> websubSubscriber bind websubEP {
    onNotification (websub:Notification notification) {
        json payload = check notification.getJsonPayload();
        io:println("WebSub Notification Received: " + payload.toString());
    }
}

@websub:SubscriberServiceConfig {
    path:"/websubTwo",
    subscribeOnStartUp:true,
    topic: "http://one.websub.topic.com",
    hub: config:getAsString("test.hub.url"),
    leaseSeconds: 3650,
    secret: "Kslk30SNF2AChs2"
}
service<websub:Service> websubSubscriberTwo bind websubEP {
    onIntentVerification (endpoint caller, websub:IntentVerificationRequest request) {
        http:Response response = request.buildSubscriptionVerificationResponse("http://one.websub.topic.com");
        if (response.statusCode == 202) {
            io:println("Intent verified explicitly for subscription change request");
        } else {
            io:println("Intent verification denied explicitly for subscription change request");
        }
        _ = caller->respond(untaint response);
    }

    onNotification (websub:Notification notification) {
        io:println("WebSub Notification Received by Two: " + check notification.getPayloadAsString());
    }
}

