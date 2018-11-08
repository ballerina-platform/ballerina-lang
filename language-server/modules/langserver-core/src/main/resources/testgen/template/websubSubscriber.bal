import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

@test:Config
function ${testWebsubFunctionName}() {
    // Start up the internal Ballerina Hub.
    io:println("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startHub(9191) but {
        websub:HubStartedUpError hubStartedUpErr => hubStartedUpErr.startedUpHub
    };

    // Register a topic at the hub.
    var registrationResponse = webSubHub.registerTopic("http://websubpubtopic.com");
    match (registrationResponse) {
        error webSubError => io:println("Error occurred registering topic: " + webSubError.message);
        () => io:println("Topic registration successful!");
    }

    // Make the publisher wait until the subscriber subscribes at the hub.
    runtime:sleep(5000);

    // Publish directly to the internal Ballerina Hub.
    var publishResponse = webSubHub.publishUpdate("http://websubpubtopic.com",
    { "action": "publish", "mode": "internal-hub" });
    match (publishResponse) {
        error webSubError => io:println("Error notifying hub: " + webSubError.message);
        () => io:println("Update notification successful!");
    }
}