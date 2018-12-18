// This is the client implementation for the TCP socket with the attached callback service. Callback service is optional.
import ballerina/io;
import ballerina/socket;

public function main() {
    // Create new socket client by providing host, port and callback service.
    socket:Client socketClient = new({ host: "localhost", port: 61598,
            callbackService: ClientService });
    string content = "Hello Ballerina";
    byte[] payloadByte = content.toByteArray("UTF-8");
    // Send desire content to the server using write function.
    var writeResult = socketClient->write(payloadByte);
    if (writeResult is error) {
        io:println("Unable to written the content ", writeResult);
    }
}

// Callback service for the TCP client. Service need to have below four predefine resources.
service ClientService = service {

    // This invokes once the client connect to the TCP server.
    resource function onConnect(socket:Caller caller) {
        io:println("Connect to: ", caller.remotePort);
    }

    // This invokes when the server send any content.
    resource function onReadReady(socket:Caller caller, byte[] content) {
        io:ReadableByteChannel byteChannel = io:createReadableChannel(content);
        io:ReadableCharacterChannel characterChannel =
                        new io:ReadableCharacterChannel(byteChannel, "UTF-8");
        var str = characterChannel.read(25);
        if (str is string) {
            io:println(untaint str);
        } else if (str is error) {
            io:println(str);
        }
        // Close the connection between server.
        var closeResult = caller->close();
        if (closeResult is error) {
            io:println(closeResult);
        } else {
            io:println("Client connection closed successfully.");
        }
    }

    // This invokes upon the connection close.
    resource function onClose(socket:Caller caller) {
        io:println("Leave from: ", caller.remotePort);
    }

    // This resource invokes for the error situation if happen during the onConnect, onReadReady and onClose.
    resource function onError(socket:Caller caller, error err) {
        io:println(err);
    }
};
