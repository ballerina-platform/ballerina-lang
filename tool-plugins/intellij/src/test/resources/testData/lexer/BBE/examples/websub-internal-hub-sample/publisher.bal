// The Ballerina WebSub Publisher brings up the internal Ballerina hub, registers a topic at the hub, and publishes updates to the topic.
import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

function main(string... args) {

    // Specify the port that the internal Ballerina hub needs to start on and start the hub.
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
    runtime:sleep(20000);

    // Publish directly to the internal Ballerina hub.
    io:println("Publishing update to internal Hub");
    var publishResponse = webSubHub.publishUpdate("http://websubpubtopic.com",
                            { "action": "publish", "mode": "internal-hub" });
    match (publishResponse) {
        error webSubError => io:println("Error notifying hub: "
                                        + webSubError.message);
        () => io:println("Update notification successful!");
    }

    // Make sure the service is running until the subscriber receives the update notification.
    runtime:sleep(5000);
}
