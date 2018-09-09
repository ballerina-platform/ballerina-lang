import ballerina/config;
import ballerina/http;
import ballerina/io;
import ballerina/runtime;
import ballerina/time;
import ballerina/websub;

boolean testSubscriberRegistered;
boolean testContentDeliveryDone;

public function main(string... args) {
    io:println("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startHub(9393)
                                    but { websub:HubStartedUpError hubStartedUpErr => hubStartedUpErr.startedUpHub };
    //Register a topic at the hub
    _ = webSubHub.registerTopic("http://www.websubpubtopic.com");

    int startTime = time:currentTime().time;

    while (!testSubscriberRegistered && time:currentTime().time - startTime < 15000) {
        runtime:sleep(1000);
    }
    testSubscriberRegistered = false;

    int index = 0;

    while (index < 5 && !testContentDeliveryDone) {
        io:println("Publishing update to internal Hub");
        //Publish to the internal Ballerina Hub directly
        _ = webSubHub.publishUpdate("http://www.websubpubtopic.com", {"action":"publish","mode":"internal-hub"});
        runtime:sleep(5000);
        index++;
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
