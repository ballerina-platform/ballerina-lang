import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/websub;

endpoint websub:Listener websubEP {
    port:8282
};

@websub:SubscriberServiceConfig {
    path:"/websub",
    subscribeOnStartUp:true,
    hub: "https://localhost:9191/websub/hub",
    topic: "http://one.websub.topic.com",
    leaseSeconds: 3000,
    secret: "Kslk30SNF2AChs2"
}
service<websub:Service> websubSubscriber bind websubEP {
    onNotification (websub:Notification notification) {
        if (notification.getContentType() == mime:TEXT_PLAIN) {
            string payload = check notification.getTextPayload();
            io:println("Text WebSub Notification Received by websubSubscriber: ", payload);
        } else if (notification.getContentType() == mime:APPLICATION_XML) {
            xml payload = check notification.getXmlPayload();
            io:println("XML WebSub Notification Received by websubSubscriber: ", payload);
        } else if (notification.getContentType() == mime:APPLICATION_JSON) {
            json payload = check notification.getJsonPayload();
            io:println("JSON WebSub Notification Received by websubSubscriber: ", payload);
        }
    }
}

@websub:SubscriberServiceConfig {
    path:"/websubTwo",
    subscribeOnStartUp:true,
    hub: "https://localhost:9191/websub/hub",
    topic: "http://one.websub.topic.com",
    leaseSeconds: 1000
}
service<websub:Service> websubSubscriberTwo bind websubEP {
    onNotification (websub:Notification notification) {
        if (notification.getContentType() == mime:TEXT_PLAIN) {
            string payload = check notification.getTextPayload();
            io:println("Text WebSub Notification Received by websubSubscriberTwo: ", payload);
        } else if (notification.getContentType() == mime:APPLICATION_XML) {
            xml payload = check notification.getXmlPayload();
            io:println("XML WebSub Notification Received by websubSubscriberTwo: ", payload);
        } else if (notification.getContentType() == mime:APPLICATION_JSON) {
            json payload = check notification.getJsonPayload();
            io:println("JSON WebSub Notification Received by websubSubscriberTwo: ", payload);
        }
    }
}
