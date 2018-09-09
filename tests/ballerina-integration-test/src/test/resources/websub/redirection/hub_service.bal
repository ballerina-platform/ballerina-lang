import ballerina/config;
import ballerina/http;
import ballerina/io;
import ballerina/runtime;
import ballerina/time;
import ballerina/websub;

boolean testSubscriberRegistered;

public function main(string... args) {
    io:println("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startHub(9595)
                                    but { websub:HubStartedUpError hubStartedUpErr => hubStartedUpErr.startedUpHub };
    //Register a topic at the hub
    _ = webSubHub.registerTopic("http://redirectiontopicone.com");
    //Register a topic at the hub
    _ = webSubHub.registerTopic("http://redirectiontopictwo.com");

    int startTime = time:currentTime().time;

    while (!testSubscriberRegistered && time:currentTime().time - startTime < 15000) {
        runtime:sleep(1000);
    }
    testSubscriberRegistered = false;
}

service<http:Service> helper bind { port: config:getAsInt("test.helper.service.port") } {
    subscribed(endpoint caller, http:Request req) {
        testSubscriberRegistered = true;
        _ = caller->respond("Subscription notified!");
    }
}
