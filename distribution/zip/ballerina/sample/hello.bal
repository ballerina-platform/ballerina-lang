import ballerina.net.grpc;

endpoint<grpc:ServiceEndpoint> ep1 {
port:9090
}

@grpc:ServiceConfig{
port:9090,
generateClientConnector:true}
service<grpc:GrpcService> hello {

resource hello (grpc:ServerConnection conn, string name) {
        string message = "Hello " + name; // response message
        grpc:ConnectorError err = conn.send(message);
        if (err != null) {
          // code to execute when there connector error
        }
	_ = conn.complete();
    }
}
