import ballerina.io;

io:ByteChannel channel;

function initFileChannel (string filePath, string permission) {
    channel = io:openFile(filePath, permission);
}

function readBytes (int numberOfBytes) (blob) {
    blob bytes;
    int numberOfBytesRead;
    bytes, numberOfBytesRead, _ = channel.read(numberOfBytes, numberOfBytes, 0);
    return bytes;
}

function writeBytes (blob content, int startOffset, int size) (int) {
    int numberOfBytesWritten;
    numberOfBytesWritten, _ = channel.write(content, size, startOffset);
    return numberOfBytesWritten;
}

function close () {
    _ = channel.close();
}
