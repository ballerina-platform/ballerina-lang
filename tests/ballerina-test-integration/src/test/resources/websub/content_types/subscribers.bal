import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/websub;

endpoint websub:Listener websubEP {
    port:8686
};

@websub:SubscriberServiceConfig {
    path:"/websub",
    subscribeOnStartUp:true,
    hub: "https://localhost:9696/websub/hub",
    topic: "http://www.websubpubtopic.com",
    leaseSeconds: 3000,
    secret: "Kslk30SNF2AChs2"
}
service<websub:Service> websubSubscriber bind websubEP {
    onNotification (websub:Notification notification) {
        http:Request request = notification.request;
        if (request.getContentType() == mime:TEXT_PLAIN) {
            string payload = check request.getTextPayload();
            io:println("Text WebSub Notification Received by websubSubscriber: ", payload);
        } else if (request.getContentType() == mime:APPLICATION_XML) {
            xml payload = check request.getXmlPayload();
            io:println("XML WebSub Notification Received by websubSubscriber: ", payload);
        }
    }
}

@websub:SubscriberServiceConfig {
    path:"/websubTwo",
    subscribeOnStartUp:true,
    hub: "https://localhost:9696/websub/hub",
    topic: "http://www.websubpubtopic.com",
    leaseSeconds: 1000
}
service<websub:Service> websubSubscriberTwo bind websubEP {
    onNotification (websub:Notification notification) {
        http:Request request = notification.request;
        if (request.getContentType() == mime:TEXT_PLAIN) {
            string payload = check request.getTextPayload();
            io:println("Text WebSub Notification Received by websubSubscriberTwo: ", payload);
        } else if (request.getContentType() == mime:APPLICATION_XML) {
            xml payload = check request.getXmlPayload();
            io:println("XML WebSub Notification Received by websubSubscriberTwo: ", payload);
        }
    }
}
