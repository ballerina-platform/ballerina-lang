import ballerina.net.grpc;
import ballerina.io;

@grpc:serviceConfig{port:9090}
service<grpc> helloWorld {
    resource hello (grpc:ServerConnection conn, string name) {
        io:println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        string message = "Hello " + name; // response message
        grpc:ConnectorError err = conn.send(message);
        io:println("**************************************************************************************");
        if (err != null) {
          // code to execute when there connector error
        }
        io:println("**************************************************************************************");
	_ = conn.complete();
    }
}
