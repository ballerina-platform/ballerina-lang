package ballerina.net.http;

///////////////////////////////
// HTTP Client Endpoint
///////////////////////////////

@Description {value:"Represents an HTTP client endpoint"}
@Field {value:"epName: The name of the endpoint"}
@Field {value:"config: The configurations associated with the endpoint"}
public struct ClientEndpoint {
    string epName;
    ClientEndpointConfiguration config;
    HttpClient httpClient;
}

public enum Algorithm {
    NONE, // defaults to no algorithm as single URL is default
    LOAD_BALANCE,
    FAIL_OVER
}

@Description {value:"Represents the configurations applied to a particular service."}
@Field {value:"uri: Target service url"}
@Field {value:"ssl: SSL/TLS related options"}
public struct TargetService {
    string uri;
    SSL ssl;
}


@Description {value:"Initializes the ClientEndpointConfiguration struct with default values."}
@Param {value:"config: The ClientEndpointConfiguration struct to be initialized"}
public function <ClientEndpointConfiguration config> ClientEndpointConfiguration() {
    config.chunking = Chunking.AUTO;
    config.transferEncoding = TransferEncoding.CHUNKING;
}

@Description {value:"Gets called when the endpoint is being initialized during the package initialization."}
@Param {value:"ep: The endpoint to be initialized"}
@Param {value:"epName: The endpoint name"}
@Param {value:"config: The ClientEndpointConfiguration of the endpoint"}
public function <ClientEndpoint ep> init(ClientEndpointConfiguration config) {
    string uri = config.targets[0].uri;
    if (uri.hasSuffix("/")) {
        int lastIndex = uri.length() - 1;
        uri = uri.subString(0, lastIndex);
    }
    ep.config = config;
    ep.httpClient = createHttpClient(uri, config);
}

public function <ClientEndpoint ep> register(type serviceType) {

}

public function <ClientEndpoint ep> start() {

}

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
public function <ClientEndpoint ep> getClient() (HttpClient) {
    return ep.httpClient;
}

@Description { value:"Stops the registered service"}
@Return { value:"Error occured during registration" }
public function <ClientEndpoint ep> stop() {

}

public native function createHttpClient(string uri, ClientEndpointConfiguration config) (HttpClient);

function createLoadBalancer (ClientEndpointConfiguration config) (HttpClient) {
    HttpClient[] lbClients = [];
    int i = 0;

    foreach target in config.targets {
        lbClients[i] = createHttpClient(config);
        i = i + 1;
    }

    LoadBalancer lb = {serviceUri:config.targets[0].uri, config:config, loadBalanceClientsArray:lbClients, algorithm:config.algorithm};
    var httpClient, _ = (HttpClient)lb;
    return httpClient;
}

@Description { value:"Retry struct represents retry related options for HTTP client invocation" }
@Field {value:"count: Number of retry attempts before giving up"}
@Field {value:"interval: Retry interval in milliseconds"}
public struct Retry {
    int count;
    int interval;
}

@Description { value:"SSL struct represents SSL/TLS options to be used for HTTP client invocation" }
@Field {value:"trustStoreFile: File path to trust store file"}
@Field {value:"trustStorePassword: Trust store password"}
@Field {value:"keyStoreFile: File path to key store file"}
@Field {value:"keyStorePassword: Key store password"}
@Field {value:"sslEnabledProtocols: SSL/TLS protocols to be enabled. eg: TLSv1,TLSv1.1,TLSv1.2"}
@Field {value:"ciphers: List of ciphers to be used. eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"}
@Field {value:"sslProtocol: SSL Protocol to be used. eg: TLS1.2"}
@Field {value:"validateCertEnabled: The status of validateCertEnabled"}
@Field {value:"cacheSize: Maximum size of the cache"}
@Field {value:"cacheValidityPeriod: Time duration of cache validity period"}
@Field {value:"hostNameVerificationEnabled: Enable/disable host name verification"}
public struct SSL {
    string trustStoreFile;
    string trustStorePassword;
    string keyStoreFile;
    string keyStorePassword;
    string sslEnabledProtocols;
    string ciphers;
    string sslProtocol;
    boolean validateCertEnabled;
    int cacheSize;
    int cacheValidityPeriod;
    boolean hostNameVerificationEnabled;
}

@Description { value:"FollowRedirects struct represents HTTP redirect related options to be used for HTTP client invocation" }
@Field {value:"enabled: Enable redirect"}
@Field {value:"maxCount: Maximun number of redirects to follow"}
public struct FollowRedirects {
    boolean enabled = false;
    int maxCount = 5;
}

@Description { value:"Proxy struct represents proxy server configurations to be used for HTTP client invocation" }
@Field {value:"proxyHost: host name of the proxy server"}
@Field {value:"proxyPort: proxy server port"}
@Field {value:"proxyUserName: Proxy server user name"}
@Field {value:"proxyPassword: proxy server password"}
public struct Proxy {
    string host;
    int port;
    string userName;
    string password;
}

@Description { value:"This struct represents the options to be used for connection throttling" }
@Field {value:"maxActiveConnections: Number of maximum active connections for connection throttling. Default value -1, indicates the number of connections are not restricted"}
@Field {value:"waitTime: Maximum waiting time for a request to grab an idle connection from the client connector"}
public struct ConnectionThrottling {
    int maxActiveConnections = -1;
    int waitTime = 60000;
}
