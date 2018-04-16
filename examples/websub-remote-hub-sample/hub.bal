// The Main program that brings up the Ballerina WebSub Hub.
import ballerina/log;
import ballerina/runtime;
import ballerina/websub;

function main (string [] args) {

    //Start up the internal Ballerina Hub.
    log:printInfo("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startUpBallerinaHub();

    //Wait for the subscriber to subscribe at this hub and for the publisher to publish the notifications.
    runtime:sleepCurrentWorker(60000);

}
