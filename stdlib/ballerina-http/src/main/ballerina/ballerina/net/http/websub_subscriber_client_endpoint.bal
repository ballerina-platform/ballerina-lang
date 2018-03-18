package ballerina.net.http;

/////////////////////////////////////////
/// WebSub Subscriber Client Endpoint ///
/////////////////////////////////////////
@Description {value:"Struct representing the WebSubSubscriber Client Endpoint"}
@Field {value:"config: The configuration for the endpoint"}
@Field {value:"httpClientEndpoint: The underlying HTTP client endpoint"}
public struct WebSubSubscriberClientEndpoint {
    WebSubSubscriberClientEndpointConfiguration config;
    ClientEndpoint httpClientEndpoint;
}

@Description {value:"Struct representing the WebSubSubscriber Client Endpoint configuration"}
@Field {value:"uri: The URI of the target Hub"}
public struct WebSubSubscriberClientEndpointConfiguration {
    string uri;
    //TODO: include header, topic-resource map
}

@Description {value:"Gets called when the endpoint is being initialized during package init"}
@Param {value:"ep: The endpoint to be initialized"}
@Param {value:"config: The configuration for the endpoint"}
public function <WebSubSubscriberClientEndpoint ep> init (WebSubSubscriberClientEndpointConfiguration config) {
    endpoint ClientEndpoint httpClientEndpoint {targets:[{uri:config.uri}]};
    ep.httpClientEndpoint = httpClientEndpoint;
    ep.config = config;
}

@Description {value:"Gets called whenever a service attaches itself to this endpoint and during package init"}
@Param {value:"serviceType: The service attached"}
public function <WebSubSubscriberClientEndpoint ep> register (type serviceType) {
    ep.httpClientEndpoint.register(serviceType);
}

@Description {value:"Starts the registered service"}
public function <WebSubSubscriberClientEndpoint ep> start () {
    ep.httpClientEndpoint.start();
}

@Description {value:"Returns the connector that client code uses"}
@Return {value:"The connector that client code uses"}
public function <WebSubSubscriberClientEndpoint ep> getClient () (WebSubSubscriberClientConnector wssClientConn) {
    wssClientConn = { hubUri:ep.config.uri, httpClientEndpoint:ep.httpClientEndpoint };
    return;
}

@Description {value:"Stops the registered service"}
@Return {value:"Error occured during registration"}
public function <WebSubSubscriberClientEndpoint ep> stop () {
    ep.httpClientEndpoint.stop();
}
