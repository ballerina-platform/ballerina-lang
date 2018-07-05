import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

// This is the remote WebSub Hub Endpoint to which subscription and unsubscription requests are sent.
endpoint websub:Client websubHubClientEP {
    url: "https://localhost:9393/websub/hub"
};

function main(string... args) {

    // Send unsubscription request for the subscriber service.
    websub:SubscriptionChangeRequest unsubscriptionRequest = {
        topic: "http://www.websubpubtopic.com",
        callback: "http://localhost:8181/websub"
    };

    var response = websubHubClientEP->unsubscribe(unsubscriptionRequest);

    match (response) {
        websub:SubscriptionChangeResponse subscriptionChangeResponse => {
            io:println("Unsubscription Request successful at Hub ["
                    + subscriptionChangeResponse.hub
                    + "] for Topic [" + subscriptionChangeResponse.topic + "]");
        }
        error e => {
            io:println("Error occurred with Unsubscription Request: ", e);
        }
    }

    // Confirm unsubscription - no notifications should be received.
    runtime:sleep(5000);
}
