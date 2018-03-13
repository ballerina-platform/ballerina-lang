package ballerina.net.grpc;

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
