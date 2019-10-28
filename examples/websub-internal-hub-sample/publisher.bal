// The Ballerina WebSub Publisher brings up the internal Ballerina hub, registers a topic at the hub, and publishes updates to the topic.
import ballerina/io;
import ballerina/http;
import ballerina/runtime;
import ballerina/websub;

public function main() {

    // Specifies the port that the internal Ballerina hub needs to start on and start the hub.
    io:println("Starting up the Ballerina Hub Service");

    var result = websub:startHub(new http:Listener(9191), "/websub", "/hub");
    websub:Hub webSubHub = result is websub:HubStartedUpError ?
                                                result.startedUpHub : result;

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

    // Publishes directly to the internal Ballerina hub.
    io:println("Publishing update to internal Hub");
    var publishResponse = webSubHub.publishUpdate("http://websubpubtopic.com",
        { "action": "publish", "mode": "internal-hub" });

    if (publishResponse is error) {
        io:println("Error notifying hub: " +
                                <string>publishResponse.detail()?.message);
    } else {
        io:println("Update notification successful!");
    }

    // Keeps the service is running until the subscriber receives the update notification.
    runtime:sleep(2000);
}
