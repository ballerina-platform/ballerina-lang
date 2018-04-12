//The Ballerina WebSub Publisher brings up the internal Ballerina Hub and registers a topic at the hub and publishes
//updates to the hub for the topic
import ballerina/log;
import ballerina/runtime;
import ballerina/websub;

function main (string [] args) {
    //Start up the internal Ballerina Hub
    log:printInfo("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startUpBallerinaHub();

    //Register a topic at the hub
    var registrationResponse = webSubHub.registerTopic("http://www.websubpubtopic.com");
    match (registrationResponse) {
        websub:WebSubError webSubError => log:printError("Error occurred registering topic: " + webSubError.message);
        () => log:printInfo("Topic registration successful!");
    }

    //Allow for subscription
    runtime:sleepCurrentWorker(20000);

    log:printInfo("Publishing update to internal Hub");
    //Publish directly to the internal Ballerina Hub
    var publishResponse = webSubHub.publishUpdate("http://www.websubpubtopic.com",
                                                                        {"action":"publish","mode":"internal-hub"});
    match (publishResponse) {
        websub:WebSubError webSubError => log:printError("Error notifying hub: " + webSubError.message);
        () => log:printInfo("Update notification successful!");
    }

    //Allow for notification by the Hub service
    runtime:sleepCurrentWorker(50000);
}
