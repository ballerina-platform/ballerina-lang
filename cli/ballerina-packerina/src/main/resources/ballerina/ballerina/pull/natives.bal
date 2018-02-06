package ballerina.pull;

import ballerina.file;
import ballerina.io;
import ballerina.net.http;

function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(args[0], {});
    }
    http:OutRequest req = {};
    http:InResponse resp = {};
    resp, _ = httpEndpoint.get("", req);
    writeToFile(resp.getBinaryPayload(), args[1]);
}

function writeToFile (blob readContent, string dstFilePath) {
    io:ByteChannel destinationChannel = getByteChannel(dstFilePath, "w");
    int numberOfBytesWritten = destinationChannel.writeBytes(readContent, 0);
    println("Content saved to the destination directory");
    destinationChannel.close();
}

function getByteChannel (string filePath, string permission) (io:ByteChannel) {
    file:File src = {path:filePath};
    io:ByteChannel channel = src.openChannel(permission);
    return channel;
}