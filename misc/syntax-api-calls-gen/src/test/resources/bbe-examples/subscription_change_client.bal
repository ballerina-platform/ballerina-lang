// The Ballerina main program, which demonstrates the usage of the [Hub client](https://ballerina.io/swan-lake/learn/api-docs/ballerina/websub/clients/SubscriptionClient.html) to subscribe/unsubscribe to notifications.
import ballerina/io;
import ballerina/runtime;
import ballerina/websub;

websub:SubscriptionClient websubHubClientEP =
                            new ("http://localhost:9191/websub/hub");

public function main() {

    // Sends the subscription request for the subscriber service.
    websub:SubscriptionChangeRequest subscriptionRequest = {
        topic: "http://websubpubtopic.com",
        callback: "http://localhost:8181/websub",
        secret: "Kslk30SNF2AChs2"
    };

    var response = websubHubClientEP->subscribe(subscriptionRequest);

    if (response is websub:SubscriptionChangeResponse) {
        io:println("Subscription Request successful at Hub [" + response.hub +
                    "] for Topic [" + response.topic + "]");
    } else {
        io:println("Error occurred with Subscription Request: " +
                    response.message());
    }

    // Waits for the initial notification before unsubscribing.
    runtime:sleep(5000);

    // Sends the unsubscription request to the subscriber service.
    websub:SubscriptionChangeRequest unsubscriptionRequest = {
        topic: "http://websubpubtopic.com",
        callback: "http://localhost:8181/websub"
    };

    response = websubHubClientEP->unsubscribe(unsubscriptionRequest);

    if (response is websub:SubscriptionChangeResponse) {
        io:println("Unsubscription Request successful at Hub [" + response.hub +
                    "] for Topic [" + response.topic + "]");
    } else {
        io:println("Error occurred with Unsubscription Request: " +
                    response.message());
    }
}
