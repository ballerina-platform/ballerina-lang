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
    SSL ssl;
}

@Description {value:"Represents the gRPC server connector connection"}
@Field {value:"remoteHost: The server host name"}
@Field {value:"port: The server port"}
public struct SSL {
    string keyFile;
    string keyChainFile;
    string keyStorePassword;
    string certFile;
    string trustStoreFile;
    string trustStorePassword;

}

@Description { value:"Options struct represents options to be used for gRPC client invocation" }
public struct Options {
    SSL ssl;
}
public function <ServiceEndpointConfiguration config> ServiceEndpointConfiguration() {}

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

public native function <Service ep> getConnector () returns (ResponseConnector repConn);

@Description {value:"Sends outbound response to the caller"}
@Param {value:"conn: The server connector connection"}
@Param {value:"res: The outbound response message"}
@Return {value:"Error occured during HTTP server connector respond"}
public native function <Service conn> send (any res) (ConnectorError);

@Description {value:"Informs the caller, server finished sending messages."}
@Param {value:"conn: The server connector connection"}
@Return {value:"Error occured during HTTP server connector respond"}
public native function <Service conn> complete () (ConnectorError);

@Description {value:"Checks whether the connection is closed by the caller."}
@Param {value:"conn: The server connector connection"}
@Return {value:"Returns true if the connection is closed, false otherwise"}
public native function <Service conn> isCancelled () (boolean);

@Description {value:"Forwards inbound response to the caller"}
@Param {value:"conn: The server connector connection"}
@Param {value:"res: The inbound response message"}
@Return {value:"Error occured during HTTP server connector forward"}
public native function <Service conn> error (ServerError serverError) (ConnectorError);