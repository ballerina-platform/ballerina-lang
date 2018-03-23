//Ballerina Websub Publisher which brings up the internal Ballerina Hub and publishes updates to the hub
import ballerina/log;
import ballerina/net.websub;
import ballerina/runtime;

//The WebSub Hub Client Endpoint to send subscription/unsubscription requests to a remote hub
endpoint websub:HubClientEndpoint websubHubClientEP {
    uri: "http://localhost:9999/websub/hub"
};

function main (string [] args) {
    //Start up the internal Ballerina Hub
    log:printInfo("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startUpBallerinaHub();

    //Allow for subscription
    runtime:sleepCurrentWorker(20000);

    log:printInfo("Publishing update to internal Hub");
    //Publish to the internal Ballerina Hub directly
    _ = webSubHub.publishUpdate("http://www.websubpubtopic.com", {"action":"publish","mode":"internal-hub"});

    log:printInfo("Publishing update to remote Hub");
    //Publish to the internal Ballerina Hub considering it as a remote hub
    _ = websubHubClientEP -> publishUpdateToRemoteHub("http://www.websubpubtopic.com",
                                                      {"action":"publish","mode":"remote-hub"});

    //Allow for notification by the Hub service
    runtime:sleepCurrentWorker(50000);
}
