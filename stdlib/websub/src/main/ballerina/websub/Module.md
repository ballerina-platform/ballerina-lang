## Module overview
This module contains an implementation of the W3C [**WebSub**](https://www.w3.org/TR/websub/) recommendation, which facilitates a push-based content delivery/notification mechanism between publishers and subscribers.

This implementation supports introducing all WebSub components: subscribers, publishers, and hubs.
 
- Subscriber - A party interested in receiving update notifications for particular topics.
- Publisher - A party that advertises topics to which interested parties subscribe in order to receive notifications
 on occurrence of events.
- Hub - A party that accepts subscription requests from subscribers and delivers content to the subscribers when the topic 
is updated by the topic's publisher.
 
 
#### Basic Flow with WebSub
1. Subscriber discovers the topics it needs to subscribe to in order to receive updates/content and discovers the hub(s) where it can subscribe.

2. Subscriber sends a subscription request to a hub, specifying the topic it needs to receive notifications for along 
 with other subscription parameters, such as:
      - The callback URL where content is expected to be delivered
      - (Optional) A lease seconds value indicating how long the subscriber wants the subscription to stay active
      - (Optional) A secret to use for [authenticated content distribution](https://www.w3.org/TR/websub/#signing-content)
  
3. The hub sends an intent verification request to the specified callback URL, and if the response indicates verification
 (by echoing a challenge specified in the request) by the subscriber, the subscription is added for the topic at the 
 hub.
   
4. Publisher notifies the hub of updates to the topic, and the content to deliver is identified.

5. The hub delivers the identified content to the subscribers of the topic.

#### Features
This module allows introducing a WebSub Subscriber Service with `onIntentVerification`, which accepts HTTP GET requests for intent verification, and `onNotification`, which accepts HTTP POST requests for notifications. The WebSub Subscriber Service provides the following capabilities:
 - Subscription Requests are sent at service start time for the hub and topic, which are either specified as annotations or discovered based on the resource URL specified as an annotation.
 - Auto Intent Verification against the topic specified as an annotation, or discovered based on the resource URL specified as an annotation, if `onIntentVerification` is not specified.
 - Signature Validation for authenticated content distribution if a secret is specified for the subscription.
  
A WebSub compliant hub based on the Ballerina Message Broker is also available for use as a remote hub or to be used by publishers who want to have their own internal hub. Ballerina's WebSub Hub honors specified lease periods and supports authenticated content distribution.

Ballerina WebSub publishers can use utility functions to add WebSub link headers indicating the hub and topic 
URLs, which facilitates WebSub discovery.

A hub client endpoint is also made available to publishers and subscribers to perform the following:
 1. Publishers
    - Register a topic at the Hub
    - Publish to the hub, indicating an update of the topic
 2. Subscribers
    - Subscribe/Unsubscribe to topics at a hub

## Samples
This sample demonstrates a Subscriber Service with `subscribeOnStartUp` set to true, which will result in a
 subscription request being sent to the specified hub for the specified topic, with the specified lease seconds value 
 and the specified secret for authenticated content distribution.
Since an `onIntentVerification` resource is not included, intent verification for subscription and unsubscription 
requests would happen automatically, if the topic specified in the request matches that specified as an annotation or 
that discovered for the annotated resource URL.
 
```ballerina
import ballerina/log;
import ballerina/websub;

endpoint websub:Listener websubEP {
    port: 8181
};

@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    topic: "<TOPIC_URL>",
    hub: "<HUB_URL>",
    leaseSeconds: 3600,
    secret: "<SECRET>"
}
service websubSubscriber bind websubEP {

    onNotification(websub:Notification notification) {
        match (notification.getPayloadAsString()) {
            string payloadAsString => log:printInfo("WebSub Notification Received: "
                                                        + payloadAsString);
            error e => log:printError("Error retrieving payload as string", err = e);
        }
    }
 
}
```

Explicit intent verification can be done by introducing an ```onIntentVerification``` resource.
```ballerina
import ballerina/log;
import ballerina/http;
import ballerina/websub;

endpoint websub:Listener websubEP {
    port: 8181
};

@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    topic: "<TOPIC_URL>",
    hub: "<HUB_URL>",
    leaseSeconds: 3600,
    secret: "<SECRET>"
}
service websubSubscriber bind websubEP {

    onIntentVerification(endpoint caller, websub:IntentVerificationRequest request) {
        http:Response response = new;
        // Insert logic to build subscription/unsubscription intent verification response
        caller->respond(response) but { 
            error e => log:printError("Error responding to intent verification request", err = e) 
        };
    }

    onNotification(websub:Notification notification) {
        match (notification.getPayloadAsString()) {
            string payloadAsString => log:printInfo("WebSub Notification Received: "
                                                        + payloadAsString);
            error e => log:printError("Error retrieving payload as string", err = e);
        }
    }
    
}
```

Functions are made available on the `websub:IntentVerificationRequest` to build a subscription or unsubscription 
verification response, specifying the topic to verify intent against:
```ballerina
http:Response response = request.buildSubscriptionVerificationResponse("<TOPIC_TO_VERIFY_FOR>");
```
```ballerina
http:Response response = request.buildUnsubscriptionVerificationResponse("<TOPIC_TO_VERIFY_FOR>");
```
 
Ballerina publishers can start up and publish directly to the Ballerina WebSub hub.
```ballerina
import ballerina/log;
import ballerina/runtime;
import ballerina/websub;

public function main(string... args) {

    log:printInfo("Starting up the Ballerina Hub Service");
    websub:WebSubHub webSubHub = websub:startHub(9191) but {
        websub:HubStartedUpError hubStartedUpErr => hubStartedUpErr.startedUpHub
    };

    var registrationResponse = webSubHub.registerTopic("<TOPIC_URL>");
    match (registrationResponse) {
        error webSubError => log:printError("Error occurred registering topic: " + webSubError.message);
        () => log:printInfo("Topic registration successful!");
    }

    // Make the publisher wait until the subscriber subscribes at the hub.
    runtime:sleep(20000);

    log:printInfo("Publishing update to internal Hub");
    var publishResponse = webSubHub.publishUpdate("<TOPIC_URL>", {"action": "publish", "mode": "internal-hub"});
    match (publishResponse) {
        error webSubError => log:printError("Error notifying hub: " + webSubError.message);
        () => log:printInfo("Update notification successful!");
    }

    // Make sure the service is running until the subscriber receives the update notification.
    runtime:sleep(5000);
}
```

Ballerina publishers can also use the hub client endpoint to register topics at Ballerina WebSub hubs 
and publish/notify updates to the remote hubs.
```ballerina
import ballerina/log;
import ballerina/runtime;
import ballerina/websub;

endpoint websub:Client websubHubClientEP {
    url: "https://localhost:9191/websub/hub"
};

public function main(string... args) {

    var registrationResponse = websubHubClientEP->registerTopic("<TOPIC_URL>");
    match (registrationResponse) {
        error webSubError => log:printError("Error occurred registering topic: " + webSubError.message);
        () => log:printInfo("Topic registration successful!");
    }

    // Make the publisher wait until the subscriber subscribes at the hub.
    runtime:sleep(10000);

    log:printInfo("Publishing update to remote Hub");
    var publishResponse = websubHubClientEP->publishUpdate("<TOPIC_URL>", {"action": "publish", "mode": "remote-hub"});
    match (publishResponse) {
        error webSubError => log:printError("Error notifying hub: " + webSubError.message);
        () => log:printInfo("Update notification successful!");
    }

}
```

The hub client endpoint can also be used by subscribers to send subscription and unsubscription requests explicitly.
```ballerina
import ballerina/log;
import ballerina/websub;

endpoint websub:Client websubHubClientEP {
    url: "<HUB_URL>"
};

public function main(string... args) {

    // Send subscription request for a subscriber service.
    websub:SubscriptionChangeRequest subscriptionRequest = { topic: "<TOPIC_URL>", 
                                                             callback: "<CALLBACK_URL>",
                                                             secret: "<SECRET>" };

    var response = websubHubClientEP->subscribe(subscriptionRequest);
    match (response) {
        websub:SubscriptionChangeResponse subscriptionChangeResponse => {
            log:printInfo("Subscription Request successful at Hub [" + subscriptionChangeResponse.hub 
                    + "] for Topic [" + subscriptionChangeResponse.topic + "]");
        }
        error e => {
            log:printError("Error occurred with Subscription Request", err = e);
        }
    }

    // Send unsubscription request for the subscriber service.
    websub:SubscriptionChangeRequest unsubscriptionRequest = { topic: "<TOPIC_URL>",
                                                               callback: "<CALLBACK_URL>" };

    response = websubHubClientEP->unsubscribe(unsubscriptionRequest);
    match (response) {
        websub:SubscriptionChangeResponse subscriptionChangeResponse => {
            log:printInfo("Unsubscription Request successful at Hub [" + subscriptionChangeResponse.hub
                    + "] for Topic [" + subscriptionChangeResponse.topic + "]");
        }
        error e => {
            log:printError("Error occurred with Unsubscription Request", err = e);
        }
    }

}
```

## Configuration Parameters
The Ballerina WebSub implementation allows specifying the following properties/parameters via the Ballerina Config API,
where the values specified via the Config API would override values specified as params on hub start up.


| Configuration Key              | Default Value | Description                                                        |
|--------------------------------| --------------|--------------------------------------------------------------------|
| b7a.websub.hub.host            | 0.0.0.0       | The host to start the WebSub Hub Service on                        |
| b7a.websub.hub.port            | N/A           | The port to start the WebSub Hub Service on                        | 
| b7a.websub.hub.leasetime       | 86400         | The default lease period, if not specified in a request            |
| b7a.websub.hub.signaturemethod | "SHA256"      | The signature method to use for authenticated content distribution |
| b7a.websub.hub.remotepublish   | false         | Whether publishing updates against the topics in the hub could be done by remote publishers via HTTP requests with `hub.mode` set to `publish`  |
| b7a.websub.hub.topicregistration | true      | Whether a topic needs to be registered at the hub for publishers to publish updates against the topic and for subscribers to send subscription requests for the topic |
| b7a.websub.hub.enablessl | true      | Whether the Hub service should be exposed over HTTPS |
