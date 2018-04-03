//Ballerina WebSub Subscriber service that subscribes to notifications at a Hub
import ballerina/log;
import ballerina/mime;
import ballerina/http;
import ballerina/net.websub;

//The endpoint to which the subscriber service is bound
endpoint websub:SubscriberServiceEndpoint websubEP {
    host:"localhost",
    port:8181
};

//Annotations specifying the subscription parameters
@websub:SubscriberServiceConfig {
    basePath:"/websub",
    subscribeOnStartUp:true,
    topic: "http://www.websubpubtopic.com",
    hub: "https://localhost:9999/websub/hub",
    leaseSeconds: 3600000,
    secret: "Kslk30SNF2AChs2"
}
service<websub:SubscriberService> websubSubscriber bind websubEP {

    //Method that accepts intent verification requests
    onVerifyIntent (endpoint client, http:Request request) {
        //Build the response for the subscription intent verification request that was received
        var subscriptionVerificationResponse = websub:buildSubscriptionVerificationResponse(request);
        http:Response response = {};
        match (subscriptionVerificationResponse) {
            http:Response httpResponse => {
                log:printInfo("Intent verified for subscription request");
                response = httpResponse;
            }
            null => {
                log:printInfo("Intent verification for subscription request denied");
                response = { statusCode:404 };
            }
        }
        _ = client -> respond(response);
    }

    //Method that accepts content delivery requests
    onNotification (endpoint client, http:Request request) {
        http:Response response = { statusCode:202 };
        _ = client -> respond(response);
        var reqPayload = request.getJsonPayload();
        match (reqPayload) {
            json jsonPayload => { log:printInfo("WebSub Notification Received: " + jsonPayload.toString()); }
            http:PayloadError error => { log:printInfo("Error occurred processing WebSub Notification"); }
        }
    }

}
