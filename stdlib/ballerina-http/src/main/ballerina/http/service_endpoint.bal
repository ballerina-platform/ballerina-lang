package ballerina.http;

/////////////////////////////
/// HTTP Service Endpoint ///
/////////////////////////////
documentation {
    Represents service endpoint where one or more services can be registered. so that ballerina program can offer service through this endpoint.

    F{{conn}}  - Service endpoint connection.
    F{{config}} - ServiceEndpointConfiguration configurations.
    F{{remote}}  - The details of remote address.
    F{{local}} - The details of local address.
    F{{protocol}}  - The protocol associate with the service endpoint.
}
public struct ServiceEndpoint {
    // TODO : Make all field Read-Only
    Connection conn;
    ServiceEndpointConfiguration config;
    Remote remote;
    Local local;
    string protocol;
}

documentation {
    Represents the details of remote address.

    F{{host}}  - The remote server host.
    F{{port}} - The remote server port.
}
public struct Remote {
    string host;
    int port;
}

documentation {
    Represents the details of local address.

    F{{host}}  - The local server host.
    F{{port}} - The local server port.
}
public struct Local {
    string host;
    int port;
}

@Description {value:"Request validation limits configuration for HTTP service endpoint"}
@Field {value:"maxUriLength: Maximum length allowed in the URL"}
@Field {value:"maxHeaderSize: Maximum size allowed in the headers"}
@Field {value:"maxEntityBodySize: Maximum size allowed in the entity body"}
public struct RequestLimits {
    int maxUriLength;
    int maxHeaderSize;
    int maxEntityBodySize;
}

@Description {value:"Initializes the RequestLimits struct with default values."}
@Param {value:"config: The RequestLimits struct to be initialized"}
public function <RequestLimits config> RequestLimits() {
    config.maxUriLength = -1;
    config.maxHeaderSize = -1;
    config.maxEntityBodySize = -1;
}

@Description {value:"Configuration for HTTP service endpoint"}
@Field {value:"host: Host of the service"}
@Field {value:"port: Port number of the service"}
@Field {value:"exposeHeaders: The array of allowed headers which are exposed to the client"}
@Field {value:"keepAlive: The keepAlive behaviour of the connection for a particular port"}
@Field {value:"transferEncoding: The types of encoding applied to the response"}
@Field {value:"chunking: The chunking behaviour of the response"}
@Field {value:"ssl: The SSL configurations for the service endpoint"}
@Field {value:"httpVersion: Highest HTTP version supported"}
@Field {value:"requestLimits: Request validation limits configuration"}
@Field {value:"filters: Filters to be applied to the request before dispatched to the actual resource"}
public struct ServiceEndpointConfiguration {
    string host;
    int port;
    KeepAlive keepAlive;
    TransferEncoding|null transferEncoding;
    Chunking chunking;
    ServiceSecureSocket|null secureSocket;
    string httpVersion;
    RequestLimits|null requestLimits;
    Filter[] filters;
}

@Description {value:"Initializes a ServiceEndpointConfiguration struct"}
@Param {value:"config: The ServiceEndpointConfiguration struct to be initialized"}
public function <ServiceEndpointConfiguration config> ServiceEndpointConfiguration() {
    config.keepAlive = KeepAlive.AUTO;
    config.chunking = Chunking.AUTO;
    config.transferEncoding = TransferEncoding.CHUNKING;
    config.httpVersion = "1.1";
    config.port = 9090;
}

@Description { value:"SecureSocket struct represents SSL/TLS options to be used for HTTP service" }
@Field {value: "trustStore: TrustStore related options"}
@Field {value: "keyStore: KeyStore related options"}
@Field {value: "protocols: SSL/TLS protocol related options"}
@Field {value: "validateCert: Certificate validation against CRL or OCSP related options"}
@Field {value:"ciphers: List of ciphers to be used. eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"}
@Field {value:"hostNameVerificationEnabled: Enable/disable host name verification"}
@Field {value:"sslVerifyClient: The type of client certificate verification"}
public struct ServiceSecureSocket {
    TrustStore|null trustStore;
    KeyStore|null keyStore;
    Protocols|null protocols;
    ValidateCert|null validateCert;
    string ciphers;
    string sslVerifyClient;
    boolean sessionCreation;
}

@Description {value:"Initializes the ServiceSecureSocket struct with default values."}
@Param {value:"config: The ServiceSecureSocket struct to be initialized"}
public function <ServiceSecureSocket config> ServiceSecureSocket() {
    config.sessionCreation = true;
}

public enum KeepAlive {
    AUTO, ALWAYS, NEVER
}

@Description { value:"Gets called when the endpoint is being initialized during the package initialization."}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
@Return { value:"Error occured during initialization" }
public function <ServiceEndpoint ep> init(ServiceEndpointConfiguration config) {
    ep.config = config;
    var err = ep.initEndpoint();
    if (err != null) {
        throw err;
    }
    // if filters are defined, call init on them
    if (config.filters != null) {
        foreach filter in config.filters  {
            filter.init();
        }
    }
}

public native function<ServiceEndpoint ep> initEndpoint() returns (error);

@Description { value:"Gets called every time a service attaches itself to this endpoint. Also happens at package initialization."}
@Param { value:"ep: The endpoint to which the service should be registered to" }
@Param { value:"serviceType: The type of the service to be registered" }
public native function <ServiceEndpoint ep> register(typedesc serviceType);

@Description { value:"Starts the registered service"}
public native function <ServiceEndpoint ep> start();

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
public native function <ServiceEndpoint ep> getClient() returns (Connection);

@Description { value:"Stops the registered service"}
public native function <ServiceEndpoint ep> stop();

//////////////////////////////////
/// WebSocket Service Endpoint ///
//////////////////////////////////
public struct WebSocketEndpoint{
    //TODO: Make these readonly
    WebSocketConnector conn;
    ServiceEndpointConfiguration config;
    ServiceEndpoint httpEndpoint;
    //Public attributes
    map attributes;
    string id;
    string negotiatedSubProtocol;
    boolean isSecure;
    boolean isOpen;
    map<string> upgradeHeaders;
}

public function <WebSocketEndpoint ep> WebSocketService() {
    ep.httpEndpoint = {};
}

@Description { value:"Gets called when the endpoint is being initialize during package init time"}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
@Return { value:"Error occured during initialization" }
public function <WebSocketEndpoint ep> init(ServiceEndpointConfiguration config) {
    ep.attributes = {};
    ep.httpEndpoint = {};
    ep.httpEndpoint.init(config);
}

@Description { value:"gets called every time a service attaches itself to this endpoint - also happens at package init time"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The outbound response message" }
@Return { value:"Error occured during registration" }
public function <WebSocketEndpoint ep> register(typedesc serviceType) {
    ep.httpEndpoint.register(serviceType);
}

@Description { value:"Starts the registered service"}
@Return { value:"Error occured during registration" }
public function <WebSocketEndpoint ep> start() {
    ep.httpEndpoint.start();
}

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
@Return { value:"Error occured during registration" }
public function <WebSocketEndpoint ep> getClient() returns (WebSocketConnector) {
    return ep.conn;
}

@Description { value:"Stops the registered service"}
@Return { value:"Error occured during registration" }
public function <WebSocketEndpoint ep> stop() {
    ep.httpEndpoint.stop();
}