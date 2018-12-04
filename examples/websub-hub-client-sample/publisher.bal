// The Ballerina WebSub Publisher brings up the internal Ballerina Hub,
// registers a topic at the hub, and publishes updates to the topic.
import ballerina/io;
import ballerina/runtime;
import ballerina/websub;
import ballerina/log;

public function main(string... args) {

    // Start up the internal Ballerina Hub.
    io:println("Starting up the Ballerina Hub Service");

    var result = websub:startHub(9191);
    websub:WebSubHub webSubHub = result is websub:HubStartedUpError ? result.startedUpHub : result;

    // Register a topic at the hub.
    var registrationResponse = webSubHub.registerTopic(
                                            "http://websubpubtopic.com");
    if (registrationResponse is error) {
        log:printError("Error occurred registering topic:", err = registrationResponse);
    } else {
        io:println("Topic registration successful!");
    }

    // Make the publisher wait until the subscriber subscribes at the hub.
    runtime:sleep(5000);

    // Publish directly to the internal Ballerina Hub.
    var publishResponse = webSubHub.publishUpdate("http://websubpubtopic.com",
                            { "action": "publish", "mode": "internal-hub" });
    if (publishResponse is error) {
        log:printError("Esrror notifying hub:", err = publishResponse);
    } else {
        io:println("Update notification successful!");
    }

    // Make the publisher wait until the subscriber unsubscribes at the hub.
    runtime:sleep(5000);

    // Publish directly to the internal Ballerina Hub.
    publishResponse = webSubHub.publishUpdate("http://websubpubtopic.com",
                            { "action": "publish", "mode": "internal-hub" });
    if (publishResponse is error) {
        log:printError("Error notifying hub:", err = publishResponse);
    } else {
        io:println("Update notification successful!");
    }

    // Make the publisher wait until notification is done to subscribers.
    runtime:sleep(2000);
}
