// The Ballerina WebSub Publisher brings up the internal Ballerina Hub,
// registers a topic at the hub, and publishes updates to the topic.
import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

function main(string... args) {

    // Start up the internal Ballerina Hub.
    io:println("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startUpBallerinaHub(port = 9191) but {
        websub:HubStartedUpError hubStartedUpErr => hubStartedUpErr.startedUpHub
    };

    // Register a topic at the hub.
    var registrationResponse = webSubHub.registerTopic(
                                            "http://websubpubtopic.com");
    match (registrationResponse) {
        error webSubError => io:println("Error occurred registering topic: "
                + webSubError.message);
        () => io:println("Topic registration successful!");
    }

    // Make the publisher wait until the subscriber subscribes at the hub.
    runtime:sleep(15000);

    // Publish directly to the internal Ballerina Hub.
    var publishResponse = webSubHub.publishUpdate("http://websubpubtopic.com",
        { "action": "publish", "mode": "internal-hub" });
    match (publishResponse) {
        error webSubError => io:println("Error notifying hub: "
                + webSubError.message);
        () => io:println("Update notification successful!");
    }

    // Make the publisher wait until the subscriber unsubscribes at the hub.
    runtime:sleep(15000);

    // Publish directly to the internal Ballerina Hub.
    publishResponse = webSubHub.publishUpdate("http://websubpubtopic.com",
        { "action": "publish", "mode": "internal-hub" });
    match (publishResponse) {
        error webSubError => io:println("Error notifying hub: "
                + webSubError.message);
        () => io:println("Update notification successful!");
    }

    // Make the publisher wait until notification is done to subscribers.
    runtime:sleep(5000);
}
