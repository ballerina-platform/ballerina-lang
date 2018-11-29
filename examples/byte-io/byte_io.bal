import ballerina/io;
import ballerina/log;

// Copies content from the source channel to a destination channel.
function copy(io:ReadableByteChannel src,
              io:WritableByteChannel dst) returns error? {
    int readCount = 1;
    byte[] readContent;
    // Here is how to read all the content from
    // the source and copy it to the destination.
    while (readCount > 0) {
        //Operation would attempt to read a maximum of 1000 bytes, the
        //operation would return with the available content which could be
        //< 1000
        (byte[], int) result = check src.read(1000);
        (readContent, readCount) = result;
        //The operation would write the given content into the channel
        var writeResult = check dst.write(readContent, 0);
    }
    return;
}

function close(io:ReadableByteChannel|io:WritableByteChannel ch) {
    abstract object {
        public function close() returns error?;
    } channelResult = ch;
    var cr = channelResult.close();
    if (cr is error) {
        log:printError("Error occured while closing the channel: ", err = cr);
    }
}

public function main() {
    string srcPath = "./files/ballerina.jpg";
    string dstPath = "./files/ballerinaCopy.jpg";
    io:ReadableByteChannel srcCh = io:openReadableFile(srcPath);
    io:WritableByteChannel dstCh = io:openWritableFile(dstPath);
    io:println("Start to copy files from " + srcPath + " to " + dstPath);
    var result = copy(srcCh, dstCh);
    if (result is error) {
        log:printError("error occurred while performing copy ", err = result);
    } else {
        io:println("File copy completed. The copied file could be located in " +
                    dstPath);
    }
    close(srcCh);
    close(dstCh);
}
