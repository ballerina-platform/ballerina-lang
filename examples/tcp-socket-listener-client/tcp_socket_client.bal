// This is the client implementation for the TCP socket with the attached callback service. Callback service is optional.
import ballerina/io;
import ballerina/socket;

public function main() {
    // Create a new socket client by providing the host, port, and callback service.
    socket:Client socketClient = new ({
        host: "localhost",
        port: 61598
    });
    string content = "Hello Ballerina";
    byte[] payloadByte = content.toBytes();
    // Send desired content to the server using the write function.
    int i = 0;
    int arrayLength = payloadByte.length();
    while (i < arrayLength) {
        var writeResult = socketClient->write(payloadByte);
        if (writeResult is error) {
            io:println("Unable to written the content ", writeResult);
        } else {
            i = i + writeResult;
            payloadByte = payloadByte.slice(writeResult, arrayLength);
        }
    }
    // Reading response from the server.
    var result = socketClient->read();
    if (result is [byte[], int]) {
        var [reply, length] = result;
        if (length > 0) {
            var byteChannel =
            io:createReadableChannel(reply);
            if (byteChannel is io:ReadableByteChannel) {
                io:ReadableCharacterChannel characterChannel =
                new io:ReadableCharacterChannel(byteChannel, "UTF-8");
                var str = characterChannel.read(25);
                if (str is string) {
                    io:println(<@untainted>str);
                } else {
                    io:println("Error while reading characters ", str);
                }
            } else {
                io:println("Client close: ", socketClient.remotePort);
            }
        }
    } else {
        io:println(result);
    }

    // Close the connection between the server and the client.
    var closeResult = socketClient->close();
    if (closeResult is error) {
        io:println(closeResult);
    } else {
        io:println("Client connection closed successfully.");
    }
}

