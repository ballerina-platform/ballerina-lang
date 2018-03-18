import ballerina.io;
import ballerina.net.http;

endpoint<http:WebSubSubscriberService> websubEP {
    host:"localhost",
    port:8181
}

@http:webSubSubscriberServiceConfig {
    basePath:"/websub",
    endpoints:[websubEP],
    subscribeOnStartUp:true,
    topic: "http://www.websubpubtopic.com",
    hub: "http://localhost:9999/websub/hub",
    leaseSeconds: 3600000,
    secret: "Kslk30SNF2AChs2"
}
service<http:WebSubSubscriberService> websubSubscriber {

    resource onVerifyIntent (http:ServerConnector conn, http:Request request) {
        http:Response response = {};
        response = http:buildSubscriptionVerificationResponse(request);
        io:println("Intent verified for subscription request");
        _ = conn -> respond(response);
    }

    resource onNotification (http:ServerConnector conn, http:Request request) {
        http:Response response = { statusCode:202 };
        _ = conn -> respond(response);
        http:WebSubNotification webSubNotification;
        http:WebSubError webSubError;
        webSubNotification, webSubError = http:processWebSubNotification(request);
        if (webSubError == null) {
            io:println("WebSub Notification Received: " + webSubNotification.payload.toString());
        } else {
            io:println("Error occurred processing WebSub Notification: " + webSubError.errorMessage);
        }
    }

}

