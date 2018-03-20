package ballerina.net.websub;

//////////////////////////////////////////
/// WebSub Subscriber Service Endpoint ///
//////////////////////////////////////////
@Description {value:"Struct representing the WebSubSubscriber Service Endpoint"}
@Field {value:"config: The configuration for the endpoint"}
@Field {value:"serviceEndpoint: The underlying HTTP service endpoint"}
public struct SubscriberServiceEndpoint {
    http:ServiceEndpointConfiguration config;
    http:ServiceEndpoint serviceEndpoint;
}

public function <SubscriberServiceEndpoint ep> SubscriberServiceEndpoint() {
    ep.serviceEndpoint = {};
}

@Description {value:"Gets called when the endpoint is being initialized during package init"}
@Param {value:"config: The HTTP ServiceEndpointConfiguration of the endpoint"}
public function <SubscriberServiceEndpoint ep> init (http:ServiceEndpointConfiguration config) {
    ep.serviceEndpoint.init(config);
    ep.initWebSubSubscriberServiceEndpoint();
}

public native function <SubscriberServiceEndpoint ep> initWebSubSubscriberServiceEndpoint();

@Description {value:"Gets called whenever a service attaches itself to this endpoint and during package init"}
@Param {value:"serviceType: The service attached"}
public function <SubscriberServiceEndpoint ep> register (typedesc serviceType) {
    ep.serviceEndpoint.register(serviceType);
    ep.registerWebSubSubscriberServiceEndpoint(serviceType);
}

public native function <SubscriberServiceEndpoint ep> registerWebSubSubscriberServiceEndpoint(typedesc serviceType);

@Description {value:"Starts the registered service"}
public function <SubscriberServiceEndpoint ep> start () {
    ep.serviceEndpoint.start();//TODO:not needed?
    ep.startWebSubSubscriberServiceEndpoint();
    ep.sendSubscriptionRequest();
}

@Description {value:"Native function to start the registered WebSub Subscriber service"}
native function <SubscriberServiceEndpoint ep> startWebSubSubscriberServiceEndpoint();

@Description {value:"Sends a subscription request to the specified hub if specified to subscribe on startup"}
function <SubscriberServiceEndpoint ep> sendSubscriptionRequest () {
    map subscriptionDetails = ep.retrieveAnnotationsAndCallback();
    var strSubscribeOnStartUp, _ = (string) subscriptionDetails["subscribeOnStartUp"];
    var subscribeOnStartUp, _ = <boolean> strSubscribeOnStartUp;
    if (subscriptionDetails != null && subscribeOnStartUp) {
        var hub, _ = (string) subscriptionDetails["hub"];
        invokeClientConnectorForSubscription(hub, subscriptionDetails);
    }
}

@Description {value:"Retrieves the annotations specified and the callback URL to which notification should happen"}
native function <SubscriberServiceEndpoint ep> retrieveAnnotationsAndCallback () (map);

@Description {value:"Returns the connector that client code uses"}
@Return {value:"The connector that client code uses"}
public function <SubscriberServiceEndpoint ep> getClient () returns (http:Connection) {
    return ep.serviceEndpoint.getClient();
}

@Description {value:"Stops the registered service"}
public function <SubscriberServiceEndpoint ep> stop () {
    ep.serviceEndpoint.stop();
}

@Description {value:"Function to invoke the WebSubSubscriberConnector's actions for subscription"}
@Param {value:"hub: The hub to which the subscription request is to be sent"}
@Param {value:"subscriptionDetails: Map containing subscription details"}
function invokeClientConnectorForSubscription (string hub, map subscriptionDetails) {
    endpoint HubClientEndpoint websubHubClientEP { uri:hub };
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
    subscriptionChangeResponse, webSubError = websubHubClientEP -> subscribe(subscriptionChangeRequest);
    if (webSubError == null) {
        log:printInfo("Subscription Request successful at Hub[" + subscriptionChangeResponse.hub +"], for Topic["
                      + subscriptionChangeResponse.topic + "]");
    } else {
        log:printError("Subscription Request failed at Hub[" + hub +"], for Topic[" + topic + "]: "
                       + webSubError.errorMessage);
    }
}
