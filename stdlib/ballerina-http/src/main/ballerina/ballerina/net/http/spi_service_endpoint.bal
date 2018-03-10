package ballerina.net.http;

import ballerina.io;
///////////////////////////////
// HTTP Service Endpoint SPI.
///////////////////////////////
public struct Request {
    // TODO : Implement
}

public struct Response {
    // TODO : Implement
}

///////////////////////////////
// HTTP Service Endpoint
///////////////////////////////

public struct ServiceEndpoint {
    // TODO : Make all field Read-Only
    string epName;
    ServiceEndpointConfiguration config;
}

// this gets called when the endpoint is being initialized
// during package init time
public function <ServiceEndpoint h> init (string epName, ServiceEndpointConfiguration c) {
    h.epName = epName;
    h.config = c;
    io:println("init called ServiceEndpoint");
    // TODO : Write HTTP logic for initializing Service connector, acquiring socket, etc.
}

// this gets called every time a service attaches itself to this
// endpoint - also happens at package init time. If the service
// has annotations then the registration code has to go get them
// and do whatever it wants with those
public function <ServiceEndpoint h> register (type serviceType) {
    // TODO : Make This Native.
    io:println("register called ServiceEndpoint");
}

// returns the connector that client code uses
function <ServiceEndpoint h> getConnector () returns (ResponseConnector) {
    // TODO: Fix this.
    Connection conn = {};
    return new ResponseConnector(conn);
}

// start
public function <ServiceEndpoint h> start () {
    io:println("start called ServiceEndpoint");
    // TODO : Make This Native.
}

// stop
public function <ServiceEndpoint h> stop () {
    io:println("stop called ServiceEndpoint");
    // TODO : Make This Native.
}

////////////////////////////////
// HTTP Service Endpoint Filters
////////////////////////////////

// request filters are called before request is delivered to a service
public struct Filter {
}
function <Filter reqf> filterRequest (Request r) {}
function <Filter reqf> filterResponse (Response r) {}

public function <ServiceEndpoint h> addFilter () {}

//////////////////////////////////////////
// HTTP Service Endpoint response connector
//////////////////////////////////////////

public connector ResponseConnector (Connection conn) {
    action respond (Response res) (HttpConnectorError) {
        return conn.respond(res);
    }
    action forward (Response res) (HttpConnectorError) {
        return conn.forward(res);
    }
    action respondContinue () (HttpConnectorError) {
        return conn.respondContinue();
    }
    action redirect (Response response, RedirectCode code, string[] locations) (HttpConnectorError) {
        return conn.redirect(response, code, locations);
    }
}


//////////////////////////////////////////
// HTTP service/resource config annotations
//////////////////////////////////////////

// TODO : Fix service/resource annotation.
public enum ServiceLifetime {
    REQUEST,
    SESSION,
    SINGLETON
}

public struct ServiceConfiguration {
    ServiceEndpoint[] endpoints;
    ServiceLifetime lifetime; // default ServiceLifetime.SINGLETON;
    string basePath; // defaults to service name
}

public function <ServiceConfiguration sc> ServiceConfiguration () {
    sc.lifetime = ServiceLifetime.SINGLETON;
}

public annotation <service> serviceConfig ServiceConfiguration;

public annotation<resource> resourceConfig resourceConfigData;
