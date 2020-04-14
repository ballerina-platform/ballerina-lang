This module provides an implementation for connecting to a remote socket server or acts as a server for an incoming socket request. The module facilitates two types of endpoints called `socket:Client`, `socket:UdpClient` and `socket:Listener`.

#### TCP Client endpoints
The `socket:Client` is used to connect to a socket server and interact with it. The client can only send the data to the server and the client's `callbackService` can retrieve the data from the server and do multiple requests/responses between the client and the server.

A Client endpoint can be defined by providing host and port as follows:

```ballerina
socket:Client socketClient = new ({host: "localhost", port: 61598});
```

#### UDP Client endpoints
The `socket:UdpClient` is used to interact with the remote UDP host. 

This endpoint can be defined as follows:
```ballerina
socket:UdpClient socketClient = new;
```

### Listener endpoints
The `socket:Listener` is used to listen to the incoming socket request. The `onConnect(socket:Caller)` resource function gets invoked when a new client is connected. The new client is represented using the `socket:Caller`.
The `onReadReady(socket:Caller)` resource gets invoked once the remote client sends some data.

A `socket:Listener` endpoint can be defined as follows:
```ballerina
listener socket:Listener server = new(61598);
```

For information on the operations, which you can perform with this module, see the below **Functions**. For examples on the usage of the operations, see [Basic TCP Socket](https://ballerina.io/learn/by-example/tcp-socket-listener-client.html), [Basic UDP Client Socket](https://ballerina.io/learn/by-example/udp-socket-client.html).
