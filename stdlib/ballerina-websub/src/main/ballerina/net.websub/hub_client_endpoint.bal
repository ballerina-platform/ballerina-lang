package ballerina.net.websub;

import ballerina/http;

//////////////////////////////////////////
/////// WebSub Hub Client Endpoint ///////
//////////////////////////////////////////
@Description {value:"Struct representing the WebSub Hub Client Endpoint"}
@Field {value:"config: The configuration for the endpoint"}
@Field {value:"httpClientEndpoint: The underlying HTTP client endpoint"}
public struct HubClientEndpoint {
    HubClientEndpointConfiguration config;
    http:ClientEndpoint httpClientEndpoint;
}

@Description {value:"Struct representing the WebSub Hub Client Endpoint configuration"}
@Field {value:"uri: The URI of the target Hub"}
public struct HubClientEndpointConfiguration {
    string uri;
    http:SecureSocket|null secureSocket;
    //TODO: include header, topic-resource map
}

@Description {value:"Gets called when the endpoint is being initialized during package init"}
@Param {value:"ep: The endpoint to be initialized"}
@Param {value:"config: The configuration for the endpoint"}
public function <HubClientEndpoint ep> init (HubClientEndpointConfiguration config) {
    endpoint http:ClientEndpoint httpClientEndpoint {targets:[{url:config.uri, secureSocket:config.secureSocket}]};
    ep.httpClientEndpoint = httpClientEndpoint;
    ep.config = config;
}

@Description {value:"Gets called whenever a service attaches itself to this endpoint and during package init"}
@Param {value:"serviceType: The service attached"}
public function <HubClientEndpoint ep> register (typedesc serviceType) {
    ep.httpClientEndpoint.register(serviceType);
}

@Description {value:"Starts the registered service"}
public function <HubClientEndpoint ep> start () {
    ep.httpClientEndpoint.start();
}

@Description {value:"Returns the connector that client code uses"}
@Return {value:"The connector that client code uses"}
public function <HubClientEndpoint ep> getClient () returns (HubClientConnector) {
    HubClientConnector webSubHubClientConn = { hubUri:ep.config.uri, httpClientEndpoint:ep.httpClientEndpoint };
    return webSubHubClientConn;
}

@Description {value:"Stops the registered service"}
@Return {value:"Error occured during registration"}
public function <HubClientEndpoint ep> stop () {
    ep.httpClientEndpoint.stop();
}
