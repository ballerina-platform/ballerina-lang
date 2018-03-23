import ballerina/log;
import ballerina/mime;
import ballerina/net.http;
import ballerina/net.websub;

endpoint websub:SubscriberServiceEndpoint websubEP {
    host:"localhost",
    port:8181
};

@websub:SubscriberServiceConfig {
    basePath:"/websub",
    subscribeOnStartUp:true,
    topic: "http://www.websubpubtopic.com",
    hub: "http://localhost:9999/websub/hub",
    leaseSeconds: 3600000,
    secret: "Kslk30SNF2AChs2"
}
service<websub:SubscriberService> websubSubscriber bind websubEP {

    onVerifyIntent (endpoint client, http:Request request) {
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
