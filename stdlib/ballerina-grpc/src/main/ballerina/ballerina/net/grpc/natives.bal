package ballerina.net.grpc;

public native function getHeader(string headerName) (string);

@Description { value:"Represents the gRPC server connector connection"}
@Field {value:"remoteHost: The server host name"}
@Field {value:"port: The server port"}
public struct Connection {
    int port;
    string host;
}
@Description {value:"gRPC protobuf client connector for outbound gRPC requests"}
@Param {value:"serviceUri: Url of the service"}
@Param {value:"connectorOptions: connector options"}
public connector GRPCConnector (string host, int port) {
    @Description {value:"Connection."}
    native action connect (string stubType,map describtorMap) (Connection, ConnectorError);

    @Description {value:"The execute action implementation of the gRPC Connector."}
    @Param {value:"Connection stub."}
    @Param {value:"Any type of request parameters."}
    native action execute (Connection conn, any payload,int methodID) (any , ConnectorError);

    @Description {value:"The execute action implementation of the gRPC Connector."}
    @Param {value:"Connection stub."}
    @Param {value:"Any type of request parameters."}
    native action send (Connection conn, any res) (ConnectorError);

    @Description {value:"The execute action implementation of the gRPC Connector."}
    @Param {value:"Connection stub."}
    @Param {value:"Any type of request parameters."}
    native action complete (Connection conn) (ConnectorError);


    @Description {value:"The execute action implementation of the gRPC Connector."}
    @Param {value:"Connection stub."}
    @Param {value:"Any type of request parameters."}
    native action error (Connection conn, ServerError serverError) (ConnectorError);
}

@Description { value:"Sends outbound response to the caller"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The outbound response message" }
@Return { value:"Error occured during HTTP server connector respond" }
public native function <Connection conn> send (any res) (ConnectorError);

@Description { value:"Informs the caller, server finished sending messages."}
@Param { value:"conn: The server connector connection" }
@Return { value:"Error occured during HTTP server connector respond" }
public native function <Connection conn> complete () (ConnectorError);

@Description { value:"Checks whether the connection is closed by the caller."}
@Param { value:"conn: The server connector connection" }
@Return { value:"Returns true if the connection is closed, false otherwise" }
public native function <Connection conn> isCancelled () (boolean);

@Description { value:"Forwards inbound response to the caller"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The inbound response message" }
@Return { value:"Error occured during HTTP server connector forward" }
public native function <Connection conn> error (ServerError serverError) (ConnectorError);

@Description { value:"ConnectorError struct represents an error occured during the HTTP client invocation" }
@Field {value:"msg:  An error message explaining about the error"}
@Field {value:"cause: The error that caused ConnectorError to get thrown"}
@Field {value:"stackTrace: Represents the invocation stack when ConnectorError is thrown"}
@Field {value:"statusCode: HTTP status code"}
public struct ConnectorError {
    string msg;
    error cause;
    StackFrame[] stackTrace;
    int statusCode;
}

@Description { value:"ServerError struct represents an error occured during gRPC server excution" }
@Field {value:"msg:  An error message explaining about the error"}
@Field {value:"cause: The error that caused ServerError to get thrown"}
@Field {value:"stackTrace: Represents the invocation stack when GrpcServerError is thrown"}
@Field {value:"statusCode: gRPC server status code. refer: https://github
.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/Status.java"}
public struct ServerError {
    string msg;
    error cause;
    StackFrame[] stackTrace;
    int statusCode;
}