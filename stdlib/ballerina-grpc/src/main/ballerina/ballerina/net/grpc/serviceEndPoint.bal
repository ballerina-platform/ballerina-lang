package ballerina.net.grpc;


@Description {value:"Represents the gRPC server connector connection"}
@Field {value:"remoteHost: The server host name"}
@Field {value:"port: The server port"}
public struct ServiceEndpoint {
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
    string keyStoreFile;
    string keyStorePassword;
    string certPassword;
    string trustStoreFile;
    string trustStorePassword;
}

@Description { value:"Options struct represents options to be used for gRPC client invocation" }
public struct Options {
    SSL ssl;
}
public function <ServiceEndpointConfiguration config> ServiceEndpointConfiguration() {}

public native function <ServiceEndpoint h> init (string epName, ServiceEndpointConfiguration c);

public native function <ServiceEndpoint h> register (type serviceType);

public native function <ServiceEndpoint h> start ();

public native function <ServiceEndpoint conn> stop ();

public native function <ServiceEndpoint h> getConnector () returns (ResponseConnector repConn);

@Description {value:"Sends outbound response to the caller"}
@Param {value:"conn: The server connector connection"}
@Param {value:"res: The outbound response message"}
@Return {value:"Error occured during HTTP server connector respond"}
public native function <ServiceEndpoint conn> send (any res) (ConnectorError);

@Description {value:"Informs the caller, server finished sending messages."}
@Param {value:"conn: The server connector connection"}
@Return {value:"Error occured during HTTP server connector respond"}
public native function <ServiceEndpoint conn> complete () (ConnectorError);

@Description {value:"Checks whether the connection is closed by the caller."}
@Param {value:"conn: The server connector connection"}
@Return {value:"Returns true if the connection is closed, false otherwise"}
public native function <ServiceEndpoint conn> isCancelled () (boolean);

@Description {value:"Forwards inbound response to the caller"}
@Param {value:"conn: The server connector connection"}
@Param {value:"res: The inbound response message"}
@Return {value:"Error occured during HTTP server connector forward"}
public native function <ServiceEndpoint conn> error (ServerError serverError) (ConnectorError);