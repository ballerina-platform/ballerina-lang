// The Ballerina WebSub Publisher, which registers a topic at the hub and publishes updates to the hub for the topic.
import ballerina/io;
import ballerina/runtime;
import ballerina/websub;
import ballerina/log;

// This is the remote WebSub Hub Endpoint to which registration and publish requests are sent.
websub:Client websubHubClientEP = new websub:Client("https://localhost:9191/websub/hub");


public function main(string... args) {

    // Register a topic at the hub.
    var registrationResponse =
                websubHubClientEP->registerTopic("http://websubpubtopic.com");
    if (registrationResponse is error) {
        log:printError("Error occurred registering topic:", err = registrationResponse);
    } else {
        io:println("Topic registration successful!");
    }

    // Make the publisher wait until the subscriber subscribes at the hub.
    runtime:sleep(5000);

    // Publish updates to the remote hub.
    io:println("Publishing update to remote Hub");
    var publishResponse =
        websubHubClientEP->publishUpdate("http://websubpubtopic.com",
                                { "action": "publish", "mode": "remote-hub" });
    if (publishResponse is error) {
        log:printError("Error notifying hub:", err = publishResponse);
    } else {
        io:println("Update notification successful!");
    }

}
