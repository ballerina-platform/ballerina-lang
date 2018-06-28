// The Ballerina WebSub Publisher, which registers a topic at the hub and publishes updates to the hub for the topic.
import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

// This is the remote WebSub Hub Endpoint to which registration and publish requests are sent.
endpoint websub:Client websubHubClientEP {
    url: "https://localhost:9191/websub/hub"
};

function main(string... args) {

    // Register a topic at the hub.
    var registrationResponse =
    websubHubClientEP->registerTopic("http://websubpubtopic.com");
    match (registrationResponse) {
        error webSubError => io:println("Error occurred registering topic: "
                + webSubError.message);
        () => io:println("Topic registration successful!");
    }

    // Make the publisher wait until the subscriber subscribes at the hub.
    runtime:sleep(10000);

    // Publish updates to the remote hub.
    io:println("Publishing update to remote Hub");
    var publishResponse =
    websubHubClientEP->publishUpdate("http://websubpubtopic.com",
        { "action": "publish", "mode": "remote-hub" });
    match (publishResponse) {
        error webSubError => io:println("Error notifying hub: "
                + webSubError.message);
        () => io:println("Update notification successful!");
    }

}
