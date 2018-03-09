package ballerina.net.grpc;

public native function getHeader(string headerName) (string);

@Description { value:"Represents the gRPC client connector connection"}
@Field {value:"host: The server host name"}
@Field {value:"port: The server port"}
public struct ClientConnection {
    int port;
    string host;
}

@Description {value:"gRPC protobuf client connector for outbound gRPC requests"}
@Param {value:"serviceUri: Url of the service"}
@Param {value:"connectorOptions: connector options"}
public connector GRPCConnector (string host, int port, string subType, string descriptorKey, map describtorMap) {
    @Description {value:"The execute action implementation of the gRPC Connector."}
    @Param {value:"Connection stub."}
    @Param {value:"Any type of request parameters."}
    native action blockingExecute (string methodID, any payload) (any , ConnectorError);

    @Description {value:"The execute action implementation of the gRPC Connector."}
    @Param {value:"Connection stub."}
    @Param {value:"Any type of request parameters."}
    native action nonBlockingExecute (string methodID, any payload, string listenerService) (ConnectorError);

    @Description {value:"The execute action implementation of the gRPC Connector."}
    @Param {value:"Connection stub."}
    @Param {value:"Any type of request parameters."}
    native action streamingExecute (string methodID, string listenerService) (ClientConnection , ConnectorError);
}

@Description { value:"Sends outbound response to the caller"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The outbound response message" }
@Return { value:"Error occured during HTTP server connector respond" }
public native function <ClientConnection conn> send (any res) (ConnectorError);

@Description { value:"Informs the caller, server finished sending messages."}
@Param { value:"conn: The server connector connection" }
@Return { value:"Error occured during HTTP server connector respond" }
public native function <ClientConnection conn> complete () (ConnectorError);

@Description { value:"Forwards inbound response to the caller"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The inbound response message" }
@Return { value:"Error occured during HTTP server connector forward" }
public native function <ClientConnection conn> error (ServerError serverError) (ConnectorError);

@Description { value:"Represents the gRPC server connector connection"}
@Field {value:"remoteHost: The server host name"}
@Field {value:"port: The server port"}
public struct ServerConnection {
    int id;
}

@Description { value:"Sends outbound response to the caller"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The outbound response message" }
@Return { value:"Error occured during HTTP server connector respond" }
public native function <ServerConnection conn> send (any res) (ConnectorError);

@Description { value:"Informs the caller, server finished sending messages."}
@Param { value:"conn: The server connector connection" }
@Return { value:"Error occured during HTTP server connector respond" }
public native function <ServerConnection conn> complete () (ConnectorError);

@Description { value:"Checks whether the connection is closed by the caller."}
@Param { value:"conn: The server connector connection" }
@Return { value:"Returns true if the connection is closed, false otherwise" }
public native function <ServerConnection conn> isCancelled () (boolean);

@Description { value:"Forwards inbound response to the caller"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The inbound response message" }
@Return { value:"Error occured during HTTP server connector forward" }
public native function <ServerConnection conn> error (ServerError serverError) (ConnectorError);


@Description { value:"ConnectorError struct represents an error occured during the HTTP client invocation" }
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error that caused ConnectorError to get thrown"}
@Field {value:"statusCode: HTTP status code"}
public struct ConnectorError {
    string message;
    error cause;
    int statusCode;
}

@Description { value:"ServerError struct represents an error occured during gRPC server excution" }
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error that caused ServerError to get thrown"}
@Field {value:"statusCode: gRPC server status code. refer: https://github
.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/Status.java"}
public struct ServerError {
    string message;
    error cause;
    int statusCode;
}

@Description { value:"ClientError struct represents an error occured during gRPC client connector" }
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error that caused ClientError to get thrown"}
@Field {value:"statusCode: gRPC server status code. refer: https://github
.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/Status.java"}
public struct ClientError {
    string message;
    error cause;
    int statusCode;
}