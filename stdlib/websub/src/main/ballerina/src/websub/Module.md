## Module overview

This module contains an implementation of the W3C [**WebSub**](https://www.w3.org/TR/websub/) recommendation, which facilitates a push-based content delivery/notification mechanism between publishers and subscribers.

This implementation supports introducing all WebSub components: subscribers, publishers, and hubs.
 
- Subscriber - A party interested in receiving update notifications for particular topics.
- Publisher - A party that advertises topics to which interested parties subscribe in order to receive notifications
 on occurrence of events.
- Hub - A party that accepts subscription requests from subscribers and delivers content to the subscribers when the topic 
is updated by the topic's publisher.
 
 
### Basic flow with WebSub
1. The subscriber discovers from the publisher, the topic it needs to subscribe to and the hub(s) that deliver notifications on updates of the topic.

2. The subscriber sends a subscription request to one or more discovered hub(s) specifying the discovered topic along 
 with other subscription parameters such as:
    - The callback URL to which content is expected to be delivered.
    - (Optional) The lease period (in seconds) the subscriber wants the subscription to stay active.
    - (Optional) A secret to use for [authenticated content distribution](https://www.w3.org/TR/websub/#signing-content).
  
3. The hub sends an intent verification request to the specified callback URL. If the response indicates 
verification
 (by echoing a challenge specified in the request) by the subscriber, the subscription is added for the topic at the 
 hub.
   
4. The publisher notifies the hub of updates to the topic and the content to deliver is identified.

5. The hub delivers the identified content to the subscribers of the topic.

### Features

#### Subscriber

This module allows introducing a WebSub Subscriber Service with `onIntentVerification`, which accepts HTTP GET requests for intent verification, and `onNotification`, which accepts HTTP POST requests for notifications. The WebSub Subscriber Service provides the following capabilities:
 - When the service is started a subscription request is sent for a hub/topic combination, either specified as annotations or discovered based on the resource URL specified as an annotation.
 - If `onIntentVerification` is not specified, intent verification will be done automatically against the topic specified as an annotation or discovered based on the resource URL specified as an annotation.
 - If a secret is specified for the subscription, signature validation will be done for authenticated content distribution.
  
#### Hub

A WebSub compliant hub based on the Ballerina Message Broker is also available. This can be used as a remote hub or to be used by publishers who want to have their own internal hub. Ballerina's WebSub hub honors specified lease periods and supports authenticated content distribution.

##### Enabling Basic Auth support for the hub

The Ballerina WebSub Hub can be secured by enforcing authentication (Basic Authentication) and (optionally) authorization. 
The `AuthProvider` and `authConfig` need to be specified for the hub listener and service respectively. If the 
`authStoreProvider` of the `AuthProvider` is set as "http:CONFIG_AUTH_STORE", usernames and passwords for authentication and scopes for authorization would be read from a config TOML file.
A user can specify `AuthProvider` as follows and set it to the `hubListenerConfig` record, which is passed when starting the hub.

``` ballerina
http:BasicAuthHandler basicAuthHandler = new(new auth:InboundBasicAuthProvider());

http:ServiceEndpointConfiguration hubListenerConfig = {
    auth: {
        authHandlers: [basicAuthHandler]
    },
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

var val = websub:startHub(new http:Listener(9191, hubListenerConfig));
```
In addition to the `BasicAuthHandler` for the listener, a user also has to specify the `authConfig` properties at the service or resource levels. 

 
They can be set by passing arguments for the `serviceAuth`, `subscriptionResourceAuth` or `publisherResourceAuth` parameters when starting up the hub.

Recognized users can be specified in a `.toml` file, which can be passed as a configuration file when running the program. 

```
[b7a.users]

[b7a.users.tom]
password="1234"
scopes="scope1"
```

Once the hub is secured using basic auth, a subscriber should provide the relevant `auth` config in the 
`hubClientConfig` field of the subscriber service annotation.

```ballerina
auth:OutboundBasicAuthProvider basicAuthProvider = new({
    username: "tom",
    password: "1234"
});

http:BasicAuthHandler basicAuthHandler = new(basicAuthProvider);

@websub:SubscriberServiceConfig {
    path: "/ordereventsubscriber",
    hubClientConfig: {
        auth: {
            authHandler: basicAuthHandler
        }
    }
}
```

##### Enabling data persistence for the hub

The Ballerina WebSub Hub supports persistence of topic and subscription data that needs to be restored when the hub is 
restarted.
 
Users can introduce their own persistence implementation, by introducing an object type that is structurally 
equivalent to the `websub:HubPersistenceStore` abstract object.

Persistence can be enabled by setting a suitable `websub:HubPersistenceStore` value for the `hubPersistenceStore` field 
in the `HubConfiguration` record, which is passed to the `websub:startHub()` function.

Any subscriptions added at the hub will be available even after the hub is restarted.

#### Publisher

Ballerina WebSub publishers can use utility functions to add WebSub link headers indicating the hub and topic 
URLs, which facilitates WebSub discovery.

A hub client endpoint is also made available to publishers and subscribers to perform the following:
- Publishers
  - Register a topic at the Hub
  - Publish to the hub indicating an update of the topic
- Subscribers
  - Subscribe/Unsubscribe to/from topics at a hub

## Samples
This sample demonstrates a Subscriber Service with `subscribeOnStartUp` set to true. This will result in a
 subscription request being sent to the specified hub for the specified topic, with the specified lease seconds value 
 and the specified secret for authenticated content distribution.
Since an `onIntentVerification` resource function is not included, intent verification for subscription and unsubscription 
requests would happen automatically, if the topic specified in the request matches that specified as an annotation or 
that discovered for the annotated resource URL.
 
```ballerina
import ballerina/log;
import ballerina/websub;

listener websub:Listener websubEP = new(8181);

@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    target: ["<HUB_URL>", "<TOPIC_URL>"],
    leaseSeconds: 3600,
    secret: "<SECRET>"
}
service websubSubscriber on websubEP {

    resource function onNotification(websub:Notification notification) {
        var payload = notification.getTextPayload();
        if (payload is string) {
            log:printInfo("WebSub Notification Received: " + payload);
        } else {
            log:printError("Error retrieving payload as string", payload);
        }
    }
}
```

Explicit intent verification can be done by introducing an `onIntentVerification` resource function.
```ballerina
import ballerina/http;
import ballerina/log;
import ballerina/websub;

listener websub:Listener websubEP = new(8181);

@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    target: ["<HUB_URL>", "<TOPIC_URL>"],
    leaseSeconds: 3600,
    secret: "<SECRET>"
}
service websubSubscriber on websubEP {

    resource function onIntentVerification(websub:Caller caller, websub:IntentVerificationRequest request) {
        http:Response response = new;
        // Insert logic to build subscription/unsubscription intent verification response.
        var result = caller->respond(response);
        if (result is error) { 
            log:printError("Error responding to intent verification request", result);
        }
    }

    resource function onNotification(websub:Notification notification) {
        var payload = notification.getTextPayload();
        if (payload is string) {
            log:printInfo("WebSub Notification Received: " + payload);
        } else {
            log:printError("Error retrieving payload as string", payload);
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
import ballerina/http;
import ballerina/runtime;
import ballerina/websub;

public function main() {

    log:printInfo("Starting up the Ballerina Hub Service");
    var result = websub:startHub(new http:Listener(9191));
    websub:Hub webSubHub = result is websub:Hub ? result : result.startedUpHub;

    var registrationResponse = webSubHub.registerTopic("<TOPIC_URL>");
    if (registrationResponse is error) {
        log:printError("Error occurred registering topic: " + <string>registrationResponse.detail()?.message);
    } else {
        log:printInfo("Topic registration successful!");
    }

    // Make the publisher wait until the subscriber subscribes at the hub.
    runtime:sleep(20000);

    log:printInfo("Publishing update to internal Hub");
    var publishResponse = webSubHub.publishUpdate("<TOPIC_URL>", {"action": "publish", "mode": "internal-hub"});
    if (publishResponse is error) {
        log:printError("Error notifying hub: " + <string>publishResponse.detail()?.message);
    } else {
        log:printInfo("Update notification successful!");
    }

    // Make sure the service is running until the subscriber receives the update notification.
    runtime:sleep(5000);
}
```

Ballerina publishers can also use the `websub:PublisherClient` to register topics at Ballerina WebSub hubs 
and publish/notify updates to the remote hubs.
```ballerina
import ballerina/log;
import ballerina/runtime;
import ballerina/websub;

websub:PublisherClient websubHubClientEP = new ("https://localhost:9191/websub/hub");

public function main() {

    var registrationResponse = websubHubClientEP->registerTopic("<TOPIC_URL>");
    if (registrationResponse is error) {
        log:printError("Error occurred registering topic: " + <string>registrationResponse.detail()?.message);
    } else {
        log:printInfo("Topic registration successful!");
    }

    // Make the publisher wait until the subscriber subscribes at the hub.
    runtime:sleep(10000);

    log:printInfo("Publishing update to remote Hub");
    var publishResponse = websubHubClientEP->publishUpdate("<TOPIC_URL>", { "action": "publish", "mode": "remote-hub" });
    if (publishResponse is error) {
        log:printError("Error notifying hub: " + <string>publishResponse.detail()?.message);
    } else {
        log:printInfo("Update notification successful!");
    }

}
```

The `websub:SubscriptionClient` can be used by subscribers to send subscription and unsubscription requests explicitly.
```ballerina
import ballerina/log;
import ballerina/websub;

websub:SubscriptionClient websubHubClientEP = new("<HUB_URL>");

public function main() {

    // Send subscription request for a subscriber service.
    websub:SubscriptionChangeRequest subscriptionRequest = {
        topic: "<TOPIC_URL>", 
        callback: "<CALLBACK_URL>",
        secret: "<SECRET>"
    };

    var subscriptionChangeResponse = websubHubClientEP->subscribe(subscriptionRequest);
    if (subscriptionChangeResponse is websub:SubscriptionChangeResponse) {
        log:printInfo("Subscription Request successful at Hub [" + subscriptionChangeResponse.hub 
                        + "] for Topic [" + subscriptionChangeResponse.topic + "]");
    } else {
        log:printError("Error occurred with Subscription Request", err = subscriptionChangeResponse);
    }

    // Send unsubscription request for the subscriber service.
    websub:SubscriptionChangeRequest unsubscriptionRequest = {
        topic: "<TOPIC_URL>",
        callback: "<CALLBACK_URL>"
    };

    subscriptionChangeResponse = websubHubClientEP->unsubscribe(unsubscriptionRequest);
    if (subscriptionChangeResponse is websub:SubscriptionChangeResponse) {
        log:printInfo("Unsubscription Request successful at Hub [" + subscriptionChangeResponse.hub
                + "] for Topic [" + subscriptionChangeResponse.topic + "]");
    } else {
        log:printError("Error occurred with Unsubscription Request", err = subscriptionChangeResponse);
    }
}
```

## Introducing Specific Subscriber Services (Webhook Callback Services)

Ballerina's WebSub subscriber service listener can be extended to introduce specific Webhooks.
 
Instead of the single `onNotification` resource, you can introduce multiple resources to accept content delivery requests using specific subscriber services. These resources will correspond to the content delivery requests that will 
 be delivered with respect to a particular topic.
 
For example, assume a scenario in which you receive notifications either when an issue is opened or when an issue is closed by subscribing to a particular topic in an issue tracking system. With a custom subscriber service listener, which extends the 
generic WebSub subscriber service listener, you can allow two resources to accept content delivery requests (e.g., `onIssueOpened` and `onIssueClosed`) instead of the `onNotification` resource.

These resources will accept two parameters:
1. The generic `websub:Notification` record as the first parameter
2. A custom record corresponding to the expected (JSON) payload of the notification (e.g., `IssueCreatedEvent`,
`IssueClosedEvent`)

You can introduce a specific service as such by extending the generic subscriber service listener, specifying a 
mapping between the expected notifications and the resources that requests need to be dispatched to.

The mapping can be based on one of the following indicators of a notification request. 
(Requests will then be dispatched based on the value of the indicator in the request and a pre-defined mapping.)

- A request header 
- The payload: the value of a particular key in the JSON payload
- A request header and the payload (combination of the above two)

#### Samples for Resource Mapping

**Based on a request header**

Dispatching will be based on the value of the request header specified as `topicHeader`. 

```ballerina
websub:ExtensionConfig extensionConfig = {
    topicIdentifier: websub:TOPIC_ID_HEADER,
    topicHeader: "<HEADER_TO_CONSIDER>",
    headerResourceMap: {
        "issueOpened": ["onIssueOpened", IssueOpenedEvent],
        "issueClosed": ["onIssueClosed", IssueClosedEvent]
    }
};
```

The `"issueOpened": ["onIssueOpened", IssueOpenedEvent]` entry indicates that when the value of the
`<HEADER_TO_CONSIDER>` header is `issueOpened`, dispatching should happen to a resource named `onIssueOpened`. 

The first parameter of this resource will be the generic `websub:Notification` record, and the second parameter will 
be a custom `IssueOpenedEvent` record mapping the JSON payload received when an issue is created.  

**Based on the payload**

Dispatching will be based on the value in the request payload of one of the map keys specified in the 
`payloadKeyResourceMap` map.

```ballerina
websub:ExtensionConfig extensionConfig = {
    topicIdentifier: websub:TOPIC_ID_PAYLOAD_KEY,
    payloadKeyResourceMap: {
        "<PAYLOAD_KEY_TO_CONSIDER>": {
            "issueOpened": ["onIssueOpened", IssueOpenedEvent],
            "issueClosed": ["onIssueClosed", IssueClosedEvent]
        }
    }
};
```

The `"issueOpened": ["onIssueOpened", IssueOpenedEvent]` entry indicates that when the value for the JSON payload
 key `<PAYLOAD_KEY_TO_CONSIDER>` is `issueOpened`, dispatching should happen to a resource named `onIssueOpened`.

The first parameter of this resource will be the generic `websub:Notification` record, and the second parameter will 
be a custom `IssueOpenedEvent` record, mapping the JSON payload received when an issue is created.   

**Based on a request header and the payload**
 
Dispatching will be based on both a request header and the payload as specified in the `headerAndPayloadKeyResourceMap`. 
Also, you can introduce a `headerResourceMap` and/or a `payloadKeyResourceMap` as additional mappings.
 
```ballerina
websub:ExtensionConfig extensionConfig = {
    topicIdentifier: websub:TOPIC_ID_HEADER_AND_PAYLOAD,
    topicHeader: "<HEADER_TO_CONSIDER>",
    headerAndPayloadKeyResourceMap: {
        "issue" : {
            "<PAYLOAD_KEY_TO_CONSIDER>" : {
                "opened": ["onIssueOpened", IssueOpenedEvent],
                "closed": ["onIssueClosed", IssueClosedEvent]
            }
        }
    }
};
```
The `"opened": ["onIssueOpened", IssueOpenedEvent]` entry indicates that when the value of the
`<HEADER_TO_CONSIDER>` header is `issue` and the value of the `<PAYLOAD_KEY_TO_CONSIDER>` JSON payload key is `opened`, 
dispatching should happen to a resource named `onIssueOpened`.

The first parameter of this resource will be the generic `websub:Notification` record and the second parameter will 
be a custom `IssueOpenedEvent` record, mapping the JSON payload received when an issue is created.  
 
#### Sample Specific Subscriber Service

In order to introduce a specific subscriber service, a new Ballerina `listener` needs to be introduced. This `listener` should wrap the generic `ballerina/websub:Listener` and include the extension configuration described above.

The following example is for a service provider that
- allows registering webhooks to receive notifications when an issue is opened or assigned
- includes a header named "Event-Header" in each content delivery request indicating what event the notification is 
for (e.g., "onIssueOpened" when an issue is opened and "onIssueAssigned" when an issue is assigned)

```ballerina
import ballerina/lang.'object as objects;
import ballerina/websub;

// Introduce a record mapping the JSON payload received when an issue is opened.
public type IssueOpenedEvent record {
    int id;
    string title;
    string openedBy;
}; 

// Introduce a record mapping the JSON payload received when an issue is assigned.
public type IssueAssignedEvent record {
    int id;
    string assignedTo;
}; 

// Introduce a new `listener` wrapping the generic `ballerina/websub:Listener` 
public type WebhookListener object {

    *objects:Listener;

    private websub:Listener websubListener;

    public function __init(int port) {
        // Introduce the extension config, based on the mapping details.
        websub:ExtensionConfig extensionConfig = {
            topicIdentifier: websub:TOPIC_ID_HEADER,
            topicHeader: "Event-Header",
            headerResourceMap: {
                "issueOpened": ["onIssueOpened", IssueOpenedEvent],
                "issueAssigned": ["onIssueAssigned", IssueAssignedEvent]
            }
        };
        
        // Set the extension config in the generic `websub:Listener` config.
        websub:SubscriberListenerConfiguration sseConfig = {
            extensionConfig: extensionConfig
        };
            
        // Initialize the wrapped generic listener.
        self.websubListener = new(port, sseConfig);
    }

    public function __attach(service s, string? name = ()) returns error?  {
        return self.websubListener.__attach(s, name);
    }

    public function __start() returns error? {
        return self.websubListener.__start();
    }
    
    public function __detach(service s) returns error? {
        return self.websubListener.__detach(s);
    }
    
    public function __immediateStop() returns error? {
        return self.websubListener.__immediateStop();
    }

    public function __gracefulStop() returns error? {
        return self.websubListener.__gracefulStop();
    }
};    
```

A service can now be introduced for the above service provider as follows.
```ballerina
import ballerina/io;
import ballerina/log;
import ballerina/websub;

@websub:SubscriberServiceConfig {
    path: "/subscriber",
    subscribeOnStartUp: false
}
service specificSubscriber on new WebhookListener(8080) {
    resource function onIssueOpened(websub:Notification notification, IssueOpenedEvent issueOpened) {
        log:printInfo(io:sprintf("Issue opened: ID: %s, Title: %s", issueOpened.id, issueOpened.title));
    }
    
    resource function onIssueAssigned(websub:Notification notification, IssueAssignedEvent issueAssigned) {
        log:printInfo(io:sprintf("Issue ID %s assigned to %s", issueAssigned.id, issueAssigned.assignedTo));
    }
}
```

For a step-by-step guide on introducing custom subscriber services, see the ["Create Webhook Callback Services"](https://ballerina.io/learn/how-to-extend-ballerina/#create-webhook-callback-services) section of "How to Extend Ballerina". 
