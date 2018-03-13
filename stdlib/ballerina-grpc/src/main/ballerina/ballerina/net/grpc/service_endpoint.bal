package ballerina.net.grpc;


@Description {value:"Represents the gRPC server connector connection"}
@Field {value:"remoteHost: The server host name"}
@Field {value:"port: The server port"}
public struct Service {
    string epName;
    ServiceEndpointConfiguration config;
}

@Description {value:"Represents the gRPC server connector connection"}
@Field {value:"remoteHost: The server host name"}
@Field {value:"port: The server port"}
public struct ServiceEndpointConfiguration {
    string host;
    int port;
    SslConfiguration ssl;
}

@Description {value:"Represents the gRPC server connector connection"}
@Field {value:"remoteHost: The server host name"}
@Field {value:"port: The server port"}
public struct SslConfiguration {
    string keyFile;
    string keyChainFile;
    string keyStorePassword;
    string certFile;
    string trustStoreFile;
    string trustStorePassword;

}

@Description { value:"Gets called when the endpoint is being initialize during package init time"}
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

public native function<Service ep> initEndpoint() returns (error);

public native function <Service ep> register (type serviceType);

public native function <Service ep> start ();

public native function <Service ep> stop ();

public native function <Service ep> getConnector () returns (ServerConnector repConn);