import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

public function main(string... args) {
    io:println("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startHub(9393)
                                    but { websub:HubStartedUpError hubStartedUpErr => hubStartedUpErr.startedUpHub };
    //Register a topic at the hub
    _ = webSubHub.registerTopic("http://www.websubpubtopic.com");

    //Allow for subscriber service start up and subscription
    runtime:sleep(30000);

    int index = 0;

    while (index < 5) {
        io:println("Publishing update to internal Hub");
        //Publish to the internal Ballerina Hub directly
        _ = webSubHub.publishUpdate("http://www.websubpubtopic.com", {"action":"publish","mode":"internal-hub"});
        runtime:sleep(5000);
        index++;
    }
}
