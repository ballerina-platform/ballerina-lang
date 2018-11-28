import ballerina/io;

// Copies content from the source channel to a destination channel.
function copy(io:ReadableByteChannel src, io:WritableByteChannel dst)
             returns error? {
    // Specifies the number of bytes that should be read from a single
    // read operation.
    int numberOfBytesWritten = 0;
    int readCount = 0;
    byte[] readContent;
    boolean doneCopying = false;
    // Here is how to read all the content from
    // the source and copy it to the destination.
    while (!doneCopying) {
        var result = src.read(1000);
        if (result is (byte[], int)) {
            (readContent, readCount) = result;
            if (readCount == 0) {
                //If no content is read, the loop is ended.
                doneCopying = true;
            }
            numberOfBytesWritten = check dst.write(readContent, 0);
        } else if (result is error) {
            return result;
        }
    }
    return;
}

function close(io:ReadableByteChannel|io:WritableByteChannel ch) {
    abstract object {public function close() returns error?;} channelResult = ch;
    var closeResult = channelResult.close();
    if (channelResult.close() is error) {
        var reason = closeResult.reason();
        if (reason is string) {
            io:println("Error occured while closing the channel: " +
                    reason);
        }
    }
}

public function main() {
    string srcFilePath = "./files/ballerina.jpg";
    string dstFilePath = "./files/ballerinaCopy.jpg";
    io:ReadableByteChannel sourceChannel = io:openReadableFile(srcFilePath);
    io:WritableByteChannel destinationChannel = io:openWritableFile(dstFilePath);

    io:println("Start to copy files from " + srcFilePath + " to " + dstFilePath);
    var result = copy(sourceChannel, destinationChannel);
    if (result is error) {
        io:println("error occurred while performing copy " + result.reason());
    }
    io:println("File copy completed. The copied file could be located in " +
            dstFilePath);

    close(sourceChannel);
    close(destinationChannel);
}
