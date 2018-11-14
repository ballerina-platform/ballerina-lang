import ballerina/io;
import ballerina/socket;

function oneWayWrite(string msg) {
    endpoint socket:Client client {
        host: "localhost",
        port: 47826
    };
    byte[] msgByteArray = msg.toByteArray("utf-8");
    var writeResult = client->write(msgByteArray);
    if (writeResult is int) {
        io:println("Number of byte written: ", writeResult);
    } else if (writeResult is error) {
        panic writeResult;
    }
    var closeResult =  client->close();
    if (closeResult is error) {
        io:println(closeResult.reason());
    } else {
        io:println("Client connection closed successfully.");
    }
}
