import ballerina.net.http;
import ballerina.io;
import ballerina.runtime;

function main (string [] args) {
    io:println("Starting up the Ballerina Hub Service");
    http:WebSubHub webSubHub = http:startUpBallerinaHub();
    //Allow for subscription
    runtime:sleepCurrentWorker(10000);

    io:println("Publishing updates to Hubs");
    //Publish to the internal Ballerina Hub directly
    _ = webSubHub.publishUpdate("http://www.websubpubtopic.com", {"action":"publish","mode":"ballerina-hub"});

    //Publish to the internal Ballerina Hub, as a remote Hub by specifying the URL - a POST request is sent
    webSubHub = { hubUrl:"http://localhost:9999/websub/hub" };
    _ = webSubHub.publishUpdateToRemoteHub("http://www.websubpubtopic.com", {"action":"publish","mode":"remote-hub"});

    //Allow for notification by the Hub service
    runtime:sleepCurrentWorker(5000);
}
