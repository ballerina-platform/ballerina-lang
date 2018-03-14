package ballerina.net.http;

//////////////////////////////////////////
/// WebSub Subscriber Service Endpoint ///
//////////////////////////////////////////
public struct WebSubSubscriberService {
    string epName;
    ServiceEndpointConfiguration config;
    Service serviceEndpoint;
}

public function <WebSubSubscriberService ep> WebSubSubscriberService() {
    ep.serviceEndpoint = {};
}

@Description { value:"Gets called when the endpoint is being initialized during package init" }
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The HTTP ServiceEndpointConfiguration of the endpoint" }
public function <WebSubSubscriberService ep> init (string epName, ServiceEndpointConfiguration config) {
    ep.epName = epName;
    ep.serviceEndpoint.init(epName, config);
}

@Description { value:"Gets called whenever a service attaches itself to this endpoint and during package init" }
@Param { value:"serviceType: The service attached" }
public function <WebSubSubscriberService ep> register (type serviceType) {
    ep.serviceEndpoint.register(serviceType);
}

@Description { value:"Starts the registered service" }
public function <WebSubSubscriberService ep> start () {
    ep.serviceEndpoint.start();
    ep.sendSubscriptionRequest();
}

@Description { value:"Sends a subscription request to the specified hub if specified to subscribe on startup" }
function <WebSubSubscriberService ep> sendSubscriptionRequest () {
    map subscriptionDetails = ep.retrieveAnnotationsAndCallback();
    var strSubscribeOnStartUp, _ = (string) subscriptionDetails["subscribeOnStartUp"];
    var subscribeOnStartUp, _ = <boolean> strSubscribeOnStartUp;
    if (subscriptionDetails != null && subscribeOnStartUp) {
        invokeClientConnectorForSubscription(subscriptionDetails);
    }
}

@Description { value:"Retrieves the annotations specified and the callback URL to which notification should happen" }
native function <WebSubSubscriberService ep> retrieveAnnotationsAndCallback () (map);

@Description { value:"Returns the connector that client code uses" }
@Return { value:"The connector that client code uses" }
public function <WebSubSubscriberService ep> getConnector () returns (ServerConnector repConn) {
    return ep.serviceEndpoint.getConnector();
}

@Description { value:"Stops the registered service" }
public function <WebSubSubscriberService ep> stop () {
    ep.serviceEndpoint.stop();
}