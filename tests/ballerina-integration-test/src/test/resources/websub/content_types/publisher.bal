import ballerina/config;
import ballerina/http;
import ballerina/io;
import ballerina/runtime;
import ballerina/time;
import ballerina/websub;

boolean testSubscriberRegistered;
boolean testContentDeliveryDone;

endpoint websub:Client websubHubClientEP {
    url: "https://localhost:9696/websub/hub"
};

public function main(string... args) {
    io:println("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = check websub:startHub(9696);
    //Register a topic at the hub
    _ = webSubHub.registerTopic("http://www.websubpubtopic.com");

    int startTime = time:currentTime().time;

    while (!testSubscriberRegistered && time:currentTime().time - startTime < 15000) {
        runtime:sleep(1000);
    }
    testSubscriberRegistered = false;

    xml xmlInternalPayload = xml `<websub><request>Notification</request><type>Internal</type></websub>`;
    xml xmlRemotePayload = xml `<websub><request>Notification</request><type>Remote</type></websub>`;

    io:println("Publishing XML update to internal Hub");
    //Publish to the internal Ballerina Hub directly
    _ = webSubHub.publishUpdate("http://www.websubpubtopic.com", xmlInternalPayload);

    io:println("Publishing XML update to remote Hub");
    //Publish to the internal Ballerina Hub considering it as a remote hub
    _ = websubHubClientEP->publishUpdate("http://www.websubpubtopic.com", xmlRemotePayload);

    string stringInternalPayload = "Text update for internal Hub";
    string stringRemotePayload = "Text update for remote Hub";

    io:println("Publishing Text update to internal Hub");
    //Publish to the internal Ballerina Hub directly
    _ = webSubHub.publishUpdate("http://www.websubpubtopic.com", stringInternalPayload);

    io:println("Publishing Text to remote Hub");
    //Publish to the internal Ballerina Hub considering it as a remote hub
    _ = websubHubClientEP->publishUpdate("http://www.websubpubtopic.com", stringRemotePayload);

    startTime = time:currentTime().time;

    while (!testContentDeliveryDone && time:currentTime().time - startTime < 5000) {
        runtime:sleep(1000);
    }
    testContentDeliveryDone = false;
}

service<http:Service> helper bind { port: config:getAsInt("test.helper.service.port") } {
    subscribed(endpoint caller, http:Request req) {
        testSubscriberRegistered = true;
        _ = caller->respond("Subscription notified!");
    }

    delivered(endpoint caller, http:Request req) {
        testContentDeliveryDone = true;
        _ = caller->respond("Delivery notified!");
    }
}
