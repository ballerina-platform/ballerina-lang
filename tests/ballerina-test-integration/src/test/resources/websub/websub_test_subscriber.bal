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
    topic: "http://www.websubpubtopic.com",
    hub: config:getAsString("test.hub.url")
}
service<websub:Service> websubSubscriber bind websubEP {
    onNotification (websub:Notification notification) {
        json payload = check notification.getJsonPayload();
        io:println("WebSub Notification Received: " + payload.toString());
    }
}

