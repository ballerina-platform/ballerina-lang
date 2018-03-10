package ballerina.net.grpc;

@Description {value:"Represents the gRPC client connector connection"}
@Field {value:"host: The server host name"}
@Field {value:"port: The server port"}
public struct ClientConnection {
    int port;
    string host;
}

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
}

@Description {value:"ConnectorError struct represents an error occured during the HTTP client invocation"}
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error that caused ConnectorError to get thrown"}
@Field {value:"statusCode: HTTP status code"}
public struct ConnectorError {
    string message;
    error cause;
    int statusCode;
}

@Description {value:"ServerError struct represents an error occured during gRPC server excution"}
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error that caused ServerError to get thrown"}
@Field {value:"statusCode: gRPC server status code. refer: https://github
.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/Status.java"}
public struct ServerError {
    string message;
    error cause;
    int statusCode;
}

@Description {value:"ClientError struct represents an error occured during gRPC client connector"}
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error that caused ClientError to get thrown"}
@Field {value:"statusCode: gRPC server status code. refer: https://github
.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/Status.java"}
public struct ClientError {
    string message;
    error cause;
    int statusCode;
}

public native function getHeader (string headerName) (string);

@Description {value:"gRPC protobuf client connector for outbound gRPC requests"}
@Param {value:"serviceUri: Url of the service"}
@Param {value:"connectorOptions: connector options"}
public connector GRPCConnector (string host, int port, string subType, string descriptorKey, map describtorMap) {
    @Description {value:"The execute action implementation of the gRPC Connector."}
    @Param {value:"Connection stub."}
    @Param {value:"Any type of request parameters."}
    native action blockingExecute (string methodID, any payload) (any, ConnectorError);

    @Description {value:"The execute action implementation of the gRPC Connector."}
    @Param {value:"Connection stub."}
    @Param {value:"Any type of request parameters."}
    native action nonBlockingExecute (string methodID, any payload, string listenerService) (ConnectorError);

    @Description {value:"The execute action implementation of the gRPC Connector."}
    @Param {value:"Connection stub."}
    @Param {value:"Any type of request parameters."}
    native action streamingExecute (string methodID, string listenerService) (ClientConnection, ConnectorError);
}

@Description {value:"Sends outbound response to the caller"}
@Param {value:"conn: The server connector connection"}
@Param {value:"res: The outbound response message"}
@Return {value:"Error occured during HTTP server connector respond"}
public native function <ClientConnection conn> send (any res) (ConnectorError);

@Description {value:"Informs the caller, server finished sending messages."}
@Param {value:"conn: The server connector connection"}
@Return {value:"Error occured during HTTP server connector respond"}
public native function <ClientConnection conn> complete () (ConnectorError);

@Description {value:"Forwards inbound response to the caller"}
@Param {value:"conn: The server connector connection"}
@Param {value:"res: The inbound response message"}
@Return {value:"Error occured during HTTP server connector forward"}
public native function <ClientConnection conn> error (ServerError serverError) (ConnectorError);

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


public connector ResponseConnector (ServiceEndpoint conn) {
    action respondComplete () (ServerError) {
        return conn.complete();
    }
    action respondSend (any res) (ServerError) {
        return conn.send();
    }
    action respondIsCancelled () (ServerError) {
        return conn.isCancelled();
    }
    action respondError (ServerError serverError) {
        conn.error(serverError);
    }
}

//////////////////////////////
/// Grpc Service Endpoint ///
/////////////////////////////
public struct GrpcService {
    string epName;
    ServiceEndpointConfiguration config;
    ServiceEndpoint serviceEndpoint;
}

public function <GrpcService ep> GrpcService () {
    ep.serviceEndpoint = {};
}

@Description {value:"Gets called when the endpoint is being initialize during package init time"}
@Param {value:"epName: The endpoint name"}
@Param {value:"config: The ServiceEndpointConfiguration of the endpoint"}
@Return {value:"Error occured during initialization"}
public function <GrpcService ep> init (string epName, ServiceEndpointConfiguration config) {
    ep.serviceEndpoint.init(epName, config);
}

@Description {value:"gets called every time a service attaches itself to this endpoint - also happens at package init time"}
@Param {value:"conn: The server connector connection"}
@Param {value:"res: The outbound response message"}
@Return {value:"Error occured during registration"}
public function <GrpcService ep> register (type serviceType) {
    ep.serviceEndpoint.register(serviceType);
}

@Description {value:"Starts the registered service"}
@Return {value:"Error occured during registration"}
public function <GrpcService ep> start () {
    ep.serviceEndpoint.start();
}

@Description {value:"Returns the connector that client code uses"}
@Return {value:"The connector that client code uses"}
@Return {value:"Error occured during registration"}
public function <GrpcService ep> getConnector () returns (ServerConnector repConn) {
    return ep.serviceEndpoint.getConnector();
}

@Description {value:"Stops the registered service"}
@Return {value:"Error occured during registration"}
public function <GrpcService ep> stop () {
    ep.serviceEndpoint.stop();
}

