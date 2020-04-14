This module contains an implementation of the W3C [**WebSub**](https://www.w3.org/TR/websub/) recommendation, which facilitates a push-based content delivery/notification mechanism between publishers and subscribers.

This implementation supports introducing all WebSub components:
 
- Subscriber - A party interested in receiving update notifications for particular topics.
- Publisher - A party that advertises topics to which interested parties subscribe in order to receive notifications
 on occurrence of events.
- Hub - A party that accepts subscription requests from subscribers and delivers content to the subscribers when the topic 
is updated by the topic's publisher.

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

```ballerina
http:BasicAuthHandler basicAuthHandler = new(new auth:InboundBasicAuthProvider());

http:ServiceEndpointConfiguration hubListenerConfig = {
    auth: {
        authHandlers: [basicAuthHandler]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("b7a.home") + "bre/security/ballerinaKeystore.p12",
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

For information on the operations, which you can perform with this module, see the below **Functions**. For examples on the usage of the operations, see the following.
 * [Internal Hub Sample Example](hhttps://ballerina.io/learn/by-example/websub-internal-hub-sample.html)
 * [Remote Hub Sample Example](https://ballerina.io/learn/by-example/websub-remote-hub-sample.html)
 * [Hub Client Sample Example](https://ballerina.io/learn/by-example/websub-hub-client-sample.html)
 * [Service Integration Sample Example](https://ballerina.io/learn/by-example/websub-service-integration-sample.html)