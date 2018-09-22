import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/websub;

endpoint websub:Listener websubEP {
    port:8383
};

@websub:SubscriberServiceConfig {
    path:"/websub",
    subscribeOnStartUp:true,
    topic: "http://three.websub.topic.com",
    hub: "https://localhost:9191/websub/hub",
    resourceUrl: "https://localhost:8080/publish", //invalid resource URL to test hub/topic prioritization
    leaseSeconds: 3600,
    secret: "Kslk30SNF2AChs2"
}
service<websub:Service> websubSubscriber bind websubEP {
    onNotification (websub:Notification notification) {
        json payload = check notification.getJsonPayload();
        io:println("WebSub Notification Received by One: " + payload.toString());
    }
}

@websub:SubscriberServiceConfig {
    path:"/websubTwo",
    subscribeOnStartUp:true,
    resourceUrl: "http://localhost:8080/publisherTwo/discover",
    leaseSeconds: 1200,
    secret: "SwklSSf42DLA"
}
service<websub:Service> websubSubscriberTwo bind websubEP {
    onNotification (websub:Notification notification) {
        json payload = check notification.getJsonPayload();
        io:println("WebSub Notification Received by Two: " + payload.toString());
    }
}
