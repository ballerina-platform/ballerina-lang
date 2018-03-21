package ballerina.net.http.mock;

import ballerina/net.http;

@Description {value:"Mock service endpoint which does not open a listening port."}
public struct NonListeningServiceEndpoint {
    http:Connection conn;
    http:ServiceEndpointConfiguration config;
}

public function <NonListeningServiceEndpoint ep> init (http:ServiceEndpointConfiguration config) {
    ep.config = config;
    var err = ep.initEndpoint();
    if (err != null) {
        throw err;
    }
}

public native function <NonListeningServiceEndpoint ep> initEndpoint () returns (error);

public native function <NonListeningServiceEndpoint ep> register (typedesc serviceType);

public native function <NonListeningServiceEndpoint ep> start ();

public native function <NonListeningServiceEndpoint ep> getClient() returns http:Connection;

public native function <NonListeningServiceEndpoint ep> stop ();
