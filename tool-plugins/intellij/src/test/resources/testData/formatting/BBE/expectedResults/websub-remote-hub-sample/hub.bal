// The Main program that brings up the Ballerina WebSub Hub.
import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

function main(string... args) {

    // Start up the internal Ballerina Hub.
    io:println("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub =
    websub:startUpBallerinaHub(port = 9191, remotePublishingEnabled = true) but {
        websub:HubStartedUpError hubStartedUpErr => hubStartedUpErr.startedUpHub
    };

    // Wait for the subscriber to subscribe at this hub and for the publisher to publish the notifications.
    runtime:sleep(60000);

}
