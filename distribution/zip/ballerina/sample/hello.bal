import ballerina.net.grpc;

endpoint<grpc:ServiceEndpoint> ep1 {
host:"localhost",
port:9090
}

@grpc:serviceConfig{
generateClientConnector:true,
endpoints: [ep1]}
service<grpc:ServiceEndpoint> hello {

resource hello (grpc:ServiceEndpoint conn, string name) {
        string message = "Hello " + name; // response message
        grpc:ConnectorError err = conn.send(message);
        if (err != null) {
          // code to execute when there connector error
        }
	_ = conn.complete();
    }
}
