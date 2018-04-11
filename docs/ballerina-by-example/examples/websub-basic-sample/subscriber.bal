//Ballerina WebSub Subscriber service which subscribes to notifications at a Hub.
import ballerina/log;
import ballerina/mime;
import ballerina/http;
import ballerina/websub;

//The endpoint to which the subscriber service is bound
endpoint websub:Listener websubEP {
    port:8181
};

//Annotations specifying the subscription parameters
@websub:SubscriberServiceConfig {
    basePath:"/websub",
    subscribeOnStartUp:true,
    topic: "http://www.websubpubtopic.com",
    hub: "https://localhost:9292/websub/hub",
    leaseSeconds: 3600000,
    secret: "Kslk30SNF2AChs2"
}
service websubSubscriber bind websubEP {

    //Resource accepting intent verification requests
    onIntentVerification (endpoint client, websub:IntentVerificationRequest request) {
        //Build the response for the subscription intent verification request received
        http:Response response = new;
        match (request.buildSubscriptionVerificationResponse()) {
            http:Response httpResponse => {
                log:printInfo("Intent verified for subscription request");
                response = httpResponse;
            }
            () => {
                log:printWarn("Intent verification for subscription request denied");
                response.statusCode = 404;
            }
        }
        _ = client -> respond(response);
    }

    //Resource accepting content delivery requests
    onNotification (websub:NotificationRequest notification) {
        json notificationPayload = notification.payload;
        string notificationString = notificationPayload.toString() but {() => ""};
        log:printInfo("WebSub Notification Received: " + notificationString);
    }

}
