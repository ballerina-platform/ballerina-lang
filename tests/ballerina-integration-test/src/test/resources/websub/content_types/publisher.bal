import ballerina/config;
import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

endpoint websub:Client websubHubClientEP {
    url: "https://localhost:9696/websub/hub"
};

function main(string... args) {
    io:println("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = check websub:startUpBallerinaHub(9696);
    //Register a topic at the hub
    _ = webSubHub.registerTopic("http://www.websubpubtopic.com");

    //Allow for subscriber service start up and subscription
    runtime:sleep(30000);

    xml xmlInternalPayload = xml `<websub><request>Notification</request><type>Internal</type></websub>`;
    xml xmlRemotePayload = xml `<websub><request>Notification</request><type>Remote</type></websub>`;

    io:println("Publishing XML update to internal Hub");
    //Publish to the internal Ballerina Hub directly
    _ = webSubHub.publishUpdate("http://www.websubpubtopic.com", xmlInternalPayload);

    io:println("Publishing XML update to remote Hub");
    //Publish to the internal Ballerina Hub considering it as a remote hub
    _ = websubHubClientEP->publishUpdate("http://www.websubpubtopic.com", xmlRemotePayload);

    string stringInternalPayload = "Text update for internal Hub";
    string stringRemotePayload = "Text update for remote Hub";

    io:println("Publishing Text update to internal Hub");
    //Publish to the internal Ballerina Hub directly
    _ = webSubHub.publishUpdate("http://www.websubpubtopic.com", stringInternalPayload);

    io:println("Publishing Text to remote Hub");
    //Publish to the internal Ballerina Hub considering it as a remote hub
    _ = websubHubClientEP->publishUpdate("http://www.websubpubtopic.com", stringRemotePayload);

    //Allow for notification by the Hub service
    runtime:sleep(5000);
}
