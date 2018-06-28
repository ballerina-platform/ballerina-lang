// The Ballerina main program that demonstrates the usage of the Hub client endpoint to subscribe/unsubscribe to notifications.
import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

// This is the remote WebSub Hub Endpoint to which subscription and unsubscription requests are sent.
endpoint websub:Client websubHubClientEP {
    url: "https://localhost:9191/websub/hub"
};

function main(string... args) {

    // Send the subscription request for the subscriber service.
    websub:SubscriptionChangeRequest subscriptionRequest = {
        topic: "http://websubpubtopic.com",
        callback: "http://localhost:8181/websub",
        secret: "Kslk30SNF2AChs2"
    };

    var response = websubHubClientEP->subscribe(subscriptionRequest);

    match (response) {
        websub:SubscriptionChangeResponse subscriptionChangeResponse => {
            io:println("Subscription Request successful at Hub ["
                    + subscriptionChangeResponse.hub + "] for Topic ["
                    + subscriptionChangeResponse.topic + "]");
        }
        error e => {
            io:println("Error occurred with Subscription Request: ", e);
        }
    }

    // Wait for the initial notification, before unsubscribing.
    runtime:sleep(15000);

    // Send unsubscription request for the subscriber service.
    websub:SubscriptionChangeRequest unsubscriptionRequest = {
        topic: "http://websubpubtopic.com",
        callback: "http://localhost:8181/websub"
    };

    response = websubHubClientEP->unsubscribe(unsubscriptionRequest);

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
