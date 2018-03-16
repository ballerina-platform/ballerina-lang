package ballerina.net.http;

/////////////////////////////
/// HTTP Service Endpoint ///
/////////////////////////////
public struct ServiceEndpoint {
    // TODO : Make all field Read-Only
    Connection conn;
    ServiceEndpointConfiguration config;
}

@Description {value:"Request validation limits configuration for HTTP service endpoint"}
@Field {value:"maxUriLength: Maximum length allowed in the URL"}
@Field {value:"maxHeaderSize: Maximum size allowed in the headers"}
@Field {value:"maxEntityBodySize: Maximum size allowed in the entity body"}
public struct RequestLimits {
    int maxUriLength = -1;
    int maxHeaderSize = -1;
    int maxEntityBodySize = -1;
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
    TransferEncoding transferEncoding;
    Chunking chunking;
    SslConfiguration ssl;
    string httpVersion;
    RequestLimits requestLimits;
    Filter[] filters;
}

@Description {value:"Initializes a ServiceEndpointConfiguration struct"}
@Param {value:"config: The ServiceEndpointConfiguration struct to be initialized"}
public function <ServiceEndpointConfiguration config> ServiceEndpointConfiguration() {
    config.keepAlive = KeepAlive.AUTO;
    config.chunking = Chunking.AUTO;
    config.transferEncoding = TransferEncoding.CHUNKING;
}

@Field {value:"keyStoreFile: File path to keystore file"}
@Field {value:"keyStorePassword: The keystore password"}
@Field {value:"trustStoreFile: File path to truststore file"}
@Field {value:"trustStorePassword: The truststore password"}
@Field {value:"sslVerifyClient: The type of client certificate verification"}
@Field {value:"certPassword: The certificate password"}
@Field {value:"sslEnabledProtocols: SSL/TLS protocols to be enabled"}
@Field {value:"ciphers: List of ciphers to be used"}
@Field {value:"sslProtocol: The SSL protocol version"}
@Field {value:"validateCertEnabled: The status of validateCertEnabled {default value : false (disable)}"}
@Field {value:"cacheSize: Maximum size of the cache"}
@Field {value:"cacheValidityPeriod: Time duration of cache validity period"}
public struct SslConfiguration {
    string keyStoreFile;
    string keyStorePassword;
    string trustStoreFile;
    string trustStorePassword;
    string sslVerifyClient;
    string certPassword;
    string sslEnabledProtocols;
    string ciphers;
    string sslProtocol;
    boolean validateCertEnabled;
    int cacheSize;
    int cacheValidityPeriod;
}

public enum KeepAlive {
    AUTO, ALWAYS, NEVER
}

@Description { value:"Gets called when the endpoint is being initialized during the package initialization."}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
@Return { value:"Error occured during initialization" }
public function <ServiceEndpoint ep> init (ServiceEndpointConfiguration config) {
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

public native function<ServiceEndpoint ep> initEndpoint () returns (error);

@Description { value:"Gets called every time a service attaches itself to this endpoint. Also happens at package initialization."}
@Param { value:"ep: The endpoint to which the service should be registered to" }
@Param { value:"serviceType: The type of the service to be registered" }
public native function <ServiceEndpoint ep> register (type serviceType);

@Description { value:"Starts the registered service"}
public native function <ServiceEndpoint ep> start ();

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
public native function <ServiceEndpoint ep> getClient () returns (Connection);

@Description { value:"Stops the registered service"}
public native function <ServiceEndpoint ep> stop ();

//////////////////////////////////
/// WebSocket Service Endpoint ///
//////////////////////////////////
public struct WebSocketEndpoint{
    string epName;
    ServiceEndpointConfiguration config;
    ServiceEndpoint httpEndpoint;
    //TODO remove below client
    WebSocketClient wsClient;
}

public function <WebSocketEndpoint ep> WebSocketService() {
    ep.httpEndpoint = {};
}

@Description { value:"Gets called when the endpoint is being initialize during package init time"}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
@Return { value:"Error occured during initialization" }
public function <WebSocketEndpoint ep> init (ServiceEndpointConfiguration config) {
    ep.httpEndpoint.init(config);
}

@Description { value:"gets called every time a service attaches itself to this endpoint - also happens at package init time"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The outbound response message" }
@Return { value:"Error occured during registration" }
public function <WebSocketEndpoint ep> register (type serviceType) {
    ep.httpEndpoint.register(serviceType);
}

@Description { value:"Starts the registered service"}
@Return { value:"Error occured during registration" }
public function <WebSocketEndpoint ep> start () {
    ep.httpEndpoint.start();
}

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
@Return { value:"Error occured during registration" }
//TODO make this native
public function <WebSocketEndpoint ep> getClient () returns (WebSocketConnector) {
    return ep.wsClient.getClient();
}

@Description { value:"Stops the registered service"}
@Return { value:"Error occured during registration" }
public function <WebSocketEndpoint ep> stop () {
    ep.httpEndpoint.stop();
}