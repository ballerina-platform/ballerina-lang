import ballerina/io;
import ballerina/net.websub;
import ballerina/runtime;

endpoint websub:HubClientEndpoint websubHubClientEP {
    url: "https://localhost:9999/websub/hub"
};

function main (string [] args) {
    io:println("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startUpBallerinaHub();
    //Allow for subscriber service start up and subscription
    runtime:sleepCurrentWorker(30000);

    io:println("Publishing update to internal Hub");
    //Publish to the internal Ballerina Hub directly
    _ = webSubHub.publishUpdate("http://www.websubpubtopic.com", {"action":"publish","mode":"internal-hub"});

    io:println("Publishing update to remote Hub");
    //Publish to the internal Ballerina Hub considering it as a remote hub
    _ = websubHubClientEP -> publishUpdateToRemoteHub("http://www.websubpubtopic.com",
                                                          {"action":"publish","mode":"remote-hub"});

    //Allow for notification by the Hub service
    runtime:sleepCurrentWorker(5000);
}
