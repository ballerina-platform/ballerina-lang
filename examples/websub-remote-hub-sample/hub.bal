//The Main program bringing up a Ballerina WebSub Hub
import ballerina/log;
import ballerina/runtime;
import ballerina/websub;

function main (string [] args) {

    //Start up the internal Ballerina Hub
    log:printInfo("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startUpBallerinaHub();

    //Allow for subscription and notification
    runtime:sleepCurrentWorker(60000);

}
