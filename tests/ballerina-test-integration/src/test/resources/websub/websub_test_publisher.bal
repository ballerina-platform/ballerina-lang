import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

endpoint websub:Client websubHubClientEP {
    url: "https://localhost:9292/websub/hub"
};

function main(string... args) {
    io:println("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startUpBallerinaHub();
    //Register a topic at the hub
    _ = webSubHub.registerTopic("http://www.websubpubtopic.com");

    //Allow for subscriber service start up and subscription
    runtime:sleep(30000);

    io:println("Publishing update to internal Hub");
    //Publish to the internal Ballerina Hub directly
    _ = webSubHub.publishUpdate("http://www.websubpubtopic.com", {"action":"publish","mode":"internal-hub"});

    io:println("Publishing update to remote Hub");
    //Publish to the internal Ballerina Hub considering it as a remote hub
    _ = websubHubClientEP->publishUpdate("http://www.websubpubtopic.com",
                                                          {"action":"publish","mode":"remote-hub"});

    //Allow for notification by the Hub service
    runtime:sleep(5000);
}
