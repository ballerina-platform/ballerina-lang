import ballerina/config;
import ballerina/http;
import ballerina/io;
import ballerina/runtime;
import ballerina/time;
import ballerina/websub;

boolean testSubscriberRegistered;
boolean testContentDeliveryDone;

endpoint websub:Client websubHubClientEP {
    url: config:getAsString("test.hub.url")
};

public function main(string... args) {
    io:println("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startHub(9292)
                                    but { websub:HubStartedUpError hubStartedUpErr => hubStartedUpErr.startedUpHub };
    //Register a topic at the hub
    _ = webSubHub.registerTopic("http://www.websubpubtopic.com");
    //Register topic to test remote registration and rejection of intent verification for invalid topic
    _ = websubHubClientEP->registerTopic("http://websubpubtopictwo.com");

    int startTime = time:currentTime().time;

    while (!testSubscriberRegistered && time:currentTime().time - startTime < 15000) {
        runtime:sleep(1000);
    }
    testSubscriberRegistered = false;

    io:println("Publishing update to internal Hub");
    //Publish to the internal Ballerina Hub directly
    _ = webSubHub.publishUpdate("http://www.websubpubtopic.com", {"action":"publish","mode":"internal-hub"});

    io:println("Publishing update to internal Hub");
    //Publish to the internal Ballerina Hub directly
    _ = webSubHub.publishUpdate("http://websubpubtopictwo.com", {"action":"publish","mode":"internal-hub-two"});

    io:println("Publishing update to remote Hub");
    //Publish to the internal Ballerina Hub considering it as a remote hub
    _ = websubHubClientEP->publishUpdate("http://www.websubpubtopic.com",
                                                          {"action":"publish","mode":"remote-hub"});

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
