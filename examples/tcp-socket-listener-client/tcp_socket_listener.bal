// This is the server implementation for the TCP socket.
import ballerina/io;
import ballerina/log;
import ballerina/socket;

// Bind the service to the port.
// The socket listener should have these four predefined resources.
service echoServer on new socket:Listener(61598) {
    // This resource is invoked when the new client joins.
    resource function onConnect(socket:Caller caller) {
        log:printInfo("Client connected: " + caller.id.toString());
    }

    // This resource is invoked once the content is received from the client.
    resource function onReadReady(socket:Caller caller) {
        var result = caller->read();
        if (result is [byte[], int]) {
            var [content, length] = result;
            if (length > 0) {
                // Create a new `ReadableByteChannel` using the newly received content.
                var byteChannel =
                io:createReadableChannel(content);
                if (byteChannel is io:ReadableByteChannel) {
                    io:ReadableCharacterChannel characterChannel =
                    new io:ReadableCharacterChannel(byteChannel, "UTF-8");
                    var str = characterChannel.read(20);
                    if (str is string) {
                        string reply = <@untainted>str + " back";
                        byte[] payloadByte = reply.toBytes();
                        // Send the reply to the `caller`.
                        int i = 0;
                        int arrayLength = payloadByte.length();
                        while (i < arrayLength) {
                            var writeResult = caller->write(payloadByte);
                            if (writeResult is int) {
                                log:printInfo("Number of bytes written: "
                                    + writeResult.toString());
                                i = i + writeResult;
                                payloadByte = payloadByte.slice(writeResult, arrayLength);
                            } else {
                                log:printError("Unable to write the content",
                                    writeResult);
                            }
                        }
                    } else {
                        log:printError("Error while writing content to the caller",
                            str);
                    }
                }
            } else {
                log:printInfo("Client left: " + caller.id.toString());
            }
        } else {
            io:println(result);
        }
    }

    // This resource is invoked for the error situation
    // if it happens during the `onConnect` and `onReadReady`.
    resource function onError(socket:Caller caller, error er) {
        log:printError("An error occurred", er);
    }
}
