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
    topic: "http://www.websubpubtopic.com",
    hub: "https://localhost:9292/websub/hub",
    leaseSeconds: 3600000,
    secret: "Kslk30SNF2AChs2"
}
service<websub:Service> websubSubscriber bind websubEP {

    onIntentVerification (endpoint caller, websub:IntentVerificationRequest request) {
        http:Response response = request.buildSubscriptionVerificationResponse();
        if (response.statusCode == 202) {
            io:println("Intent verified for subscription request");
        } else {
            io:println("Intent verification for subscription request denied");
        }
        _ = caller->respond(response);
    }

    onNotification (websub:Notification notification) {
        io:println("WebSub Notification Received: " + notification.payload.toString());
    }

}

