## Module Overview

This module provides an implementation for sending/receiving messages to/from another application process (local or remote) for both connection-oriented and connectionless protocols.

#### TCP Client

The `socket:Client` is used to connect to a socket server and interact with it. The client can only send the data to the server and the client's call-back service can retrieve the data from the server and do multiple requests/responses between the client and the server.

A Client can be defined by providing the host, port, and callbackService as follows.

```ballerina
socket:Client socketClient = new ({host: "localhost", port: 61598, callbackService: ClientService});
string msg = "Hello Ballerina\n";
byte[] message = msg.toBytes();
var writeResult = socketClient->write(message);
```

A client's call-back service can be defined as follows:

```ballerina 
service ClientService = service {
    resource function onConnect(socket:Caller caller) {
        io:println("connect: ", caller.remotePort);
    }
}
```

#### UDP Client
The `socket:UdpClient` is used to interact with the remote UDP host and it can be defined as follows:
```ballerina
socket:UdpClient socketClient = new;
string msg = "Hello from UDP client";
byte[] message = msg.toBytes();
int|socket:Error sendResult = socketClient->sendTo(message, { host: "localhost", port: 48826 });
```

#### Listener
The `socket:Listener` is used to listen to the incoming socket request. The `onConnect(socket:Caller)` resource function gets invoked when a new client is connected. The new client is represented using the `socket:Caller`.
The `onReadReady(socket:Caller)` resource gets invoked once the remote client sends some data.

A `socket:Listener` can be defined as follows:
```ballerina
listener socket:Listener server = new(61598);
service echoServer on server {

    resource function onConnect(socket:Caller caller) {
        io:println("connect: ", caller.remotePort);
    }

    resource function onReadReady(socket:Caller caller) {
        [byte[], int]|socket:ReadTimedOutError result = caller->read();
    }
}
```

For information on the operations, which you can perform with this module, see the below **Functions**. For examples on the usage of the operations, see the following.
 * [Basic TCP Socket Example](https://ballerina.io/learn/by-example/tcp-socket-listener-client.html)
 * [Basic UDP Client Socket Example](https://ballerina.io/learn/by-example/udp-socket-client.html)
