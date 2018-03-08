package ballerina.net.grpc;

public annotation resourceConfig attach resource {
    boolean streaming;
}

@Description { value:"gRPC service configuration"}
@Field {value:"port: gRPC service listening port"}
@Field {value:"rpcEndpoint: gRPC resource name. This applies only for client streaming and bidirectional streaming
where we can define only one resource. In order to generate proto file, we need resource name."}
@Field {value:"clientStreaming: gRPC client streaming service flag. This applies only for client streaming and
bidirectional streaming. Flag sets to true, if the service is client/bidirectional streaming."}
@Field {value:"serverStreaming: gRPC server streaming service flag. This applies only for client streaming and
bidirectional streaming. Flag sets to true, if the service is bidirectional streaming."}
public annotation serviceConfig attach service {
    int port;
    string rpcEndpoint;
    boolean clientStreaming;
    boolean serverStreaming;
    boolean generateClientConnector;
}

@Description { value:"Identify the service as server message listener"}
public annotation messageListener attach service {
}