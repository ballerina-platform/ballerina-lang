import ballerina.net.grpc;

endpoint<grpc:Service> ep1 {
host:"localhost",
port:9090
}

@grpc:serviceConfig{
endpoints: [ep1]
}
service<grpc:Service> hello {

@grpc:resourceConfig {
}
resource hello (grpc:Service conn, string name) {
        string message = "Hello " + name; // response message
        grpc:ConnectorError err = conn.send(message);
        if (err != null) {
          // code to execute when there connector error
        }
	_ = conn.complete();
    }
}
