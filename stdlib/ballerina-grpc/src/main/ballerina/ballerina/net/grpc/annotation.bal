package ballerina.net.grpc;

@Description { value:"Configuration for gRPC resource"}
@Field {value:"inputMessage: name of the input proto message"}
@Field {value:"outputMessage: name of the output proto message"}
public annotation methodInfo attach resource {
    string inputMessage;
    string outputMessage;
}
