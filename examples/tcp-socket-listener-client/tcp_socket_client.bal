// This is the client implementation for the TCP socket with the attached callback service. Callback service is optional.
import ballerina/io;
import ballerina/socket;

public function main() {
    // Create a new socket client by providing the host, port, and callback service.
    socket:Client socketClient = new({ host: "localhost", port: 61598,
            callbackService: ClientService });
    string content = "Hello Ballerina";
    byte[] payloadByte = content.toByteArray("UTF-8");
    // Send desired content to the server using the write function.
    var writeResult = socketClient->write(payloadByte);
    if (writeResult is error) {
        io:println("Unable to written the content ", writeResult);
    }
}

// Callback service for the TCP client. The service needs to have four predefined resources.
service ClientService = service {

    // This is invoked once the client connects to the TCP server.
    resource function onConnect(socket:Caller caller) {
        io:println("Connect to: ", caller.remotePort);
    }

    // This is invoked when the server sends any content.
    resource function onReadReady(socket:Caller caller, byte[] content) {
        io:ReadableByteChannel byteChannel = io:createReadableChannel(content);
        io:ReadableCharacterChannel characterChannel =
                        new io:ReadableCharacterChannel(byteChannel, "UTF-8");
        var str = characterChannel.read(25);
        if (str is string) {
            io:println(untaint str);
        } else {
            io:println(str);
        }
        // Close the connection between the server and the client.
        var closeResult = caller->close();
        if (closeResult is error) {
            io:println(closeResult);
        } else {
            io:println("Client connection closed successfully.");
        }
    }

    // This is invoked once the connection is closed.
    resource function onClose(socket:Caller caller) {
        io:println("Leave from: ", caller.remotePort);
    }

    // This resource is invoked for the error situation
    // if it happens during the `onConnect`, `onReadReady`, and `onClose` functions.
    resource function onError(socket:Caller caller, error err) {
        io:println(err);
    }
};
