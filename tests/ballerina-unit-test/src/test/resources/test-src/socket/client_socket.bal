import ballerina/io;
import ballerina/socket;

function oneWayWrite(string msg) {
    socket:Client socketClient = new({host: "localhost", port: 47826});
    byte[] msgByteArray = msg.toByteArray("utf-8");
    var writeResult = socketClient->write(msgByteArray);
    if (writeResult is int) {
        io:println("Number of byte written: ", writeResult);
    } else if (writeResult is error) {
        panic writeResult;
    }
    var closeResult =  socketClient->close();
    if (closeResult is error) {
        io:println(closeResult.reason());
    } else {
        io:println("Client connection closed successfully.");
    }
}

function shutdownWrite(string firstMsg, string secondMsg) returns error? {
    socket:Client socketClient = new({host: "localhost", port: 47826});
    byte[] msgByteArray = firstMsg.toByteArray("utf-8");
    var writeResult = socketClient->write(msgByteArray);
    if (writeResult is int) {
        io:println("Number of byte written: ", writeResult);
    } else if (writeResult is error) {
        panic writeResult;
    }
    var shutdownResult = socketClient->shutdownWrite();
    if (shutdownResult is error) {
        panic shutdownResult;
    }
    msgByteArray = secondMsg.toByteArray("utf-8");
    writeResult = socketClient->write(msgByteArray);
    if (writeResult is int) {
        io:println("Number of byte written: ", writeResult);
    } else if (writeResult is error) {
        var closeResult = socketClient->close();
        if (closeResult is error) {
            io:println(closeResult.reason());
        } else {
            io:println("Client connection closed successfully.");
        }
        return writeResult;
    }
    return;
}
