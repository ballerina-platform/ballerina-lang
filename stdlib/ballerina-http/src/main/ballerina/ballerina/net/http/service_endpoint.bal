package ballerina.net.http;

/////////////////////////////
/// HTTP Service Endpoint ///
/////////////////////////////
public struct Service {
    // TODO : Make all field Read-Only
    string epName;
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
public struct ServiceEndpointConfiguration {
    string host;
    int port;
    KeepAlive keepAlive;
    TransferEncoding transferEncoding;
    Chunking chunking;
    SslConfiguration ssl;
    string httpVersion;
    RequestLimits requestLimits;
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

//
//public enum Chunking {
//    AUTO, ALWAYS, NEVER
//}
//
//public enum TransferEncoding {
//    CHUNKING
//}

@Description { value:"Gets called when the endpoint is being initialized during the package initialization."}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
@Return { value:"Error occured during initialization" }
public function <Service ep> init (string epName, ServiceEndpointConfiguration config) {
    ep.epName = epName;
    ep.config = config;
    var err = ep.initEndpoint();
    if (err != null) {
        throw err;
    }
}

public native function<Service ep> initEndpoint () returns (error);

@Description { value:"Gets called every time a service attaches itself to this endpoint. Also happens at package initialization."}
@Param { value:"ep: The endpoint to which the service should be registered to" }
@Param { value:"serviceType: The type of the service to be registered" }
public native function <Service ep> register (type serviceType);

@Description { value:"Starts the registered service"}
public native function <Service ep> start ();

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
public native function <Service ep> getConnector () returns (ServerConnector repConn);

@Description { value:"Stops the registered service"}
public native function <Service ep> stop ();

//////////////////////////////////
/// WebSocket Service Endpoint ///
//////////////////////////////////
public struct WebSocketService{
    string epName;
    ServiceEndpointConfiguration config;
    Service serviceEndpoint;
}

public function <WebSocketService ep> WebSocketService() {
    ep.serviceEndpoint = {};
}

@Description { value:"Gets called when the endpoint is being initialize during package init time"}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
@Return { value:"Error occured during initialization" }
public function <WebSocketService ep> init (string epName, ServiceEndpointConfiguration config) {
    ep.serviceEndpoint.init(epName, config);
}

@Description { value:"gets called every time a service attaches itself to this endpoint - also happens at package init time"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The outbound response message" }
@Return { value:"Error occured during registration" }
public function <WebSocketService ep> register (type serviceType) {
    ep.serviceEndpoint.register(serviceType);
}

@Description { value:"Starts the registered service"}
@Return { value:"Error occured during registration" }
public function <WebSocketService ep> start () {
    ep.serviceEndpoint.start();
}

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
@Return { value:"Error occured during registration" }
public function <WebSocketService ep> getConnector () returns (ServerConnector repConn) {
    return ep.serviceEndpoint.getConnector();
}

@Description { value:"Stops the registered service"}
@Return { value:"Error occured during registration" }
public function <WebSocketService ep> stop () {
    ep.serviceEndpoint.stop();
}