import ballerina.net.grpc;

endpoint<grpc:ServiceEndpoint> ep1 {
host:"localhost",
port:9090
}

@grpc:serviceConfig{
endpoints: [ep1],
generateClientConnector:true}
service<grpc:GrpcService> hello {

resource hello (grpc:GrpcService conn, string name) {
        string message = "Hello " + name; // response message
        grpc:ConnectorError err = conn.send(message);
        if (err != null) {
          // code to execute when there connector error
        }
	_ = conn.complete();
    }
}
