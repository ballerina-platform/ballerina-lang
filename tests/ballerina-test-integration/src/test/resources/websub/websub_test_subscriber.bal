import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/net.websub;

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

    onVerifyIntent (endpoint client, http:Request request) {
        var subscriptionVerificationResponse = websub:buildSubscriptionVerificationResponse(request);
        http:Response response = {};
        match (subscriptionVerificationResponse) {
            http:Response httpResponse => {
                io:println("Intent verified for subscription request");
                response = httpResponse;
            }
            (any | null) => {
                io:println("Intent verification for subscription request denied");
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
            json jsonPayload => { io:println("WebSub Notification Received: " + jsonPayload.toString()); }
            http:PayloadError => { io:println("Error occurred processing WebSub Notification"); }
        }
    }

}

