import ballerina/config;
import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

function main(string... args) {
    io:println("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startHub(9595)
                                    but { websub:HubStartedUpError hubStartedUpErr => hubStartedUpErr.startedUpHub };
    //Register a topic at the hub
    _ = webSubHub.registerTopic("http://redirectiontopicone.com");
    //Register a topic at the hub
    _ = webSubHub.registerTopic("http://redirectiontopictwo.com");

    //Allow for subscriber service start up and subscription
    runtime:sleep(45000);
}
