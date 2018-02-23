package ballerina.net.grpc;

@Description { value:"Configuration for gRPC resource"}
@Field {value:"inputMessage: name of the input proto message"}
@Field {value:"outputMessage: name of the output proto message"}
public annotation methodInfo attach resource {
    string inputMessage;
    string outputMessage;
}

public annotation serviceInfo attach service {
    string serviceName;
}

public annotation resourceConfig attach resource {
    boolean streaming;
}

public annotation serviceConfig attach service {
    int port;
    string rpcEndpoint;
    string inputType;
    string outputType;
    boolean clientStreaming;
    boolean serverStreaming;
}