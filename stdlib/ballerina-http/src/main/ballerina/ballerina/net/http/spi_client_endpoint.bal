package ballerina.net.http;

///////////////////////////////
// HTTP Client Endpoint SPI.
///////////////////////////////

public enum Algorithm {
    NONE, // defaults to no algorithm as single URL is default
    LOAD_BALANCE,
    FAIL_OVER
}

public struct TargetService {
    string url;
    //CircuitBreakerConfig cb;
    //SslConfig ssl;
    //ChunkingConfig chunk;
    Filter[] rf;
}

public struct ClientEndpointConfiguration {
    TargetService[] targets;
    Algorithm algorithm;
}

///////////////////////////////
// HTTP Client Endpoint
///////////////////////////////

public struct ClientEndpoint {
    string epName;
    ClientEndpointConfiguration config;
}

public function <ClientEndpoint h> init (string epName, ClientEndpointConfiguration c) {
    h.epName = epName;
    h.config = c;
}

// this gets called every time a service attaches itself to this
// endpoint - also happens at package init time. If the service
// has annotations then the registration code has to go get them
// and do whatever it wants with those
public function <ClientEndpoint h> register (type serviceType) returns (error) {
    // TODO : Make This Native.
    return null;
}

// returns the connector that client code uses
public function <ClientEndpoint h> getConnector () returns (HttpClient) {
    return null;
}

// start
public function <ClientEndpoint h> start () {
}

// stop
public function <ClientEndpoint h> stop () {
}

// add a filter
public function <ClientEndpoint h> addFilter (Filter f) {}

///////////////////////////////
// Simple HTTP Client Endpoint
///////////////////////////////

public struct SimpleClientEndpoint {
    ClientEndpoint ep;
    string epName;
    TargetService config;
}

function <SimpleClientEndpoint h> init (string epName, TargetService c){
    h.ep = {};
    h.ep.init(epName, {targets:[c]});
    h.epName = epName;
    h.config = c;
}

function <SimpleClientEndpoint h> register (type serviceType) returns (error) {
    return h.ep.register(serviceType);
}

function <SimpleClientEndpoint h> getConnector () returns (HttpClient) {
    return null;
}

// start
public function <SimpleClientEndpoint h> start () {
}

// stop
public function <SimpleClientEndpoint h> stop () {
}

function <SimpleClientEndpoint h> addFilter (Filter f) {
    h.ep.addFilter(f);
}
