import ballerina.lang.files;
import ballerina.io;

function getFileChannel (string filePath, string permission) (io:ByteChannel) {
    files:File src = {path:filePath};
    io:ByteChannel channel = src.openChannel(permission);
    return channel;
}

function readBytes (io:ByteChannel channel, int numberOfBytes) (blob, int) {
    blob bytes;
    int numberOfBytesRead;
    bytes, numberOfBytesRead = channel.readBytes(numberOfBytes);
    return bytes, numberOfBytesRead;
}

function writeBytes (io:ByteChannel channel, blob content, int startOffset) (int) {
    int numberOfBytesWritten = channel.writeBytes(content, startOffset);
    return numberOfBytesWritten;
}

function copy (io:ByteChannel src, io:ByteChannel dst) {
    int bytesChunk = 10000;
    blob readContent;
    int readCount = -1;
    int numberOfBytesWritten = 0;

    while (readCount != 0) {
        readContent,readCount = readBytes(src, bytesChunk);
        numberOfBytesWritten = writeBytes(dst, readContent, 0);
    }
}

function main (string[] args) {
    io:ByteChannel sourceChannel = getFileChannel("./files/ballerina.jpg", "r");
    io:ByteChannel destinationChannel = getFileChannel("./files/ballerinaCopy.jpg", "w");
    copy(sourceChannel, destinationChannel);
}
