package ballerina.net.grpc;

@Description {value:"Represents the gRPC client connector"}
@Field {value:"epName: connector endpoint identifier"}
@Field {value:"config: gRPC client endpoint configuration"}
public struct Client {
    // TODO : Make all field Read-Only
    string epName;
    ClientEndpointConfiguration config;
}

@Description {value:"Represents the gRPC client endpoint configuration"}
@Field {value:"host: The server hostname"}
@Field {value:"port: The server port"}
@Field {value:"ssl: The SSL configurations for the client endpoint"}
public struct ClientEndpointConfiguration {
    string host;
    int port;
    type stub;
    SSL ssl;
}

@Description { value:"SSL struct represents SSL/TLS options to be used for client invocation" }
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

@Description { value:"Gets called when the endpoint is being initialize during package init time"}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ClientEndpointConfiguration of the endpoint" }
@Return { value:"Error occured during initialization" }
public function <Client ep> init (string epName, ClientEndpointConfiguration config) {
    ep.epName = epName;
    ep.config = config;
    ep.initEndpoint();
}

public native function<Client ep> initEndpoint();

@Description { value:"gets called every time a service attaches itself to this endpoint - also happens at package
init time. not supported in client connector"}
@Param { value:"serviceType: The type of the service to be registered" }
@Return { value:"Error occured during registration" }
public native function <Client ep> register (type serviceType);

@Description { value:"Starts the registered service"}
@Return { value:"Error occured during registration" }
public native function <Client ep> start ();

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
@Return { value:"Error occured during registration" }
public native function <Client ep> getConnector() returns (ServiceStub stub);

@Description { value:"Stops the registered service"}
@Return { value:"Error occured during registration" }
public native function <Client ep> stop();

@Description { value:"Returns the client sub that servicestub code uses"}
@Return { value:"client sub that servicestub code uses" }
public native function <Client ep> getStub() returns (any clientSub);