import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/websub;

endpoint websub:Listener websubEP {
    port:8585
};

@websub:SubscriberServiceConfig {
    path:"/websub",
    subscribeOnStartUp:true,
    resourceUrl: "http://localhost:9291/original/one",
    leaseSeconds: 3600,
    secret: "Kslk30SNF2AChs2",
    followRedirects: {
        enabled: true
    }
}
service<websub:Service> websubSubscriber bind websubEP {
    onNotification (websub:Notification notification) {
        io:println("WebSub Notification Received: " + notification.payload.toString());
    }
}

@websub:SubscriberServiceConfig {
    path:"/websubTwo",
    subscribeOnStartUp:true,
    resourceUrl: "http://localhost:9291/original/two",
    leaseSeconds: 1200,
    secret: "SwklSSf42DLA",
    followRedirects: {
        enabled: true
    }
}
service<websub:Service> websubSubscriberTwo bind websubEP {
    onNotification (websub:Notification notification) {
        io:println("WebSub Notification Received: " + notification.payload.toString());
    }
}
