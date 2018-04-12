//The Ballerina WebSub Publisher which registers a topic at the hub and publishes updates to the hub for the topic
import ballerina/log;
import ballerina/runtime;
import ballerina/websub;

//This is the WebSub Hub Client Endpoint to send registration and subscription/unsubscription requests to a remote hub
endpoint websub:Client websubHubClientEP {
    url: "https://localhost:9292/websub/hub"
};

function main (string [] args) {

    //Register a topic at the hub
    var registrationResponse = websubHubClientEP -> registerTopic("http://www.websubpubtopic.com");
    match (registrationResponse) {
        websub:WebSubError webSubError => log:printError("Error occurred registering topic: " + webSubError.message);
        () => log:printInfo("Topic registration successful!");
    }

    //Allow for subscription
    runtime:sleepCurrentWorker(10000);

    log:printInfo("Publishing update to remote Hub");
    //Publish to the remote hub
    var publishResponse = websubHubClientEP -> publishUpdate("http://www.websubpubtopic.com",
                                                                            {"action":"publish","mode":"remote-hub"});
    match (publishResponse) {
        websub:WebSubError webSubError => log:printError("Error notifying hub: " + webSubError.message);
        () => log:printInfo("Update notification successful!");
    }

}
