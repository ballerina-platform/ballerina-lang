//Ballerina Websub Subscriber service which subscribes to notifications at a Hub.
import ballerina/log;
import ballerina/mime;
import ballerina/net.http;
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
    hub: "http://localhost:9999/websub/hub",
    leaseSeconds: 3600000,
    secret: "Kslk30SNF2AChs2"
}
service<websub:SubscriberService> websubSubscriber bind websubEP {

    //Resource accepting intent verification requests
    onVerifyIntent (endpoint client, http:Request request) {
        //Build the response for the subscription intent verification request received
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

    //Resource accepting content delivery requests
    onNotification (endpoint client, http:Request request) {
        http:Response response = { statusCode:202 };
        _ = client -> respond(response);
        var reqPayload = request.getJsonPayload();
        match (reqPayload) {
            json jsonPayload => { log:printInfo("WebSub Notification Received: " + jsonPayload.toString()); }
            mime:EntityError => { log:printInfo("Error occurred processing WebSub Notification"); }
        }
    }

}
