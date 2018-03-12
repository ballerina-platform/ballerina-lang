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
    string keyStoreFile;
    string keyStorePassword;
    string certPassword;
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

public native function <ServiceEndpoint ep> init (string epName, ServiceEndpointConfiguration c);

public native function <ServiceEndpoint ep> register (type serviceType);

public native function <ServiceEndpoint ep> start ();

public native function <ServiceEndpoint ep> stop ();
