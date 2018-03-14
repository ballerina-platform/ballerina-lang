package ballerina.net.http;

/////////////////////////////////////////
/// WebSub Subscriber Client Endpoint ///
/////////////////////////////////////////
public struct WebSubSubscriberClient {
    string epName;
    ClientEndpointConfiguration config;
    Client clientEndpoint;
}

@Description { value:"Gets called when the endpoint is being initialized during package init" }
@Param { value:"ep: The endpoint to be initialized" }
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The HTTP ClientEndpointConfiguration of the endpoint" }
public function <WebSubSubscriberClient ep> init (string epName, ClientEndpointConfiguration config) {
    ep.config = config;
    ep.clientEndpoint = {};
    ep.clientEndpoint.init(epName, config);
}

@Description { value:"Gets called whenever a service attaches itself to this endpoint and during package init" }
@Param { value:"serviceType: The service attached" }
public function <WebSubSubscriberClient ep> register (type serviceType) {
    ep.clientEndpoint.register(serviceType);
}

@Description { value:"Starts the registered service" }
public function <WebSubSubscriberClient ep> start () {
    ep.clientEndpoint.start();
}

@Description { value:"Returns the connector that client code uses" }
@Return { value:"The connector that client code uses" }
public function <WebSubSubscriberClient ep> getConnector () returns (WebSubSubscriberClientConnector repConn) {
    repConn = new WebSubSubscriberClientConnector(ep.config.serviceUri, ep.clientEndpoint.getConnector());
    return;
}

@Description { value:"Stops the registered service" }
@Return { value:"Error occured during registration" }
public function <WebSubSubscriberClient ep> stop () {
    ep.clientEndpoint.stop();
}