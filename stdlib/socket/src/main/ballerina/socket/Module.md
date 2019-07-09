## Module overview
This module provides an implementation for connecting to a remote socket server or acts as a server for an incoming socket request. The module facilitates two types of endpoints called `Client` and `Listener`.
## Samples
### TCP Listener endpoints
The sample given below shows how a listener is used to listen to the incoming socket request. The `onConnect(socket:Caller)` resource function gets invoked when a new client is connected. The new client is represented using the `socket:Caller`.
`onReadReady(socket:Caller)` resource gets invoked once the remote client sends some data.
 
```ballerina
import ballerina/log;
import ballerina/socket;

listener socket:Listener server = new(61598);

service echoServer on server {
    resource function onConnect(socket:Caller caller) {
        log:printInfo("Join: " + caller.remotePort);
    }

    resource function onReadReady(socket:Caller caller) {
        var result = caller->read();
        if (result is (byte[], int)) {
            var (content, length) = result;
            if (length > 0) {
                var writeResult = caller->write(content);
                if (writeResult is int) {
                    log:printInfo("Number of bytes written: " + writeResult);
                } else {
                    log:printError("Unable to written the content", err = writeResult);
                }
            } else {
                log:printInfo("Client close: " + caller.remotePort);
            }
        } else {
            log:printError("Unable to read the content", err = result);
        }
    }

    resource function onError(socket:Caller caller, error er) {
        log:printError("An error occurred", err = er);
    }
}
```

### TCP Client endpoints
Client endpoints are used to connect to and interact with a socket server. The client can only send the data to the server. The client's `callbackService` needs to retrieve the data from the server and do multiple requests/responses between the client and the server.

```ballerina
import ballerina/io;
import ballerina/socket;

public function main() {
    socket:Client socketClient = new({ host: "localhost", port: 61598, callbackService: ClientService });
    string msg = "Hello Ballerina\n";
    byte[] c1 = msg.toByteArray("utf-8");
    var writeResult = socketClient->write(c1);
    if (writeResult is int) {
        io:println("Number of bytes written: " , writeResult);
    } else {
        io:println("Unable to written the content", writeResult.detail().message);
    }
}

service ClientService = service {

    resource function onConnect(socket:Caller caller) {
        io:println("connect: ", caller.remotePort);
    }
    
    resource function onReadReady(socket:Caller caller) {
        var result = caller->read();
        if (result is (byte[], int)) {
            var (content, length) = result;
            if (length > 0) {
                var str = getString(content);
                if (str is string) {
                    io:println(untaint str);
                } else {
                    io:println(str.reason());
                }
                var closeResult = caller->close();
                if (closeResult is error) {
                    io:println(closeResult.detail().message);
                } else {
                    io:println("Client connection closed successfully.");
                }
            } else {
                io:println("Client close: ", caller.remotePort);
            }
        } else {
            io:println(result);
        }
    }
    
    resource function onError(socket:Caller caller, error er) {
        io:println(er.reason());
    }
};

function getString(byte[] content) returns string | error {
    io:ReadableByteChannel byteChannel = io:createReadableChannel(content);
    io:ReadableCharacterChannel characterChannel = new io:ReadableCharacterChannel(byteChannel, "UTF-8");
    return characterChannel.read(50);
}
```
### UDP Client endpoints
This is a Ballerina UDP client sample. The `sendTo` and `receiveFrom` actions are available to interact with the remote UDP host.

```ballerina
import ballerina/io;
import ballerina/socket;

public function main() {
    socket:UdpClient socketClient = new;
    string msg = "Hello from UDP client";
    byte[] c1 = msg.toByteArray("utf-8");
    var sendResult =
        socketClient->sendTo(c1, { host: "localhost", port: 48826 });
    if (sendResult is int) {
        io:println("Number of bytes written: ", sendResult);
    } else {
        panic sendResult;
    }
    var result = socketClient->receiveFrom();
    if (result is (byte[], int, socket:Address)) {
        var (content, length, address) = result;
        io:ReadableByteChannel byteChannel = io:createReadableChannel(content);
        io:ReadableCharacterChannel characterChannel = new io:ReadableCharacterChannel(byteChannel, "UTF-8");
        var str = characterChannel.read(60);
        if (str is string) {
            io:println("Received: ", untaint str);
        } else {
            io:println(str.detail().message);
        }
    } else {
        io:println("An error occurred while receiving the data ", result);
    }
    var closeResult = socketClient->close();
    if (closeResult is error) {
        io:println("An error occurred while closing the connection ", closeResult);
    }
}
```