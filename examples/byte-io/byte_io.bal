import ballerina/io;
import ballerina/log;

// Copies the content from the source channel to a destination channel.
function copy(io:ReadableByteChannel src,
              io:WritableByteChannel dst) returns error? {
    int readCount = 1;
    byte[] readContent;
    // The below example shows how to read all the content from
    // the source and copy it to the destination.
    while (readCount > 0) {
        // The operation attempts to read a maximum of 1000 bytes and returns
        // with the available content, which could be < 1000.
        [byte[], int] result = check src.read(1000);
        [readContent, readCount] = result;
        // The operation writes the given content into the channel.
        var writeResult = check dst.write(readContent, 0);
    }
    return;
}

// Closes a given readable or writable byte channel.
function close(io:ReadableByteChannel|io:WritableByteChannel ch) {
    abstract object {
        public function close() returns error?;
    } channelResult = ch;
    var cr = channelResult.close();
    if (cr is error) {
        log:printError("Error occurred while closing the channel: ", err = cr);
    }
}

public function main() returns error? {
    string srcPath = "./files/ballerina.jpg";
    string dstPath = "./files/ballerinaCopy.jpg";
    // Initializes the readable byte channel.
    io:ReadableByteChannel srcCh = check io:openReadableFile(srcPath);
    // Initializes the writable byte channel.
    io:WritableByteChannel dstCh = check io:openWritableFile(dstPath);
    io:println("Start to copy files from " + srcPath + " to " + dstPath);
    // Copies the source byte channel to the target byte channel.
    var result = copy(srcCh, dstCh);
    if (result is error) {
        log:printError("error occurred while performing copy ", err = result);
    } else {
        io:println("File copy completed. The copied file is located at " +
                    dstPath);
    }
    // Closes the connections.
    close(srcCh);
    close(dstCh);
}
