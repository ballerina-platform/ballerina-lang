// The Ballerina WebSub Publisher, which registers a topic at the hub and publishes updates to the hub for the topic.
import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

// This is the remote [WebSub Hub client]((https://ballerina.io/swan-lake/learn/api-docs/ballerina/websub/clients/PublisherClient.html) to which registration and publish requests are sent.
websub:PublisherClient websubHubClientEP =
                    new ("http://localhost:9191/websub/publish");

public function main() {

    // Registers a topic at the hub using [registerTopic](https://ballerina.io/swan-lake/learn/api-docs/ballerina/websub/clients/PublisherClient.html#registerTopic).
    var registrationResponse =
                websubHubClientEP->registerTopic("http://websubpubtopic.com");
    if (registrationResponse is error) {
        io:println("Error occurred registering topic: " +
                                registrationResponse.message());
    } else {
        io:println("Topic registration successful!");
    }

    // Makes the publisher wait until the subscriber subscribes at the hub.
    runtime:sleep(5000);

    // Publishes updates to the remote hub using [publishUpdate](https://ballerina.io/swan-lake/learn/api-docs/ballerina/websub/clients/PublisherClient.html#publishUpdate).
    io:println("Publishing update to remote Hub");
    var publishResponse =
        websubHubClientEP->publishUpdate("http://websubpubtopic.com",
                                {"action": "publish", "mode": "remote-hub"});
    if (publishResponse is error) {
        io:println("Error notifying hub: " +
                                    publishResponse.message());
    } else {
        io:println("Update notification successful!");
    }

}
