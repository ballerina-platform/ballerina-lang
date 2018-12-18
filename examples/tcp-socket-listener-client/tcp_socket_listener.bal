// This is the server implementation for the TCP socket.
import ballerina/io;
import ballerina/log;
import ballerina/socket;

// Bind the service to the port.
// Socket listener should have these four predefine resources.
service echoServer on new  socket:Listener(61598) {
    // This resource invokes when the new client joins.
    resource function onAccept(socket:Caller caller) {
        log:printInfo("Client connected: " + caller.id);
    }

    // This resource invokes once content received from the client.
    resource function onReadReady(socket:Caller caller, byte[] content) {
        // Create a new ReadableByteChannel using the newly received content.
        io:ReadableByteChannel byteChannel =
                    io:createReadableChannel(content);
        io:ReadableCharacterChannel characterChannel =
                    new io:ReadableCharacterChannel(byteChannel, "UTF-8");
        var str = characterChannel.read(20);
        if (str is string) {
            string reply = untaint str + " back";
            byte[] payloadByte = reply.toByteArray("UTF-8");
            // Send reply to the caller.
            var writeResult = caller->write(payloadByte);
            if (writeResult is int) {
                log:printInfo("Number of bytes written: " + writeResult);
            } else if (writeResult is error) {
                log:printError("Unable to written the content",
                    err = writeResult);
            }
        } else if (str is error) {
            log:printError("Error while writing content to the caller",
                err = str);
        }
    }

    // This resource invokes when the client departs.
    resource function onClose(socket:Caller caller) {
        log:printInfo("Client leaved: " + caller.id);
    }

    // This resource invokes for the error situation if happen during the onAccpet, onReadReady and onClose.
    resource function onError(socket:Caller caller, error er) {
        log:printError("An error occured", err = er);
    }
}
