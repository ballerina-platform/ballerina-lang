// The Ballerina WebSub Publisher brings up the internal Ballerina Hub,
// registers a topic at the hub, and publishes updates to the topic.
import ballerina/io;
import ballerina/http;
import ballerina/runtime;
import ballerina/websub;

public function main() {

    // Starts the internal Ballerina Hub.
    io:println("Starting up the Ballerina Hub Service");
    websub:Hub webSubHub;
    var result = websub:startHub(new http:Listener(9191), "/websub", "/hub");
    if (result is websub:Hub) {
        webSubHub = result;
    } else if (result is websub:HubStartedUpError) {
        webSubHub = result.startedUpHub;
    } else {
        io:println("Hub start error:" + <string> result.detail()?.message);
        return;
    }
    // Registers a topic at the hub.
    var registrationResponse = webSubHub.registerTopic(
                                            "http://websubpubtopic.com");
    if (registrationResponse is error) {
        io:println("Error occurred registering topic: " +
                                <string>registrationResponse.detail()?.message);
    } else {
        io:println("Topic registration successful!");
    }

    // Makes the publisher wait until the subscriber subscribes at the hub.
    runtime:sleep(5000);

    // Publishes directly to the internal Ballerina Hub.
    var publishResponse = webSubHub.publishUpdate("http://websubpubtopic.com",
                            { "action": "publish", "mode": "internal-hub" });
    if (publishResponse is error) {
        io:println("Error notifying hub: " +
                                    <string>publishResponse.detail()?.message);
    } else {
        io:println("Update notification successful!");
    }

    // Makes the publisher wait until the subscriber unsubscribes at the hub.
    runtime:sleep(5000);

    // Publishes directly to the internal Ballerina Hub.
    publishResponse = webSubHub.publishUpdate("http://websubpubtopic.com",
                            { "action": "publish", "mode": "internal-hub" });
    if (publishResponse is error) {
        io:println("Error notifying hub: " +
                                    <string>publishResponse.detail()?.message);
    } else {
        io:println("Update notification successful!");
    }

    // Makes the publisher wait until subscribers are notified.
    runtime:sleep(2000);
}
