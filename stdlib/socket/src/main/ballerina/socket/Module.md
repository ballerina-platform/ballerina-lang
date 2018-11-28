## Module overview
This module provides an implementation for connecting to a remote socket server or acts as a server for an incoming socket request. The module facilitates two types of endpoints called `Client` and `Listener`.
## Samples
### Listener endpoints
The sample given below shows how a listener is used to listen to the incoming socket request. The `onAccept(socket:Caller)` resource function gets invoked when a new client is connected. The new client is represented using the `socket:Caller`.
`onReadReady(socket:Caller, byte[])` resource gets invoked once the remote client sends some data and `onClose(socket:Caller)` is invoked once the client is departs. 
```ballerina
import ballerina/io;
import ballerina/socket;

listener socket:Server server = new ({ port:61598 });

service echoServer on server {
    resource function onAccept (socket:Caller caller) {
        io:println("Join: ", caller.remotePort);
    }

    resource function onReadReady (socket:Caller caller, byte[] content) {
        _ = caller->write(content);
    }

    resource function onClose(socket:Caller caller) {
        io:println("Leave: " + caller.remotePort);
    }

    resource function onError(socket:Caller caller, error er) {
        io:println(er.reason());
    }
}
```
### Client endpoints
Client endpoints are used to connect to and interact with a socket server. The client can only send the data to the server. Client's `callbackService` needs to retrieve the data from the server and do multiple requests/responses between client and the server.
```ballerina
import ballerina/io;
import ballerina/socket;

public function main() {
    socket:Client socketClient = new({host: "localhost", port: 9999, callbackService: ClientService});
    string msg = "Hello Ballerina\n";
    byte[] c1 = msg.toByteArray("utf-8");
    _ = socketClient->write(c1);
}

service ClientService = service {

    resource function onConnect(socket:Caller caller) {
        io:println("connect: ", caller.remotePort);
    }
    
    resource function onReadReady (socket:Caller caller, byte[] content) {
        io:println("client write");	
        _ = caller->write(content);		
    }
    
    resource function onClose(socket:Caller caller) {
        io:println("Leave: ", caller.remotePort);
    }
    
    resource function onError(socket:Caller caller, error er) {
        io:println(er.reason());
    }
};
```
