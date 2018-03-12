package ballerina.net.http.mock;

import ballerina.net.http;

@Description {value:"Mock service endpoint which does not open a listening port."}
public struct NonListeningServiceEndpoint {
    string epName;
    http:ServiceEndpointConfiguration config;
}

public function <NonListeningServiceEndpoint ep> init (string epName, http:ServiceEndpointConfiguration config) {
    ep.epName = epName;
    ep.config = config;
    ep.initEndpoint();
}

public native function <NonListeningServiceEndpoint ep> initEndpoint ();

public native function <NonListeningServiceEndpoint ep> register (type serviceType);

public native function <NonListeningServiceEndpoint ep> start ();

public native function <NonListeningServiceEndpoint ep> getConnector () returns (http:ServerConnector);

public native function <NonListeningServiceEndpoint ep> stop ();