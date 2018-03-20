package ballerina.net.http.mock;

import ballerina.net.http;

@Description {value:"Mock service endpoint which does not open a listening port."}
public struct NonListeningService {
    string epName;
    http:ServiceEndpointConfiguration config;
}

public function <NonListeningService ep> init (string epName, http:ServiceEndpointConfiguration config) {
    ep.epName = epName;
    ep.config = config;
    var err = ep.initEndpoint();
    if (err != null) {
        throw err;
    }
}

public native function <NonListeningService ep> initEndpoint () returns (error);

public native function <NonListeningService ep> register (type serviceType);

public native function <NonListeningService ep> start ();

public native function <NonListeningService ep> getConnector () returns (http:ServerConnector);

public native function <NonListeningService ep> stop ();