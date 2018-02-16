package ballerina.net.grpc;

public native function getHeader(string headerName) (string);

@Description { value:"Represents the gRPC server connector connection"}
@Field {value:"remoteHost: The server host name"}
@Field {value:"port: The server port"}
public struct Connection {
    string remoteHost;
    int port;
}

@Description { value:"Sends outbound response to the caller"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The outbound response message" }
@Return { value:"Error occured during HTTP server connector respond" }
public native function <Connection conn> send (any res) (ConnectorError);

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