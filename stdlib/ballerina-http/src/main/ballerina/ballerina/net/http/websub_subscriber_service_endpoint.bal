package ballerina.net.http;

//////////////////////////////////////////
/// WebSub Subscriber Service Endpoint ///
//////////////////////////////////////////
@Description {value:"Struct representing the WebSubSubscriber Service Endpoint"}
@Field {value:"config: The configuration for the endpoint"}
@Field {value:"serviceEndpoint: The underlying HTTP service endpoint"}
public struct WebSubSubscriberServiceEndpoint {
    ServiceEndpointConfiguration config;
    ServiceEndpoint serviceEndpoint;
}

public function <WebSubSubscriberServiceEndpoint ep> WebSubSubscriberServiceEndpoint() {
    ep.serviceEndpoint = {};
}

@Description {value:"Gets called when the endpoint is being initialized during package init"}
@Param {value:"config: The HTTP ServiceEndpointConfiguration of the endpoint"}
public function <WebSubSubscriberServiceEndpoint ep> init (ServiceEndpointConfiguration config) {
    ep.serviceEndpoint.init(config);
}

@Description {value:"Gets called whenever a service attaches itself to this endpoint and during package init"}
@Param {value:"serviceType: The service attached"}
public function <WebSubSubscriberServiceEndpoint ep> register (type serviceType) {
    ep.serviceEndpoint.register(serviceType);
}

@Description {value:"Starts the registered service"}
public function <WebSubSubscriberServiceEndpoint ep> start () {
    ep.serviceEndpoint.start();//TODO:not needed?
    ep.startWebSubSubscriberServiceEndpoint();
    ep.sendSubscriptionRequest();
}

@Description {value:"Native function to start the registered WebSub Subscriber service"}
native function <WebSubSubscriberServiceEndpoint ep> startWebSubSubscriberServiceEndpoint();

@Description {value:"Sends a subscription request to the specified hub if specified to subscribe on startup"}
function <WebSubSubscriberServiceEndpoint ep> sendSubscriptionRequest () {
    map subscriptionDetails = ep.retrieveAnnotationsAndCallback();
    var strSubscribeOnStartUp, _ = (string) subscriptionDetails["subscribeOnStartUp"];
    var subscribeOnStartUp, _ = <boolean> strSubscribeOnStartUp;
    if (subscriptionDetails != null && subscribeOnStartUp) {
        var hub, _ = (string) subscriptionDetails["hub"];
        invokeClientConnectorForSubscription(hub, subscriptionDetails);
    }
}

@Description {value:"Retrieves the annotations specified and the callback URL to which notification should happen"}
native function <WebSubSubscriberServiceEndpoint ep> retrieveAnnotationsAndCallback () (map);

@Description {value:"Returns the connector that client code uses"}
@Return {value:"The connector that client code uses"}
public function <WebSubSubscriberServiceEndpoint ep> getClient () returns (Connection) {
    return ep.serviceEndpoint.getClient();
}

@Description {value:"Stops the registered service"}
public function <WebSubSubscriberServiceEndpoint ep> stop () {
    ep.serviceEndpoint.stop();
}

@Description {value:"Function to invoke the WebSubSubscriberConnector's actions for subscription"}
@Param {value:"hub: The hub to which the subscription request is to be sent"}
@Param {value:"subscriptionDetails: Map containing subscription details"}
function invokeClientConnectorForSubscription (string hub, map subscriptionDetails) {
    endpoint WebSubSubscriberClientEndpoint websubSubscriberClientEP { uri:hub };
    var topic, _ = (string) subscriptionDetails["topic"];
    var callback, _ = (string) subscriptionDetails["callback"];
    if (hub == null || topic == null || callback == null) {
        log:printError("Subscription Request not sent since hub, topic and/or callback not specified");
        return;
    }

    var strLeaseSeconds, _ = (string) subscriptionDetails["leaseSeconds"];
    var leaseSeconds, _ = <int> strLeaseSeconds;
    var secret, _ = (string) subscriptionDetails["secret"];

    SubscriptionChangeRequest subscriptionChangeRequest = {topic:topic, callback:callback, leaseSeconds:leaseSeconds,
                                                              secret:secret};
    SubscriptionChangeResponse subscriptionChangeResponse;
    WebSubError webSubError;
    subscriptionChangeResponse, webSubError = websubSubscriberClientEP -> subscribe(subscriptionChangeRequest);
    if (webSubError == null) {
        log:printInfo("Subscription Request successful at Hub[" + subscriptionChangeResponse.hub +"], for Topic["
                      + subscriptionChangeResponse.topic + "]");
    } else {
        log:printError("Subscription Request failed at Hub[" + hub +"], for Topic[" + topic + "]: "
                       + webSubError.errorMessage);
    }
}
