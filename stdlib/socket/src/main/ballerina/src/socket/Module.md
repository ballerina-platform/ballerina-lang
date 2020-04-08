This module provides an implementation for connecting to a remote socket server or acts as a server for an incoming socket request. The module facilitates two types of endpoints called `Client` and `Listener`.

### Client endpoints

There are two types of client endpoints. They are`TCP Client endpoints` and `UDP Client endpoints`.

#### TCP Client endpoints
The `client endpoint` is used to connect to and interact with a socket server. The client can only send the data to the server and client's `callbackService` can retrieve the data from the server and do multiple requests/responses between the client and the server.

A Client endpoint can be defined by providing host and port as follows:

```ballerina
socket:Client socketClient = new ({host: "localhost", port: 61598});
```

#### UDP Client endpoints
The `UDP client endpoint` is used to interact with the remote UDP host. 

This endpoint can be defined as follows:
```ballerina
socket:UdpClient socketClient = new;
```

### Listener endpoints
The `Listener` is used to listen to the incoming socket request. The `onConnect(socket:Caller)` resource function gets invoked when a new client is connected. The new client is represented using the `socket:Caller`.
`onReadReady(socket:Caller)` resource gets invoked once the remote client sends some data.

A `Listener` endpoint can be defined as follows:
```ballerina
listener socket:Listener server = new(61598);
```

For information on the operations, which you can perform with this module, see the below **Functions**. For examples on the usage of the operations, see [
Basic TCP Socket](https://ballerina.io/v1-2/learn/by-example/tcp-socket-listener-client.html) and [
Basic UDP Client Socket](https://ballerina.io/v1-2/learn/by-example/udp-socket-client.html).
