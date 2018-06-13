import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/websub;

endpoint websub:Listener websubEP {
    port:8484
};

@websub:SubscriberServiceConfig {
    path:"/websub",
    subscribeOnStartUp:true,
    topic: "http://www.websubpubtopic.com",
    hub: "https://localhost:9494/websub/hub",
    resourceUrl: "https://localhost:9290/publisherService/discover",
    leaseSeconds: 3600,
    secret: "Kslk30SNF2AChs2"
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
    resourceUrl: "http://localhost:9290/publisherService/discover",
    leaseSeconds: 1200,
    secret: "SwklSSf42DLA"
}
service<websub:Service> websubSubscriberTwo bind websubEP {
    onNotification (websub:Notification notification) {
        json payload = check notification.getJsonPayload();
        io:println("WebSub Notification Received: " + payload.toString());
    }
}
