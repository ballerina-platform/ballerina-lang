import ballerina/io;
import ballerina/log;

// Copies the content from the source channel to a destination channel.
function copy(io:ReadableByteChannel src,
              io:WritableByteChannel dst) returns error? {
    // The below example shows how to read all the content from
    // the source and copy it to the destination.
    while (true) {
        // The operation attempts to read a maximum of 1000 bytes and returns
        // with the available content, which could be < 1000.
        byte[]|io:Error result = src.read(1000);
        if (result is io:EofError) {
            break;
        } else if (result is error) {
            return <@untained> result;
        } else {
            // The operation writes the given content into the channel.
            int i = 0;
            while (i < result.length()) {
                var result2 = dst.write(result, i);
                if (result2 is error) {
                    return result2;
                } else {
                    i = i + result2;
                }
            }
        }
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
        log:printError("Error occurred while closing the channel: ", cr);
    }
}

public function main() returns @tainted error? {
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
        log:printError("error occurred while performing copy ", result);
    } else {
        io:println("File copy completed. The copied file is located at " +
                    dstPath);
    }
    // Closes the connections.
    close(srcCh);
    close(dstCh);
}
